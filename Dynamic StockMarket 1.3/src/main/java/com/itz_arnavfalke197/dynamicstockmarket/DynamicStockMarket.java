package com.itz_arnavfalke197.dynamicstockmarket;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DynamicStockMarket extends JavaPlugin {
   private static DynamicStockMarket instance;
   private Economy economy;
   private StockManager stockManager;
   private DataManager dataManager;
   private PriceUpdater priceUpdater;
   private GuiManager guiManager;
   private DividendTask dividendTask;

   public void onEnable() {
      instance = this;
      if (!this.setupEconomy()) {
         this.getLogger().severe("Vault economy not found! Disabling plugin.");
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.saveDefaultConfig();
         this.stockManager = new StockManager(this);
         this.dataManager = new DataManager(this);
         this.priceUpdater = new PriceUpdater(this);
         this.guiManager = new GuiManager(this);
         this.getServer().getPluginManager().registerEvents(this.guiManager, this);
         StocksCommand stocksCmd = new StocksCommand(this);
         this.getCommand("stocks").setExecutor(stocksCmd);
         this.getCommand("stocks").setTabCompleter(stocksCmd);
         DSMCommand dsmCmd = new DSMCommand(this);
         this.getCommand("dsm").setExecutor(dsmCmd);
         this.getCommand("dsm").setTabCompleter(dsmCmd);
         this.priceUpdater.start();
         this.dividendTask = new DividendTask(this);
         long divInterval = this.getConfig().getLong("dividend-interval", 3600L) * 20L;
         this.dividendTask.runTaskTimer(this, divInterval, divInterval);
         this.getLogger().info("Dynamic StockMarket v1.3 enabled!");
         // ASCII
         Bukkit.getConsoleSender().sendMessage(AsciiBanner.DESIGN_CREDIT);
      }
   }

   public void onDisable() {
      if (this.priceUpdater != null) {
         this.priceUpdater.stop();
      }

      if (this.dividendTask != null) {
         this.dividendTask.cancel();
      }

      if (this.dataManager != null) {
         this.dataManager.saveData();
      }

      if (this.stockManager != null) {
         this.stockManager.saveStocks();
      }

   }

   private boolean setupEconomy() {
      if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
         return false;
      } else {
         RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
            return false;
         } else {
            this.economy = (Economy) rsp.getProvider();
            return this.economy != null;
         }
      }
   }

   public static DynamicStockMarket getInstance() {
      return instance;
   }

   public Economy getEconomy() {
      return this.economy;
   }

   public StockManager getStockManager() {
      return this.stockManager;
   }

   public DataManager getDataManager() {
      return this.dataManager;
   }

   public GuiManager getGuiManager() {
      return this.guiManager;
   }

   public void reloadPlugin() {
      this.reloadConfig();
      if (this.priceUpdater != null) {
         this.priceUpdater.stop();
      }
      this.priceUpdater = new PriceUpdater(this);
      this.priceUpdater.start();
      if (this.stockManager != null) {
         this.stockManager.reload();
      }
      if (this.dividendTask != null) {
         this.dividendTask.cancel();
      }
      this.dividendTask = new DividendTask(this);
      long divInterval = this.getConfig().getLong("dividend-interval", 3600L) * 20L;
      this.dividendTask.runTaskTimer(this, divInterval, divInterval);
   }
}
