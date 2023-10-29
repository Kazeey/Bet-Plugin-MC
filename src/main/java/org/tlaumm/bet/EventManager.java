package org.tlaumm.bet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import net.milkbowl.vault.economy.Economy;

public class EventManager
{
    private Map<String, BetEvent> events = new HashMap<>(); // nom de l'événement et l'objet associé
    private static final double MIN_BET_AMOUNT = 100.0;

    public void createEvent(String name)
    {
        events.put(name, new BetEvent(name));
    }

    public List<BetEvent> getActiveEvents()
    {
        return events.values().stream()
                .filter(event -> !event.isClosed())
                .collect(Collectors.toList());
    }

    public void placeBet(String eventName, Player bettor, Player target, double amount, Economy economy)
    {
        BetEvent event = events.get(eventName);
        if(event != null && !event.isClosed())
        {
            if(bettor.getUniqueId().equals(target.getUniqueId()))
            {
                bettor.sendMessage("Vous ne pouvez pas parier sur vous-même!");
                return;
            }

            // Vérifiez si le montant du pari est inférieur à la limite minimale
            if(amount < MIN_BET_AMOUNT)
            {
                bettor.sendMessage("Le montant minimum pour parier est de " + MIN_BET_AMOUNT + " $.");
                return;
            }

            // Vérifiez si le joueur a suffisamment d'argent
            if(economy.getBalance(bettor) < amount)
            {
                bettor.sendMessage("Vous n'avez pas suffisamment d'argent pour parier ce montant.");
                return;
            }

            event.getBets().put(bettor.getUniqueId(), amount);
            bettor.sendMessage("Vous avez parié " + amount + " sur " + target.getName());
        }
        else
        {
            bettor.sendMessage("Cet événement n'existe pas ou est déjà fermé.");
        }
    }

    public void removeBet(String eventName, Player player)
    {
        BetEvent event = events.get(eventName);
        if(event != null)
        {
            event.getBets().remove(player.getUniqueId());
        }
    }

    public Map<UUID, Double> getOdds(String eventName)
    {
        BetEvent event = events.get(eventName);
        Map<UUID, Double> odds = new HashMap<>();
        if(event == null) return odds;

        double totalBets = event.getBets().values().stream().mapToDouble(Double::doubleValue).sum();
        for(UUID playerId : event.getBets().keySet()) {
            double playerBet = event.getBets().get(playerId);
            odds.put(playerId, totalBets / playerBet);
        }

        return odds;
    }

    public boolean setWinner(String eventName, Player winner)
    {
        BetEvent event = events.get(eventName);
        if(event != null && !event.isClosed())
        {
            event.setWinner(winner.getUniqueId());

            return true;
        }

        return false;
    }

    public boolean closeEvent(String eventName)
    {
        BetEvent event = events.get(eventName);
        if(event != null && !event.isClosed())
        {
            event.setClosed(true);

            return event.isClosed();
        }

        return event.isClosed();
    }

    public void distributeWinnings(String eventName, Economy economy)
    {
        BetEvent event = events.get(eventName);
        if(event == null || event.isClosed()) return;

        UUID winnerId = event.getWinner();

        // Obtenir le total des paris placés sur le vainqueur
        double totalBetsOnWinner = event.getBets().entrySet().stream()
                .filter(entry -> entry.getKey().equals(winnerId))
                .mapToDouble(Map.Entry::getValue)
                .sum();

        for(UUID playerId : event.getBets().keySet())
        {
            Player player = Bukkit.getPlayer(playerId);
            double betAmount = event.getBets().get(playerId);

            // Si le joueur a parié sur le vainqueur
            if(playerId.equals(winnerId))
            {
                double odds = totalBetsOnWinner / betAmount;
                double winningAmount = betAmount * odds;
                economy.depositPlayer(player, winningAmount);
                player.sendMessage("Vous avez gagné " + winningAmount + " pièces car vous avez misé sur le gagnant !");
            }
            else
            {
                economy.withdrawPlayer(player, betAmount);
                player.sendMessage("Vous avez perdu votre pari de " + betAmount + " pièces.");
            }
        }

        event.setClosed(true);
    }

    public Map<String, Double> getBetsForPlayer(String playerName)
    {
        Map<String, Double> playerBets = new HashMap<>();

        // Parcourir tous les événements
        for (Map.Entry<String, BetEvent> eventEntry : events.entrySet())
        {
            String eventName = eventEntry.getKey();
            BetEvent betEvent = eventEntry.getValue();

            // Vérifiez si le joueur a fait un pari pour cet événement
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null)
            {
                UUID playerId = player.getUniqueId();
                if (betEvent.getBets().containsKey(playerId))
                {
                    double betAmount = betEvent.getBets().get(playerId);
                    playerBets.put(eventName, betAmount);
                }
            }
        }

        return playerBets;
    }

    public double getCashOutValue(Player player, String eventName) {
        BetEvent event = events.get(eventName);
        if(event == null || event.isClosed()) return 0;

        double playerBet = event.getBets().get(player.getUniqueId());
        double potentialWinnings = playerBet * getOdds(eventName).get(player.getUniqueId());

        // Offrir 75% des gains potentiels comme valeur de cash out
        return 0.75 * potentialWinnings;
    }
}
