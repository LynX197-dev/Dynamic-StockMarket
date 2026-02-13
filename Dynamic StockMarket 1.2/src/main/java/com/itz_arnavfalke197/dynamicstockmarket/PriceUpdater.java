package com.itz_arnavfalke197.dynamicstockmarket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

public class PriceUpdater extends BukkitRunnable {
   private DynamicStockMarket plugin;
   private int interval;
   private Random random = new Random();
   private double marketTrend = 0.0D;

   public PriceUpdater(DynamicStockMarket plugin) {
      this.plugin = plugin;
      this.interval = plugin.getConfig().getInt("price-update-interval", 300) * 20;
   }

   public void start() {
      this.runTaskTimer(this.plugin, (long) this.interval, (long) this.interval);
   }

   public void stop() {
      this.cancel();
   }

   public void run() {
      double eventMultiplier = 1.0D;
      if (this.random.nextDouble() < this.plugin.getConfig().getDouble("event-chance", 0.05D)) {
         ConfigurationSection events = this.plugin.getConfig().getConfigurationSection("market-events");
         if (events != null) {
            List<String> keys = new ArrayList(events.getKeys(false));
            if (!keys.isEmpty()) {
               String key = (String) keys.get(this.random.nextInt(keys.size()));
               String message = events.getString(key + ".message");
               eventMultiplier = events.getDouble(key + ".multiplier", 1.0D);
               if (message != null) {
                  Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
               }
            }
         }
      }

      this.marketTrend += (this.random.nextDouble() - 0.5D) * 0.05D;
      this.marketTrend = Math.max(-0.2D, Math.min(0.2D, this.marketTrend));
      Iterator var1 = this.plugin.getStockManager().getStocks().values().iterator();

      while (var1.hasNext()) {
         Stock stock = (Stock) var1.next();
         stock.setPreviousPrice(stock.getPrice());
         double changePercent = this.random.nextGaussian() * stock.getVolatility() * 0.05D + this.marketTrend;
         double newPrice = stock.getPrice() * (1.0D + changePercent) * eventMultiplier;
         stock.setPrice(Math.max(1.0D, newPrice));
      }

      this.plugin.getStockManager().saveStocks();
   }
}
