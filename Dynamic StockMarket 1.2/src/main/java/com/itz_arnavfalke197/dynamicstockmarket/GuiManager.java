package com.itz_arnavfalke197.dynamicstockmarket;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiManager implements Listener {
    private DynamicStockMarket plugin;

    public GuiManager(DynamicStockMarket plugin) {
        this.plugin = plugin;
    }

    public void openMarket(Player player) {
        int size = 9;
        int stockCount = this.plugin.getStockManager().getStocks().size();

        while (size < stockCount) {
            size += 9;
        }

        if (size > 54) {
            size = 54;
        }

        Inventory inv = Bukkit.createInventory((org.bukkit.inventory.InventoryHolder) null, size,
                ChatColor.DARK_GREEN + "Stock Market");

        for (Stock stock : this.plugin.getStockManager().getStocks().values()) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + stock.getName() + " (" + stock.getSymbol() + ")");
            List<String> lore = new ArrayList();
            lore.add(ChatColor.GRAY + "Price: " + ChatColor.GREEN + "$" + String.format("%.2f", stock.getPrice()));
            lore.add(ChatColor.GRAY + "Volatility: " + ChatColor.YELLOW + stock.getVolatility());
            double change = stock.getPrice() - stock.getPreviousPrice();
            String trend = change >= 0.0D ? ChatColor.GREEN + "▲" : ChatColor.RED + "▼";
            lore.add(ChatColor.GRAY + "Trend: " + trend);
            lore.add("");
            lore.add(ChatColor.YELLOW + "L-Click: Buy 1 | Shift+L: Buy 10");
            lore.add(ChatColor.YELLOW + "R-Click: Sell 1 | Shift+R: Sell 10");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(new ItemStack[] { item });
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatColor.DARK_GREEN + "Stock Market")) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()) {
                Player player = (Player) e.getWhoClicked();
                String displayName = e.getCurrentItem().getItemMeta().getDisplayName();
                String symbol = displayName.substring(displayName.lastIndexOf(40) + 1, displayName.lastIndexOf(41));
                int amount = e.isShiftClick() ? 10 : 1;
                if (e.isLeftClick()) {
                    player.performCommand("stocks buy " + symbol + " " + amount);
                } else if (e.isRightClick()) {
                    player.performCommand("stocks sell " + symbol + " " + amount);
                }

                this.openMarket(player);
            }
        }
    }
}