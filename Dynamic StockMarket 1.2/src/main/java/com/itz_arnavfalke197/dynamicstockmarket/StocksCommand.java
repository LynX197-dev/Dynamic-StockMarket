package com.itz_arnavfalke197.dynamicstockmarket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class StocksCommand implements CommandExecutor, TabCompleter {
   private DynamicStockMarket plugin;

   public StocksCommand(DynamicStockMarket plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage("Only players can use this command.");
         return true;
      } else {
         Player player = (Player) sender;
         if (!player.hasPermission("dsm.user")) {
            player.sendMessage("You don't have permission.");
            return true;
         } else if (args.length == 0) {
            player.sendMessage("Usage: /stocks <buy|sell|info|market|portfolio>");
            return true;
         } else {
            String sub = args[0].toLowerCase();
            if (sub.equals("buy")) {
               return this.handleBuy(player, args);
            } else if (sub.equals("sell")) {
               return this.handleSell(player, args);
            } else if (sub.equals("info")) {
               return this.handleInfo(player, args);
            } else if (sub.equals("market")) {
               return this.handleMarket(player);
            } else if (sub.equals("portfolio")) {
               return this.handlePortfolio(player);
            } else if (sub.equals("leaderboard")) {
               return this.handleLeaderboard(player);
            } else {
               player.sendMessage("Invalid subcommand.");
               return true;
            }
         }
      }
   }

   private boolean handleBuy(Player player, String[] args) {
      if (args.length < 3) {
         player.sendMessage("Usage: /stocks buy <symbol> <amount>");
         return true;
      } else {
         String symbol = args[1].toUpperCase();

         int amount;
         try {
            amount = Integer.parseInt(args[2]);
         } catch (NumberFormatException var9) {
            player.sendMessage("Invalid amount.");
            return true;
         }

         if (amount <= 0) {
            player.sendMessage("Amount must be positive.");
            return true;
         } else {
            Stock stock = this.plugin.getStockManager().getStock(symbol);
            if (stock == null) {
               player.sendMessage("Stock not found.");
               return true;
            } else {
               double cost = stock.getPrice() * (double) amount;
               double tax = cost * this.plugin.getConfig().getDouble("tax-rate", 0.0D);
               double totalCost = cost + tax;
               if (!this.plugin.getEconomy().has(player, totalCost)) {
                  player.sendMessage("Insufficient funds.");
                  return true;
               } else {
                  EconomyResponse response = this.plugin.getEconomy().withdrawPlayer(player, totalCost);
                  if (response.transactionSuccess()) {
                     this.plugin.getDataManager().updateInvestment(player.getUniqueId(), symbol, amount,
                           stock.getPrice());
                     // Dynamic Volatility: Buying increases price slightly (Demand)
                     double impact = 0.0005D * (double) amount;
                     stock.setPrice(stock.getPrice() * (1.0D + impact));
                     player.sendMessage(ChatColor.GREEN + "Bought " + amount + " shares of " + symbol + " for $"
                           + String.format("%.2f", cost) + ChatColor.GRAY + " (Tax: $" + String.format("%.2f", tax)
                           + ")");
                  } else {
                     player.sendMessage("Transaction failed.");
                  }

                  return true;
               }
            }
         }
      }
   }

   private boolean handleSell(Player player, String[] args) {
      if (args.length < 3) {
         player.sendMessage("Usage: /stocks sell <symbol> <amount>");
         return true;
      } else {
         String symbol = args[1].toUpperCase();

         int amount;
         try {
            amount = Integer.parseInt(args[2]);
         } catch (NumberFormatException var11) {
            player.sendMessage("Invalid amount.");
            return true;
         }

         if (amount <= 0) {
            player.sendMessage("Amount must be positive.");
            return true;
         } else {
            List<Investment> invs = this.plugin.getDataManager().getInvestments(player.getUniqueId());
            Investment inv = invs.stream().filter((i) -> {
               return i.getStockSymbol().equals(symbol);
            }).findFirst().orElse(null);
            if (inv != null && inv.getShares() >= amount) {
               Stock stock = this.plugin.getStockManager().getStock(symbol);
               double revenue = stock.getPrice() * (double) amount;
               double tax = revenue * this.plugin.getConfig().getDouble("tax-rate", 0.0D);
               double finalRevenue = revenue - tax;
               EconomyResponse response = this.plugin.getEconomy().depositPlayer(player, finalRevenue);
               if (response.transactionSuccess()) {
                  this.plugin.getDataManager().updateInvestment(player.getUniqueId(), symbol, -amount,
                        stock.getPrice());
                  // Dynamic Volatility: Selling decreases price slightly (Supply)
                  double impact = 0.0005D * (double) amount;
                  stock.setPrice(Math.max(1.0D, stock.getPrice() * (1.0D - impact)));
                  player.sendMessage(ChatColor.GREEN + "Sold " + amount + " shares of " + symbol + " for $"
                        + String.format("%.2f", revenue) + ChatColor.GRAY + " (Tax: $" + String.format("%.2f", tax)
                        + ")");
               } else {
                  player.sendMessage("Transaction failed.");
               }

               return true;
            } else {
               player.sendMessage("Not enough shares.");
               return true;
            }
         }
      }
   }

   private boolean handleInfo(Player player, String[] args) {
      if (args.length < 2) {
         player.sendMessage("Usage: /stocks info <symbol>");
         return true;
      } else {
         String symbol = args[1].toUpperCase();
         Stock stock = this.plugin.getStockManager().getStock(symbol);
         if (stock == null) {
            player.sendMessage("Stock not found.");
            return true;
         } else {
            double price = stock.getPrice();
            double prev = stock.getPreviousPrice();
            String var10001 = String.valueOf(ChatColor.WHITE);
            player.sendMessage(var10001 + symbol + " - " + stock.getName() + ": $"
                  + String.format("%.2f", stock.getPrice()) + " (Vol: " + stock.getVolatility() + ")");
            return true;
         }
      }
   }

   private boolean handleMarket(Player player) {
      this.plugin.getGuiManager().openMarket(player);
      return true;
   }

   private boolean handlePortfolio(Player player) {
      List<Investment> invs = this.plugin.getDataManager().getInvestments(player.getUniqueId());
      if (invs.isEmpty()) {
         player.sendMessage("No investments.");
         return true;
      } else {
         player.sendMessage("Portfolio:");
         Iterator var3 = invs.iterator();

         while (var3.hasNext()) {
            Investment inv = (Investment) var3.next();
            Stock stock = this.plugin.getStockManager().getStock(inv.getStockSymbol());
            double currentValue = stock.getPrice() * (double) inv.getShares();
            String var10001 = inv.getStockSymbol();
            player.sendMessage(
                  var10001 + ": " + inv.getShares() + " shares @ $" + String.format("%.2f", inv.getAvgPrice())
                        + " (Value: $" + String.format("%.2f", currentValue) + ")");
         }

         return true;
      }
   }

   private boolean handleLeaderboard(Player player) {
      Map<String, Double> netWorths = new HashMap();
      Map<UUID, List<Investment>> allData = this.plugin.getDataManager().getAllData();

      for (UUID uuid : allData.keySet()) {
         OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
         double balance = this.plugin.getEconomy().getBalance(op);
         double stockValue = 0.0D;
         for (Investment inv : (List<Investment>) allData.get(uuid)) {
            Stock stock = this.plugin.getStockManager().getStock(inv.getStockSymbol());
            if (stock != null) {
               stockValue += stock.getPrice() * (double) inv.getShares();
            }
         }
         netWorths.put(op.getName() != null ? op.getName() : "Unknown", balance + stockValue);
      }

      List<Map.Entry<String, Double>> sorted = new ArrayList(netWorths.entrySet());
      sorted.sort((a, b) -> ((Double) b.getValue()).compareTo((Double) a.getValue()));

      player.sendMessage(ChatColor.DARK_GREEN + "=== Richest Portfolios ===");
      for (int i = 0; i < Math.min(10, sorted.size()); ++i) {
         Map.Entry<String, Double> entry = (Map.Entry) sorted.get(i);
         player.sendMessage(ChatColor.GOLD + "" + (i + 1) + ". " + ChatColor.WHITE + (String) entry.getKey() + ": "
               + ChatColor.GREEN + "$" + String.format("%.2f", entry.getValue()));
      }
      return true;
   }

   public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         List<String> commands = Arrays.asList("buy", "sell", "info", "market", "portfolio", "leaderboard");
         for (String s : commands) {
            if (s.startsWith(args[0].toLowerCase())) {
               completions.add(s);
            }
         }
      } else if (args.length == 2) {
         if (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("info")) {
            for (String s : this.plugin.getStockManager().getStocks().keySet()) {
               if (s.toLowerCase().startsWith(args[1].toLowerCase())) {
                  completions.add(s);
               }
            }
         }
      }
      return completions;
   }
}
