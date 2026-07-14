package services;

import models.Item_new;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Discount_new {

    private final Persistence_new persistence = new Persistence_new();
    private final PriceAuditLog_new priceAuditLog = new PriceAuditLog_new();

    // ---------------------------------------------------------------
    // FEATURE 4: dynamic discount calculation.
    // Evaluates three conditions and adds them together:
    //   1) how close the item is to its expiry date
    //   2) its category (perishables get a higher baseline than shelf-stable goods)
    //   3) whether "today" falls inside a Sri Lankan festive/promotional season
    // ---------------------------------------------------------------
    public double calculateDynamicDiscount(Item_new item) {
        double discount = 0.0;

        long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(), item.getExpiryDate());

        if (daysToExpiry < 0) {
            // Already expired: flag it, but do not compute a "discount" on unsellable stock.
            return -1.0;
        } else if (daysToExpiry <= 7) {
            discount += 20.0;   // urgent clearance
        } else if (daysToExpiry <= 14) {
            discount += 10.0;   // approaching clearance window
        }

        discount += getCategoryBaseline(item.getCategory());
        discount += getSeasonalBonus(LocalDate.now());

        // Cap so a discount can never accidentally wipe out the entire margin.
        if (discount > 50.0) {
            discount = 50.0;
        }
        return discount;
    }

    // Perishable dairy (A) needs to move faster than biscuits (B) or rice (C),
    // so it gets a slightly higher baseline discount.
    private double getCategoryBaseline(String category) {
        if (category == null) return 0.0;
        switch (category.toUpperCase()) {
            case "A": return 5.0;
            case "B": return 3.0;
            case "C": return 1.0;
            default:  return 0.0;
        }
    }

    // Sri Lankan promotional seasons: Sinhala & Tamil New Year (mid-April),
    // Vesak (May), and the Christmas/New Year shopping season (late December).
    private double getSeasonalBonus(LocalDate today) {
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        boolean sinhalaTamilNewYear = (month == 4 && day >= 10 && day <= 16);
        boolean vesakSeason = (month == 5);
        boolean christmasSeason = (month == 12 && day >= 20 && day <= 31);

        if (sinhalaTamilNewYear || christmasSeason) return 10.0;
        if (vesakSeason) return 5.0;
        return 0.0;
    }

    // ---------------------------------------------------------------
    // Manual discount entry (kept from Version A) — a staff member types in
    // an explicit percentage. Still protected by the wholesale-floor check.
    // changedBy identifies the logged-in staff member for the audit trail.
    // ---------------------------------------------------------------
    public void applyDiscount(Item_new[] stockroom, int currentItemCount, String itemId, double percent, String changedBy) {
        applyDiscountInternal(stockroom, currentItemCount, itemId, percent, "MANUAL DISCOUNT", changedBy);
    }

    // ---------------------------------------------------------------
    // FEATURE 4 continued: auto-suggested discount, computed from
    // calculateDynamicDiscount() instead of typed in by a human.
    // ---------------------------------------------------------------
    public void applyAutoDiscount(Item_new[] stockroom, int currentItemCount, String itemId, String changedBy) {
        Item_new item = findItem(stockroom, currentItemCount, itemId);
        if (item == null) {
            System.out.println("Error: Item ID [" + itemId + "] not found in records.");
            return;
        }

        double suggested = calculateDynamicDiscount(item);
        if (suggested < 0) {
            System.out.println("This item expired on " + item.getExpiryDate() + " — remove it from sale instead of discounting it.");
            return;
        }

        System.out.println("Auto-calculated discount for [" + itemId + "]: " + suggested + "%");
        applyDiscountInternal(stockroom, currentItemCount, itemId, suggested, "AUTO DISCOUNT", changedBy);
    }

    private void applyDiscountInternal(Item_new[] stockroom, int currentItemCount, String itemId, double percent, String mode, String changedBy) {
        Item_new item = findItem(stockroom, currentItemCount, itemId);

        if (item == null) {
            System.out.println("Error: Item ID [" + itemId + "] not found in records.");
            return;
        }

        double expectedPrice = item.getSellingPrice() * (1.0 - (percent / 100.0));

        if (expectedPrice < item.getPurchasingPrice()) {
            System.out.println("\nERROR: Discount Denied! Financial Loss Risk.");
            System.out.println("   Proposed Price LKR " + expectedPrice + " drops below Wholesale Floor LKR " + item.getPurchasingPrice());
            return;
        }

        double oldFinalPrice = item.getFinalSellingPrice();
        item.setDiscountPercent(percent);
        double newFinalPrice = item.getFinalSellingPrice();

        System.out.println("\n" + mode + " APPLIED SUCCESSFULLY!");
        System.out.println("New Price: LKR " + newFinalPrice);

        // NEW: audit the effective price change (old -> new, who, when).
        priceAuditLog.logPriceChange(item.getId(), item.getName(), oldFinalPrice, newFinalPrice, changedBy, mode + " (" + percent + "%)");

        // FEATURE 1: a discount changes the effective selling price, so it
        // counts as a "price update" and must trigger the auto-save.
        persistence.saveInventory(stockroom, currentItemCount);
    }

    private Item_new findItem(Item_new[] stockroom, int currentItemCount, String itemId) {
        for (int i = 0; i < currentItemCount; i++) {
            Item_new item = stockroom[i];
            if (item != null && item.getId().equalsIgnoreCase(itemId)) {
                return item;
            }
        }
        return null;
    }
}
