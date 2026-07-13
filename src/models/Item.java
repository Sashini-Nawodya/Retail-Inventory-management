package models;

import java.time.LocalDate;

public class Item {
    String id;
    String name;
    String category;
    double purchasingPrice;
    double sellingPrice;
    int batchNo;
    int unitNo;
    LocalDate expiryDate;
    private final int minStock;
    private final int optimalStock;

    private double discountPercent = 0.0;

    public Item(String id, String name, String category, double purchasingPrice,
                double sellingPrice, int batchNo, int unitNo, LocalDate expiryDate,int minStock,int optimalStock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.purchasingPrice = purchasingPrice;
        this.sellingPrice = sellingPrice;
        this.batchNo = batchNo;
        this.unitNo = unitNo;
        this.expiryDate = expiryDate;
        this.minStock=minStock;
        this.optimalStock=optimalStock;
    }

    // 3. Define the Getter method (This matches the .getId() line exactly)
    public String getId() {
        return this.id;
    }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getUnitNo() {return this.unitNo;}
    public double getPurchasingPrice() { return purchasingPrice; }
    public double getSellingPrice() { return sellingPrice; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getMinStock() {
        return this.minStock;
    }
    public int getOptimalStock() {
        return this.optimalStock;
    }
    public double getDiscountPercent() { return this.discountPercent; }

    // SETTER
    public void setDiscountPercent(double percent) {
        this.discountPercent = percent;
    }

    public double getFinalSellingPrice() {
        return this.sellingPrice * (1.0 - (this.discountPercent / 100.0));
    }
}


