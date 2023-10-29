package org.tlaumm.bet;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BetEvent
{
    private String eventName;
    private Map<UUID, Double> bets = new HashMap<>(); // UUID du joueur et montant parié
    private UUID winner;
    private boolean isClosed = false;

    public BetEvent(String eventName)
    {
        this.eventName = eventName;
    }

    public String getEventName()
    {
        return eventName;
    }

    public void setEventName(String eventName)
    {
        this.eventName = eventName;
    }

    public Map<UUID, Double> getBets()
    {
        return bets;
    }

    public void setBets(Map<UUID, Double> bets)
    {
        this.bets = bets;
    }

    public UUID getWinner()
    {
        return winner;
    }

    public void setWinner(UUID winner)
    {
        this.winner = winner;
    }

    public boolean isClosed()
    {
        return isClosed;
    }

    public void setClosed(boolean closed)
    {
        isClosed = closed;
    }
}
