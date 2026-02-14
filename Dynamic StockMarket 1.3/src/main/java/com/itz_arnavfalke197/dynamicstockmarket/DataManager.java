package com.itz_arnavfalke197.dynamicstockmarket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class DataManager {
   private DynamicStockMarket plugin;
   private File playersFile;
   private FileConfiguration playersConfig;
   private Map<UUID, List<Investment>> playerData = new HashMap();

   public DataManager(DynamicStockMarket plugin) {
      this.plugin = plugin;
      this.playersFile = new File(plugin.getDataFolder(), "players.yml");
      this.loadData();
   }

   private void loadData() {
      if (this.playersFile.exists()) {
         this.playersConfig = YamlConfiguration.loadConfiguration(this.playersFile);
         Iterator var1 = this.playersConfig.getKeys(false).iterator();

         while (var1.hasNext()) {
            String key = (String) var1.next();
            UUID uuid = UUID.fromString(key);
            List<Investment> investments = new ArrayList();
            List<Map<?, ?>> list = this.playersConfig.getMapList(key);
            Iterator var6 = list.iterator();

            while (var6.hasNext()) {
               Map<?, ?> map = (Map) var6.next();
               String symbol = (String) map.get("symbol");
               int shares = ((Number) map.get("shares")).intValue();
               double avgPrice = ((Number) map.get("avgPrice")).doubleValue();
               investments.add(new Investment(uuid, symbol, shares, avgPrice));
            }

            this.playerData.put(uuid, investments);
         }

      }
   }

   public void saveData() {
      this.playersConfig = new YamlConfiguration();
      Iterator var1 = this.playerData.entrySet().iterator();

      while (var1.hasNext()) {
         Entry<UUID, List<Investment>> entry = (Entry) var1.next();
         List<Map<String, Object>> list = new ArrayList();
         Iterator var4 = ((List) entry.getValue()).iterator();

         while (var4.hasNext()) {
            Investment inv = (Investment) var4.next();
            Map<String, Object> map = new HashMap();
            map.put("symbol", inv.getStockSymbol());
            map.put("shares", inv.getShares());
            map.put("avgPrice", inv.getAvgPrice());
            list.add(map);
         }

         this.playersConfig.set(((UUID) entry.getKey()).toString(), list);
      }

      try {
         this.playersConfig.save(this.playersFile);
      } catch (IOException var7) {
         var7.printStackTrace();
      }

   }

   public List<Investment> getInvestments(UUID player) {
      return (List) this.playerData.getOrDefault(player, new ArrayList());
   }

   public void updateInvestment(UUID player, String symbol, int shares, double price) {
      List<Investment> invs = (List) this.playerData.computeIfAbsent(player, (k) -> {
         return new ArrayList();
      });
      Investment existing = invs.stream().filter((i) -> {
         return i.getStockSymbol().equals(symbol);
      }).findFirst().orElse(null);
      if (existing != null) {
         int newShares = existing.getShares() + shares;
         if (newShares <= 0) {
            invs.remove(existing);
         } else {
            double newAvg = (existing.getAvgPrice() * (double) existing.getShares() + price * (double) shares)
                  / (double) newShares;
            existing.setShares(newShares);
         }
      } else if (shares > 0) {
         invs.add(new Investment(player, symbol, shares, price));
      }

   }

   public Map<UUID, List<Investment>> getAllData() {
      return this.playerData;
   }
}
