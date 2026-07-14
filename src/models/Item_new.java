package models;

import java.time.LocalDate;

public class Item_new {
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

    public Item_new(String id, String name, String category, double purchasingPrice,
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

    public String getId() {
        return this.id;
    }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getBatchNo() { return this.batchNo; }
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

    // SETTERS
    public void setDiscountPercent(double percent) {
        this.discountPercent = percent;
    }

    // NEW: allows Management_new to update the selling price (e.g. a manual price
    // revision) which in turn triggers the auto-save feature.
    public void setSellingPrice(double newSellingPrice) {
        this.sellingPrice = newSellingPrice;
    }

    public double getFinalSellingPrice() {
        return this.sellingPrice * (1.0 - (this.discountPercent / 100.0));
    }

    // NEW: convenience method used by Management_new/EmailAlertService_new to describe
    // stock health without duplicating comparison logic everywhere.
    public boolean isBelowMinimumStock(int currentStock) {
        return currentStock < this.minStock;
    }

    public boolean isAboveOptimalStock(int currentStock) {
        return currentStock > this.optimalStock;
    }

    // NEW: reduces this batch's on-hand quantity when a cashier sells units.
    // Returns false (and changes nothing) if the sale would drop stock below
    // zero, so Management_new can reject invalid sales safely.
    public boolean reduceUnitNo(int quantitySold) {
        if (quantitySold <= 0 || quantitySold > this.unitNo) {
            return false;
        }
        this.unitNo -= quantitySold;
        return true;
    }
}
