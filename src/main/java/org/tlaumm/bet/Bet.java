package org.tlaumm.bet;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Bet extends JavaPlugin {

    private EventManager eventManager;
    private Economy economy;  // Assurez-vous d'avoir Vault et un plugin économique installé pour utiliser Economy

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Economy plugin missing! Disabling BetPlugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        eventManager = new EventManager();

        // Set the executor for all commands
        BetCommandExecutor executor = new BetCommandExecutor(eventManager, economy);
        this.getCommand("createevent").setExecutor(executor);
        this.getCommand("placebet").setExecutor(executor);
        this.getCommand("viewodds").setExecutor(executor);
        this.getCommand("setwinner").setExecutor(executor);
        this.getCommand("closeevent").setExecutor(executor);
        this.getCommand("bethelp").setExecutor(executor);
        this.getCommand("viewevents").setExecutor(executor);
        this.getCommand("mybets").setExecutor(executor);
        this.getCommand("cashout").setExecutor(executor);
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();

        return economy != null;
    }
}

