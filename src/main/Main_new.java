package main;

import java.util.Scanner;
import java.time.LocalDate;

import models.Item_new;
import models.Staff_new;
import services.Management_new;
import services.Login_new;
import services.Discount_new;
import services.ReportGenerator_new;

public class Main_new {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        Login_new loginStaff = new Login_new();
        Management_new inventoryManager = new Management_new();
        Discount_new discountService = new Discount_new();
        ReportGenerator_new reportGenerator = new ReportGenerator_new();

        System.out.println("\nRETAIL INVENTORY CONTROL SYSTEM ");

        Staff_new loggedIn = null;

        while (loggedIn == null) {
            System.out.print("\nEnter Staff ID: ");
            String id = sc.nextLine();

            System.out.print("Enter 4-Digit PIN: ");
            String pin = sc.nextLine();

            loggedIn = loginStaff.loginVerify(id, pin);

            if (loggedIn == null) {
                System.out.println("INVALID! Try again.");
            }
        }

        System.out.println("\nACCESS GRANTED! Welcome, " + loggedIn.getName());

        // NEW: Only this staff ID may generate the end-of-month report.
        // All other menu options remain open to every logged-in staff member.
        final String MANAGER_STAFF_ID = "EMP-01";

        // Used for the price-change audit trail (Feature: who made the change).
        String staffIdentity = loggedIn.getName() + " (" + loggedIn.getStaffId() + ")";

        int choice;
        do {
            System.out.println("\nEnter what task you want to do (1-9):");
            System.out.println("1. Add items");
            System.out.println("2. Check availability & Manage Discounts");
            System.out.println("3. Sell Items (Cashier Checkout)");
            System.out.println("4. Run Expiration Checks");
            System.out.println("5. Update Item Price");
            System.out.println("6. Add New Supplier");
            System.out.println("7. Low-Stock Dashboard");
            System.out.println("8. Generate End-of-Month Report (Manager Only)");
            System.out.println("9. Exit");
            System.out.print("Choice: ");

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("\n--- ADD NEW INVENTORY ITEM ---");

                    System.out.print("Enter Item ID: ");
                    String id = sc.nextLine();

                    System.out.print("Enter Item Name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter Category (A/B/C): ");
                    String category = sc.nextLine();

                    System.out.print("Enter Purchasing Price: ");
                    double pPrice = sc.nextDouble();

                    System.out.print("Enter Selling Price: ");
                    double sPrice = sc.nextDouble();

                    System.out.print("Enter Batch Number: ");
                    int batchNo = sc.nextInt();

                    System.out.print("Enter Unit Quantity Number: ");
                    int unitNo = sc.nextInt();

                    System.out.print("Enter Expiry Year (YYYY): ");
                    int year = sc.nextInt();

                    System.out.print("Enter Expiry Month (1-12): ");
                    int month = sc.nextInt();

                    System.out.print("Enter Expiry Day (1-31): ");
                    int day = sc.nextInt();

                    System.out.print("Enter minimum stock count: ");
                    int minStock = sc.nextInt();
                    System.out.print("Enter maximum stock count: ");
                    int optimalStock = sc.nextInt();
                    sc.nextLine();

                    LocalDate expiryDate = LocalDate.of(year, month, day);
                    // Management_new.addItem() now validates unitNo against
                    // [minStock, optimalStock], auto-saves on success, and
                    // fires a supplier email alert if it's out of bounds.
                    inventoryManager.addItem(id, name, category, pPrice, sPrice, batchNo, unitNo, expiryDate, minStock, optimalStock);
                    break;

                case 2:
                    System.out.println("\n--- CHECK STOCK AVAILABILITY & APPLY DISCOUNTS ---");
                    System.out.print("Enter Item ID to check: ");
                    String searchId = sc.nextLine();

                    // checkAvailability() now also triggers low/overstock
                    // supplier email alerts internally.
                    int currentCount = inventoryManager.checkAvailability(searchId);
                    System.out.println( currentCount+" units found in stockroom ");

                    Item_new targetItem = inventoryManager.getItemById(searchId);

                    if (targetItem != null) {
                        if (currentCount > targetItem.getMinStock() && currentCount <= targetItem.getOptimalStock()) {
                            System.out.println("\n=============================================");
                            System.out.println("Product Name:     " + targetItem.getName());
                            System.out.println("Current Stock:    " + currentCount + " units");
                            System.out.println("Expiry Date:      " + targetItem.getExpiryDate());
                            System.out.println("Selling Price:    LKR " + targetItem.getSellingPrice());
                            System.out.println("Wholesale Floor:  LKR " + targetItem.getPurchasingPrice());
                            System.out.println("=============================================");

                            System.out.print("\nIs this item selling slowly? Apply a discount? (Y/N): ");
                            String response = sc.nextLine().trim();

                            if (response.equalsIgnoreCase("Y")) {
                                System.out.print("Discount mode - (M)anual entry or (A)uto-suggested: ");
                                String mode = sc.nextLine().trim();

                                if (mode.equalsIgnoreCase("A")) {
                                    discountService.applyAutoDiscount(
                                            inventoryManager.getStockroom(),
                                            inventoryManager.getCurrentItemCount(),
                                            searchId,
                                            staffIdentity
                                    );
                                } else {
                                    System.out.print("Enter Discount Percentage (e.g., 10 for 10%): ");
                                    double percentInput = sc.nextDouble();
                                    sc.nextLine();

                                    discountService.applyDiscount(
                                            inventoryManager.getStockroom(),
                                            inventoryManager.getCurrentItemCount(),
                                            searchId,
                                            percentInput,
                                            staffIdentity
                                    );
                                }
                            } else {
                                System.out.println("Skipping Discounts.");
                            }
                        } else {
                            System.out.println("Item stock count does not fall within stagnation audit bounds.");
                        }
                    } else {
                        System.out.println("Error: Item ID [" + searchId + "] not found in records.");
                    }
                    break;

                case 3:
                    System.out.println("\n--- SELL ITEMS (CASHIER CHECKOUT) ---");
                    System.out.print("Enter Item ID: ");
                    String sellItemId = sc.nextLine();
                    System.out.print("Enter Quantity Sold: ");
                    int quantitySold = sc.nextInt();
                    sc.nextLine();

                    inventoryManager.sellItem(sellItemId, quantitySold, staffIdentity);
                    break;

                case 4:
                    System.out.println("\n--- RUNNING EXPIRY ALERTS ---");
                    inventoryManager.checkExpirationAlerts();
                    break;

                case 5:
                    System.out.println("\n--- UPDATE ITEM PRICE ---");
                    System.out.print("Enter Item ID: ");
                    String priceItemId = sc.nextLine();
                    System.out.print("Enter New Selling Price: ");
                    double newPrice = sc.nextDouble();
                    sc.nextLine();

                    inventoryManager.updateItemPrice(priceItemId, newPrice, staffIdentity);
                    break;

                case 6:
                    System.out.println("\n--- ADD NEW SUPPLIER ---");
                    System.out.print("Enter Supplier ID: ");
                    String supplierId = sc.nextLine();

                    System.out.print("Enter Supplier Name: ");
                    String supplierName = sc.nextLine();

                    System.out.print("Enter Category Handled (A/B/C): ");
                    String supplierCategory = sc.nextLine();

                    System.out.print("Enter Contact Email: ");
                    String supplierEmail = sc.nextLine();

                    inventoryManager.getSupplierManagement().addSupplier(supplierId, supplierName, supplierCategory, supplierEmail);
                    break;

                case 7:
                    System.out.println("\n--- LOW-STOCK / OVERSTOCK DASHBOARD ---");
                    inventoryManager.printLowStockDashboard();
                    break;

                case 8:
                    if (loggedIn.getStaffId().equalsIgnoreCase(MANAGER_STAFF_ID)) {
                        System.out.println("\n--- GENERATING END-OF-MONTH REPORT ---");
                        reportGenerator.generateMonthlyReport(inventoryManager.getStockroom(), inventoryManager.getCurrentItemCount());
                    } else {
                        System.out.println("\nACCESS DENIED: Only the Manager (Staff ID " + MANAGER_STAFF_ID
                                + ") can generate the end-of-month report.");
                    }
                    break;

                case 9:
                    System.out.println("Logging out...");
                    break;

                default:
                    System.out.println("Invalid selection! Please choose a number between 1 and 9.");
            }
        } while (choice != 9);

        sc.close();
    }
}
