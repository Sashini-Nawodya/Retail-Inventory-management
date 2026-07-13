package services;

import models.Item;

public class Discount {
    public void applyDiscount(Item[] stockroom, int currentItemCount, String itemId, double percent) {
        boolean itemFound = false;

        for (int i = 0; i < currentItemCount; i++) {
            Item item = stockroom[i];

            if (item != null && item.getId().equalsIgnoreCase(itemId)) {
                itemFound = true;
                double expectedPrice = item.getSellingPrice() * (1.0 - (percent / 100.0));

                if (expectedPrice < item.getPurchasingPrice()) {
                    System.out.println("\nERROR: Discount Denied! Financial Loss Risk.");
                    System.out.println("   Proposed Price LKR " + expectedPrice + " drops below Wholesale Floor LKR " + item.getPurchasingPrice());
                    return;
                }

                item.setDiscountPercent(percent);
                System.out.println("\nDISCOUNT APPLIED SUCCESSFULLY!");
                System.out.println("New Price: LKR " + item.getFinalSellingPrice());
                break;
            }
        }
    }
}





