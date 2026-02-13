package com.itz_arnavfalke197.dynamicstockmarket;

public class Stock {
   private String symbol;
   private String name;
   private double price;
   private double volatility;
   private double previousPrice;

   public Stock(String symbol, String name, double price, double volatility) {
      this.symbol = symbol;
      this.name = name;
      this.price = price;
      this.volatility = volatility;
      this.previousPrice = price;
   }

   public String getSymbol() {
      return this.symbol;
   }

   public String getName() {
      return this.name;
   }

   public double getPrice() {
      return this.price;
   }

   public void setPrice(double price) {
      this.price = Math.max(0.01D, price);
   }

   public double getVolatility() {
      return this.volatility;
   }

   public double getPreviousPrice() {
      return this.previousPrice;
   }

   public void setPreviousPrice(double previousPrice) {
      this.previousPrice = previousPrice;
   }
}
