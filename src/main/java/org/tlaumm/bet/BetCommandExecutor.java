package org.tlaumm.bet;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.milkbowl.vault.economy.Economy;

public class BetCommandExecutor implements CommandExecutor
{
    private final EventManager eventManager;
    private final Economy economy;

    public BetCommandExecutor(EventManager eventManager, Economy economy)
    {
        this.eventManager = eventManager;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        switch (cmd.getName().toLowerCase())
        {
            case "createevent":
                return handleCreateEvent(sender, args);
            case "placebet":
                return handleBet(sender, args);
            case "viewodds":
                return handleViewOdds(sender, args);
            case "setwinner":
                return handleSetWinner(sender, args);
            case "closeevent":
                return handleCloseEvent(sender, args);
            case "bethelp":
                return handleHelp(sender);
            case "viewevents":
                return handleViewEvents(sender);
            case "mybets":
                return handleMyBets(sender);
            case "cashout":
                return handleCashOut(sender, args);
            default:
                return false;
        }
    }

    private boolean handleCreateEvent(CommandSender sender, String[] args) {
        if (args.length > 0)
        {
            String eventName = args[0];

            // Vérifiez si un événement avec ce nom existe déjà parmi les événements actifs
            List<BetEvent> activeEvents = eventManager.getActiveEvents();
            for (BetEvent event : activeEvents)
            {
                if (event.getEventName().equalsIgnoreCase(eventName))
                {
                    sender.sendMessage("Un événement actif avec le nom " + eventName + " existe déjà !");
                    return true;
                }
            }

            eventManager.createEvent(eventName);
            sender.sendMessage("Événement " + eventName + " créé !");
            return true;
        }
        return false;
    }

    private boolean handleBet(CommandSender sender, String[] args)
    {
        if (args.length == 3 && sender instanceof Player)
        {
            Player player = (Player) sender;
            String eventName = args[0];
            String playerNameToBetOn = args[1];
            double amount;

            try
            {
                amount = Double.parseDouble(args[2]);
            }
            catch (NumberFormatException e)
            {
                player.sendMessage("Veuillez entrer un montant valide !");
                return true;
            }

            if (player.getName().equalsIgnoreCase(playerNameToBetOn))
            {
                player.sendMessage("Vous ne pouvez pas parier sur vous-même !");
                return true;
            }
            Player playerToBetOn = player.getServer().getPlayer(playerNameToBetOn);

            if (playerToBetOn == null) {
                player.sendMessage("Le joueur " + playerNameToBetOn + " n'est pas en ligne ou n'existe pas.");
                return true;
            }

            eventManager.placeBet(eventName, player, playerToBetOn, amount, economy);
            player.sendMessage("Vous avez parié " + amount + " sur " + playerNameToBetOn + " pour l'événement " + eventName + " !");
            return true;
        }
        return false;
    }

    private boolean handleViewOdds(CommandSender sender, String[] args) {
        if(args.length == 1) {
            String eventName = args[0];
            Map<UUID, Double> odds = eventManager.getOdds(eventName);

            if(odds.isEmpty()) {
                sender.sendMessage("Événement non trouvé ou pas de paris pour cet événement.");
                return true;
            }

            sender.sendMessage("Cotes pour l'événement " + eventName + ":");
            for(Map.Entry<UUID, Double> entry : odds.entrySet()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
                sender.sendMessage(player.getName() + ": " + entry.getValue());
            }
            return true;
        }
        sender.sendMessage("Usage correct: /viewodds <nom_event>");
        return false;
    }

    private boolean handleSetWinner(CommandSender sender, String[] args)
    {
        if(args.length == 2) {
            String eventName = args[0];
            String winnerName = args[1];

            Player winner = Bukkit.getPlayer(winnerName);

            boolean success = eventManager.setWinner(eventName, winner);

            if(success)
            {
                sender.sendMessage(winnerName + " a été défini comme vainqueur pour l'événement " + eventName);

                // Distribuer les gains après avoir défini le gagnant
                eventManager.distributeWinnings(eventName, economy);

            } else
            {
                sender.sendMessage("Erreur lors de la définition du vainqueur. Vérifiez le nom de l'événement et du joueur.");
            }
            return true;
        }
        sender.sendMessage("Usage correct: /setwinner <nom_event> <nom_joueur>");
        return false;
    }

    private boolean handleCloseEvent(CommandSender sender, String[] args)
    {
        if(args.length == 1) {
            String eventName = args[0];

            // Supposons que eventManager a une méthode pour clôturer un événement
            boolean success = eventManager.closeEvent(eventName);

            if(success)
            {
                sender.sendMessage("L'événement " + eventName + " a été clôturé.");
            } else
            {
                sender.sendMessage("Erreur lors de la clôture de l'événement.");
            }
            return true;
        }
        sender.sendMessage("Usage correct: /closeevent <nom_event>");
        return false;
    }

    private boolean handleHelp(CommandSender sender)
    {
        sender.sendMessage("Liste des commandes:");
        sender.sendMessage("/createevent <nom_event>");
        sender.sendMessage("/bet <nom_event> <nom_joueur> <montant>");
        sender.sendMessage("/viewodds <nom_event>");
        sender.sendMessage("/setwinner <nom_event> <nom_joueur>");
        sender.sendMessage("/closeevent <nom_event>");
        sender.sendMessage("/cashout <nom_event>");
        sender.sendMessage("/viewevents");
        sender.sendMessage("/mybets");
        return true;
    }

    private boolean handleViewEvents(CommandSender sender)
    {
        // Supposons que eventManager a une méthode pour récupérer la liste des événements actifs
        List<BetEvent> events = eventManager.getActiveEvents();

        if(events.isEmpty()) {
            sender.sendMessage("Aucun événement actif pour le moment.");
            return true;
        }

        sender.sendMessage("Événements actifs:");
        for(BetEvent event : events) {
            sender.sendMessage("- " + event.getEventName());
        }

        return true;
    }

    private boolean handleMyBets(CommandSender sender)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;

            // Supposons que eventManager a une méthode pour récupérer les paris d'un joueur
            Map<String, Double> bets = eventManager.getBetsForPlayer(player.getName());

            if(bets.isEmpty())
            {
                sender.sendMessage("Vous n'avez pas de paris actifs.");
                return true;
            }

            sender.sendMessage("Vos paris:");
            for(Map.Entry<String, Double> bet : bets.entrySet())
            {
                sender.sendMessage(bet.getKey() + ": " + bet.getValue() + " pièces");
            }
            return true;
        }
        return false;
    }

    private boolean handleCashOut(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seuls les joueurs peuvent effectuer cette action.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("Usage: /bet cashout <nom_event>");
            return true;
        }

        Player player = (Player) sender;
        String eventName = args[0];

        double cashOutValue = eventManager.getCashOutValue(player, eventName);

        if (cashOutValue <= 0) {
            sender.sendMessage("Erreur lors du cash out. Vérifiez le nom de l'événement ou votre pari.");
            return true;
        }

        // Retirer le pari du joueur pour l'événement
        eventManager.removeBet(eventName, player);

        // Mettre à jour le solde du joueur avec la valeur du cash out
        economy.depositPlayer(player, cashOutValue);
        sender.sendMessage("Vous avez utilisé le cash out et avez reçu " + cashOutValue + " pièces!");

        return true;
    }
}
