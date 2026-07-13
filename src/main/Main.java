package main;

import java.util.Scanner;
import java.time.LocalDate;

import models.Item;
import models.Staff;
import services.Management;
import services.Login;
import services.Discount;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        Login loginStaff = new Login();
        Management inventoryManager = new Management();
        Discount discountService = new Discount();

        System.out.println("\nRETAIL INVENTORY CONTROL SYSTEM ");

        Staff loggedIn = null;

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

        int choice;
        do {
            System.out.println("\nEnter what task you want to do (1-4):");
            System.out.println("1. Add items");
            System.out.println("2. Check availability & Manage Discounts");
            System.out.println("3. Run Expiration Checks");
            System.out.println("4. Exit");
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
                    inventoryManager.addItem(id, name, category, pPrice, sPrice, batchNo, unitNo, expiryDate, minStock, optimalStock);
                    break;

                case 2:
                    System.out.println("\n--- CHECK STOCK AVAILABILITY & APPLY DISCOUNTS ---");
                    System.out.print("Enter Item ID to check: ");
                    String searchId = sc.nextLine();

                    int currentCount = inventoryManager.checkAvailability(searchId);
                    System.out.println( currentCount+" units found in stockroom ");

                    Item targetItem = inventoryManager.getItemById(searchId);

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
                                System.out.print("Enter Discount Percentage (e.g., 10 for 10%): ");
                                double percentInput = sc.nextDouble();
                                sc.nextLine();

                                discountService.applyDiscount(
                                        inventoryManager.getStockroom(),
                                        inventoryManager.getCurrentItemCount(),
                                        searchId,
                                        percentInput
                                );
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
                    System.out.println("\n--- RUNNING EXPIRY ALERTS ---");
                    inventoryManager.checkExpirationAlerts();
                    break;

                case 4:
                    System.out.println("Logging out...");
                    break;

                default:
                    System.out.println("Invalid selection! Please choose a number between 1 and 4.");
            }
        } while (choice != 4);
    }
}



