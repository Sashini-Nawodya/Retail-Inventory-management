package services;

import models.Item_new;
import models.Supplier_new;

import java.time.LocalDate;

public class Management_new {
    private Item_new[] stockroom;
    private int currentItemCount;

    // NEW collaborators for the Version B features
    private final Persistence_new persistence;
    private final SupplierManagement_new supplierManagement;
    private final EmailAlertService_new emailAlertService;
    private final PriceAuditLog_new priceAuditLog;

    // Constructor
    public Management_new() {
        this.stockroom = new Item_new[200];
        this.currentItemCount = 0;
        this.persistence = new Persistence_new();
        this.supplierManagement = new SupplierManagement_new();
        this.emailAlertService = new EmailAlertService_new();
        this.priceAuditLog = new PriceAuditLog_new();

        // Try to resume from a previous run's auto-saved data first.
        Item_new[] restored = persistence.loadInventory(stockroom.length);
        if (restored != null && persistence.getLastLoadedCount() > 0) {
            this.stockroom = restored;
            this.currentItemCount = persistence.getLastLoadedCount();
            System.out.println("[STARTUP] Restored " + currentItemCount + " item(s) from a previous session.");
        } else {
            initializeInventory();
        }
    }

    private void initializeInventory() {
        stockroom[0]=(new Item_new("A-001", "Kothmale Cheese", "A", 400.0, 550.0, 51, 8, LocalDate.of(2026,12,1),3,10));
        stockroom[1]=(new Item_new("A-002", "Kothmale Greek Yogurt", "A", 150.0, 220.0, 102, 5, LocalDate.of(2026, 8, 13),3,10));

        stockroom[2]=(new Item_new("B-001", "Maliban Chocolate Biscuits", "B", 80.0, 120.0, 23, 10, LocalDate.of(2026, 7, 17),5,20));
        stockroom[3]=(new Item_new("B-002", "Maliban Ginger Biscuits", "B", 70.0, 100.0, 15, 6,LocalDate.of(2026, 8, 16),5,20));

        stockroom[4]=(new Item_new("C-001", "Araliya Local Red Rice 5kg", "C", 900.0, 1100.0, 45, 15, LocalDate.of(2027, 8, 10),10,30));
        stockroom[5]=(new Item_new("C-002", "Araliya Keeri Samba 5kg", "C", 1300.0, 1600.0, 54, 14, LocalDate.of(2027, 8, 15),10,30));
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
            Item_new item = stockroom[i];
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

    // FEATURE 2: reject the add if the unit number does not fall within
    // [minStock, optimalStock]. Returns true if the item was accepted.
    private boolean isUnitNoWithinRange(int unitNo, int minStock, int optimalStock) {
        return unitNo >= minStock && unitNo <= optimalStock;
    }

    public void addItem(String id, String name, String category, double pPrice, double sPrice,int batchNo,int unitNo, LocalDate expiryDate,int minStock,int optimalStock) {

        if (minStock > optimalStock) {
            System.out.println("\nWARNING: Item REJECTED! Minimum stock (" + minStock
                    + ") cannot be greater than optimal/maximum stock (" + optimalStock + ").");
            return;
        }

        if (!isUnitNoWithinRange(unitNo, minStock, optimalStock)) {
            System.out.println("\nWARNING: Item REJECTED! Unit quantity (" + unitNo
                    + ") must be between the minimum stock level (" + minStock
                    + ") and the optimal/maximum stock level (" + optimalStock + ").");
            return;
        }

        Item_new newItem = new Item_new(id, name,category , pPrice, sPrice, batchNo, unitNo, expiryDate,minStock,optimalStock);

        if (currentItemCount < stockroom.length) {
            stockroom[currentItemCount] = newItem;
            currentItemCount++;
            System.out.println("Item added successfully!");

            // FEATURE 1: auto-save whenever a new item is added.
            persistence.saveInventory(stockroom, currentItemCount);

            // FEATURE 3: an item can already be outside min/max at intake
            // time (e.g. a huge incoming batch) so check immediately too.
            checkStockThresholds(newItem, checkAvailability(newItem.getId()));
        } else {
            System.out.println("Inventory array is completely full!");
        }
    }

    // NEW: lets Main_new (or future UI) update a selling price directly, which
    // also counts as a "price update" that must trigger the auto-save.
    // changedBy identifies the logged-in staff member for the audit trail.
    public boolean updateItemPrice(String itemId, double newSellingPrice, String changedBy) {
        Item_new item = getItemById(itemId);
        if (item == null) {
            System.out.println("Error: Item ID [" + itemId + "] not found in records.");
            return false;
        }
        if (newSellingPrice < item.getPurchasingPrice()) {
            System.out.println("WARNING: Price update REJECTED! LKR " + newSellingPrice
                    + " is below the wholesale/purchasing floor of LKR " + item.getPurchasingPrice() + ".");
            return false;
        }

        double oldPrice = item.getSellingPrice();
        item.setSellingPrice(newSellingPrice);
        System.out.println("Price updated successfully for [" + itemId + "] to LKR " + newSellingPrice);

        // NEW: audit the price change (old -> new, who, when).
        priceAuditLog.logPriceChange(item.getId(), item.getName(), oldPrice, newSellingPrice, changedBy, "Manual Price Update");

        // FEATURE 1: auto-save whenever a price is updated.
        persistence.saveInventory(stockroom, currentItemCount);
        return true;
    }

    // NEW: cashier checkout — reduces stock by the quantity sold. Deducts
    // from batches in stockroom order (oldest batch first / FIFO) since one
    // item ID can be split across multiple deliveries. Rejects the sale if
    // there isn't enough total stock, so numbers can never go negative.
    public boolean sellItem(String itemId, int quantitySold, String soldBy) {
        if (quantitySold <= 0) {
            System.out.println("WARNING: Sale REJECTED! Quantity sold must be greater than zero.");
            return false;
        }

        int totalAvailable = sumStockForId(itemId);
        if (totalAvailable <= 0) {
            System.out.println("Error: Item ID [" + itemId + "] not found in records or has zero stock.");
            return false;
        }
        if (quantitySold > totalAvailable) {
            System.out.println("WARNING: Sale REJECTED! Only " + totalAvailable + " unit(s) of [" + itemId
                    + "] are in stock; cannot sell " + quantitySold + ".");
            return false;
        }

        int remainingToDeduct = quantitySold;
        Item_new referenceItem = null;

        for (int i = 0; i < currentItemCount && remainingToDeduct > 0; i++) {
            Item_new batch = stockroom[i];
            if (batch == null || !batch.getId().equalsIgnoreCase(itemId)) continue;
            referenceItem = batch;

            int deductHere = Math.min(batch.getUnitNo(), remainingToDeduct);
            if (deductHere > 0 && batch.reduceUnitNo(deductHere)) {
                remainingToDeduct -= deductHere;
            }
        }

        int newTotal = totalAvailable - quantitySold;
        System.out.println("SALE COMPLETE: " + quantitySold + " unit(s) of [" + itemId + "] sold by "
                + soldBy + ". Remaining stock: " + newTotal + " unit(s).");

        // FEATURE 1: auto-save whenever stock changes.
        persistence.saveInventory(stockroom, currentItemCount);

        // FEATURE 3: a sale can push stock below minimum, so check right away.
        if (referenceItem != null) {
            checkStockThresholds(referenceItem, newTotal);
        }

        return true;
    }

    public Item_new getItemById(String searchId) {
        for (int i = 0; i < currentItemCount; i++) {
            if (stockroom[i] != null && stockroom[i].getId().equalsIgnoreCase(searchId)) {
                return stockroom[i];
            }
        }

        return null;
    }

    public int checkAvailability(String itemId) {
        int totalUnits = sumStockForId(itemId);

        // FEATURE 3: whenever stock is checked, evaluate thresholds and
        // fire off a supplier email if we're below minimum or above optimal.
        Item_new referenceItem = getItemById(itemId);
        if (referenceItem != null) {
            checkStockThresholds(referenceItem, totalUnits);
        }

        return totalUnits;
    }

    // Shared helper: sums quantities across every batch that shares an ID
    // (an item can appear multiple times in the stockroom as separate
    // batches/deliveries).
    private int sumStockForId(String itemId) {
        int totalUnits = 0;
        for (int i = 0; i < currentItemCount; i++) {
            if (stockroom[i] != null && stockroom[i].getId().equalsIgnoreCase(itemId)) {
                totalUnits += stockroom[i].getUnitNo();
            }
        }
        return totalUnits;
    }

    // NEW: Low-stock / overstock dashboard — scans the whole stockroom in
    // one pass instead of requiring the user to check one item ID at a time.
    // Also triggers the same supplier email alerts as checkAvailability()
    // for every item it flags, so running the dashboard doubles as a bulk
    // reorder/hold-order sweep.
    public void printLowStockDashboard() {
        System.out.println("\n===================== STOCK HEALTH DASHBOARD =====================");
        System.out.printf("%-8s %-28s %-4s %-9s %-6s %-6s %-10s%n",
                "ID", "Name", "Cat", "Current", "Min", "Max", "Status");
        System.out.println("---------------------------------------------------------------------");

        java.util.HashSet<String> seenIds = new java.util.HashSet<>();
        int lowCount = 0;
        int overCount = 0;

        for (int i = 0; i < currentItemCount; i++) {
            Item_new item = stockroom[i];
            if (item == null || seenIds.contains(item.getId())) continue;
            seenIds.add(item.getId());

            int totalStock = sumStockForId(item.getId());
            String status = "OK";

            if (item.isBelowMinimumStock(totalStock)) {
                status = "LOW";
                lowCount++;
            } else if (item.isAboveOptimalStock(totalStock)) {
                status = "OVERSTOCK";
                overCount++;
            }

            if (!status.equals("OK")) {
                System.out.printf("%-8s %-28s %-4s %-9d %-6d %-6d %-10s%n",
                        item.getId(), item.getName(), item.getCategory(), totalStock,
                        item.getMinStock(), item.getOptimalStock(), status);
                checkStockThresholds(item, totalStock);
            }
        }

        System.out.println("---------------------------------------------------------------------");
        if (lowCount == 0 && overCount == 0) {
            System.out.println("All items are within healthy stock levels. Nothing to report!");
        } else {
            System.out.println(lowCount + " item(s) below minimum stock, " + overCount + " item(s) above optimal stock.");
        }
        System.out.println("=====================================================================");
    }

    // FEATURE 3: compares current stock to the item's min/optimal levels and
    // emails the matching category supplier when it's out of bounds.
    private void checkStockThresholds(Item_new item, int currentStock) {
        Supplier_new supplier = supplierManagement.getSupplierForCategory(item.getCategory());

        if (item.isBelowMinimumStock(currentStock)) {
            System.out.println("[STOCK ALERT] " + item.getId() + " is BELOW minimum stock (" + currentStock + " < " + item.getMinStock() + ").");
            emailAlertService.sendLowStockAlert(supplier, item, currentStock);
        } else if (item.isAboveOptimalStock(currentStock)) {
            System.out.println("[STOCK ALERT] " + item.getId() + " is ABOVE optimal stock (" + currentStock + " > " + item.getOptimalStock() + ").");
            emailAlertService.sendOverstockAlert(supplier, item, currentStock);
        }
    }

    public Item_new[] getStockroom() {
        return this.stockroom;
    }

    public int getCurrentItemCount() {
        return this.currentItemCount;
    }

    // Exposed so Main_new/ReportGenerator_new can trigger a manual save or reuse the
    // same persistence instance instead of creating a new one.
    public Persistence_new getPersistence() {
        return this.persistence;
    }

    public SupplierManagement_new getSupplierManagement() {
        return this.supplierManagement;
    }
}
