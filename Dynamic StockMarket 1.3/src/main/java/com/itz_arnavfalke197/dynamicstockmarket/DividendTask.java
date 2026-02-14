package com.itz_arnavfalke197.dynamicstockmarket;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class DividendTask extends BukkitRunnable {
    private DynamicStockMarket plugin;

    public DividendTask(DynamicStockMarket plugin) {
        this.plugin = plugin;
    }

    public void run() {
        double yield = this.plugin.getConfig().getDouble("dividend-yield", 0.01D);
        double taxRate = this.plugin.getConfig().getDouble("tax-rate", 0.0D);
        Map<UUID, List<Investment>> allData = this.plugin.getDataManager().getAllData();
        OfflinePlayer bankVault = Bukkit
                .getOfflinePlayer(this.plugin.getConfig().getString("tax-account", "BankVault"));

        for (Map.Entry<UUID, List<Investment>> entry : allData.entrySet()) {
            UUID uuid = entry.getKey();
            List<Investment> investments = entry.getValue();
            double totalDividend = 0.0D;

            for (Investment inv : investments) {
                Stock stock = this.plugin.getStockManager().getStock(inv.getStockSymbol());
                if (stock != null) {
                    totalDividend += stock.getPrice() * (double) inv.getShares() * yield;
                }
            }

            if (totalDividend > 0.0D) {
                double tax = totalDividend * taxRate;
                double netDividend = totalDividend - tax;
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                EconomyResponse response = this.plugin.getEconomy().depositPlayer(player, netDividend);
                if (response.transactionSuccess()) {
                    this.plugin.getEconomy().depositPlayer(bankVault, tax);
                    if (player.isOnline()) {
                        player.getPlayer().sendMessage(ChatColor.GREEN + "You received $"
                                + String.format("%.2f", netDividend) + " in dividends!" + ChatColor.GRAY + " (Tax: $"
                                + String.format("%.2f", tax) + ")");
                    }
                }
            }
        }
    }
}