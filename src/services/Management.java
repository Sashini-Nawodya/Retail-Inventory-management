package services;
import models.Item;
import java.util.Scanner;
import java.time.LocalDate;

public class Management {
    private Item[] stockroom;
    private int currentItemCount;

    // Constructor
    public Management() {
        this.stockroom = new Item[200];
        this.currentItemCount = 0;

        initializeInventory();
    }
    private void initializeInventory() {
        // Category A: Kothmale Dairy (Optimal: 10)
        // Note: You will need to pass dates matching your Item constructor, e.g., java.time.LocalDate.now().plusDays(10)
        stockroom[0]=(new Item("A-001", "Kothmale Cheese", "A", 400.0, 550.0, 51, 8, LocalDate.of(2026,12,1),3,10));
        stockroom[1]=(new Item("A-002", "Kothmale Greek Yogurt", "A", 150.0, 220.0, 102, 5, LocalDate.of(2026, 8, 13),3,10));

        // Category B: Maliban Biscuits (Optimal: 20)
        stockroom[2]=(new Item("B-001", "Maliban Chocolate Biscuits", "B", 80.0, 120.0, 23, 10, LocalDate.of(2026, 7, 17),5,20));
        stockroom[3]=(new Item("B-002", "Maliban Ginger Biscuits", "B", 70.0, 100.0, 15, 6,LocalDate.of(2026, 8, 16),5,20));

        // Category C: Araliya Rice (Optimal: 30)
        stockroom[4]=(new Item("C-001", "Araliya Local Red Rice 5kg", "C", 900.0, 1100.0, 45, 15, LocalDate.of(2027, 8, 10),10,30));
        stockroom[5]=(new Item("C-002", "Araliya Keeri Samba 5kg", "C", 1300.0, 1600.0, 54, 14, LocalDate.of(2027, 8, 15),10,30));
        this.currentItemCount = 6;
    }
    public void checkExpirationAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysFromNow = today.plusDays(7);

        System.out.println("\n EXPIRATION ALERTS (Next 7 Days)");
        System.out.println("Today: " + today);
        System.out.println("Checking items expiring on or before: " + sevenDaysFromNow);

        boolean foundExpiringItems = false;

        for (int i = 0; i < currentItemCount; i++) {
            Item item = stockroom[i];
            LocalDate expiry = item.getExpiryDate();

            if ((expiry.isAfter(today) || expiry.isEqual(today)) && (expiry.isBefore(sevenDaysFromNow) || expiry.isEqual(sevenDaysFromNow))) {
                System.out.println("ALERT - [ID: " + item.getId() + "] " + item.getName() +
                            " expires on: " + expiry );
                    foundExpiringItems = true;
                }
            }


        if (!foundExpiringItems) {
            System.out.println("\n No items are expiring within the next 7 days. Safe!");
        }
    }
    public void addItem(String id, String name, String category, double pPrice, double sPrice,int batchNo,int unitNo, LocalDate expiryDate,int minStock,int optimalStock) {

        Item newItem = new Item(id, name,category , pPrice, sPrice, batchNo, unitNo, expiryDate,minStock,optimalStock);

        if (currentItemCount < stockroom.length) {
            stockroom[currentItemCount] = newItem;
            currentItemCount++;
            System.out.println("Item added successfully!");
        } else {
            System.out.println("Inventory array is completely full!");
        }
    }

    // Updated availability check logic to loop through a standard fixed array safely

    public Item getItemById(String searchId) {
        for (int i = 0; i < currentItemCount; i++) {
            if (stockroom[i] != null && stockroom[i].getId().equalsIgnoreCase(searchId)) {
                return stockroom[i];
            }
        }

        return null;
    }

    public int checkAvailability(String itemId) {
        int totalUnits = 0;

        for (int i = 0; i < currentItemCount; i++) {
            if (stockroom[i] != null && stockroom[i].getId().equalsIgnoreCase(itemId)) {

                totalUnits += stockroom[i].getUnitNo();
            }
        }

        return totalUnits;
    }
    public Item[] getStockroom() {
        return this.stockroom;
    }

    public int getCurrentItemCount() {
        return this.currentItemCount;
    }
}


