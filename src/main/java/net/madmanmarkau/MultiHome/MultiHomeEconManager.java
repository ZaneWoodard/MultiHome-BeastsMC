package net.madmanmarkau.MultiHome;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.RegisteredServiceProvider;

public class MultiHomeEconManager {

	public static EconomyHandler handler;
	private static Economy vault = null;
	public static MultiHome plugin;

	public enum EconomyHandler {
		VAULT, NONE
	}

	protected static void initialize(MultiHome plugin) {
		MultiHomeEconManager.plugin = plugin;
		
		if (Settings.isEconomyEnabled()) {
	        RegisteredServiceProvider<Economy> vaultEconomyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
	        if (vaultEconomyProvider != null) {
				handler = EconomyHandler.VAULT;
	        	vault = vaultEconomyProvider.getProvider();
				Messaging.logInfo("Economy enabled using: Vault", plugin);
				return;
	        }
			
			handler = EconomyHandler.NONE;
			Messaging.logWarning("An economy plugin wasn't detected!", plugin);
		} else {
			handler = EconomyHandler.NONE;
		}
	}

	// Determine if player has enough money to cover [amount]
	public static boolean hasEnough(String player, double amount) {
		if (vault != null) {
			return vault.has(player, amount);
		}
		return true;
	}

	// Remove [amount] from players account
	public static boolean chargePlayer(String player, double amount) {
        if (vault != null) {
            return vault.bankWithdraw(player, amount).transactionSuccess();
        }
		return true;
	}

	// Format the monetary amount into a string, according to the configured format
	public static String formatCurrency(double amount) {
        if (vault != null) {
            return vault.format(amount);
        }
		return Double.toString(amount);
	}
}