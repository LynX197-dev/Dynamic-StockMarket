package com.itz_arnavfalke197.dynamicstockmarket;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class StockManager {
   private DynamicStockMarket plugin;
   private Map<String, Stock> stocks = new HashMap();
   private File stocksFile;
   private FileConfiguration stocksConfig;

   public StockManager(DynamicStockMarket plugin) {
      this.plugin = plugin;
      this.stocksFile = new File(plugin.getDataFolder(), "stocks.yml");
      this.loadStocks();
      this.loadFromConfig();
   }

   private void loadFromConfig() {
      FileConfiguration config = this.plugin.getConfig();
      List<Map<?, ?>> stockList = config.getMapList("starting-stocks");
      Iterator var3 = stockList.iterator();

      while(var3.hasNext()) {
         Map<?, ?> map = (Map)var3.next();
         String symbol = (String)map.get("symbol");
         String name = (String)map.get("name");
         double price = ((Number)map.get("price")).doubleValue();
         double volatility = ((Number)map.get("volatility")).doubleValue();
         if (!this.stocks.containsKey(symbol)) {
            this.stocks.put(symbol, new Stock(symbol, name, price, volatility));
         }
      }

   }

   private void loadStocks() {
      if (this.stocksFile.exists()) {
         this.stocksConfig = YamlConfiguration.loadConfiguration(this.stocksFile);
         Iterator var1 = this.stocksConfig.getKeys(false).iterator();

         while(var1.hasNext()) {
            String key = (String)var1.next();
            String name = this.stocksConfig.getString(key + ".name");
            double price = this.stocksConfig.getDouble(key + ".price");
            double volatility = this.stocksConfig.getDouble(key + ".volatility");
            this.stocks.put(key, new Stock(key, name, price, volatility));
         }

      }
   }

   public void saveStocks() {
      this.stocksConfig = new YamlConfiguration();
      Iterator var1 = this.stocks.values().iterator();

      while(var1.hasNext()) {
         Stock stock = (Stock)var1.next();
         this.stocksConfig.set(stock.getSymbol() + ".name", stock.getName());
         this.stocksConfig.set(stock.getSymbol() + ".price", stock.getPrice());
         this.stocksConfig.set(stock.getSymbol() + ".volatility", stock.getVolatility());
      }

      try {
         this.stocksConfig.save(this.stocksFile);
      } catch (IOException var3) {
         var3.printStackTrace();
      }

   }

   public Stock getStock(String symbol) {
      return (Stock)this.stocks.get(symbol);
   }

   public Map<String, Stock> getStocks() {
      return this.stocks;
   }

   public void reload() {
      this.stocks.clear();
      this.loadStocks();
      this.loadFromConfig();
   }
}
