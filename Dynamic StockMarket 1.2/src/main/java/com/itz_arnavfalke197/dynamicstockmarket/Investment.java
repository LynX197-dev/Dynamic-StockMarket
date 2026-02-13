package com.itz_arnavfalke197.dynamicstockmarket;

import java.util.UUID;

public class Investment {
   private UUID player;
   private String stockSymbol;
   private int shares;
   private double avgPrice;
   private double totalInvest;

   public Investment(UUID player, String stockSymbol, int shares, double avgPrice) {
      this.player = player;
      this.stockSymbol = stockSymbol;
      this.shares = shares;
      this.avgPrice = avgPrice;
      this.totalInvest = (double)shares * avgPrice;
   }

   public UUID getPlayer() {
      return this.player;
   }

   public String getStockSymbol() {
      return this.stockSymbol;
   }

   public int getShares() {
      return this.shares;
   }

   public void setShares(int shares) {
      this.shares = shares;
      this.totalInvest = (double)shares * this.avgPrice;
   }

   public double getAvgPrice() {
      return this.avgPrice;
   }

   public double getTotalInvest() {
      return this.totalInvest;
   }
}
