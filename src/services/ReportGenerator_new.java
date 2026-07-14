package services;

import models.Item_new;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

// FEATURE 5: end-of-month reporting — writes a formatted .txt analysis of
// the whole stockroom (value, discounts, low/over-stock items, items about
// to expire) and also prints it to the console.
public class ReportGenerator_new {

    public void generateMonthlyReport(Item_new[] stockroom, int currentItemCount) {
        LocalDate today = LocalDate.now();
        String fileName = String.format("Monthly_Report_%d_%02d.txt", today.getYear(), today.getMonthValue());

        double totalInventoryCost = 0.0;
        double totalPotentialRevenue = 0.0;
        int lowStockCount = 0;
        int overstockCount = 0;
        int discountedCount = 0;
        int expiringSoonCount = 0;

        StringBuilder itemLines = new StringBuilder();

        for (int i = 0; i < currentItemCount; i++) {
            Item_new item = stockroom[i];
            if (item == null) continue;

            double lineCost = item.getPurchasingPrice() * item.getUnitNo();
            double lineRevenue = item.getFinalSellingPrice() * item.getUnitNo();
            totalInventoryCost += lineCost;
            totalPotentialRevenue += lineRevenue;

            if (item.getUnitNo() < item.getMinStock()) lowStockCount++;
            if (item.getUnitNo() > item.getOptimalStock()) overstockCount++;
            if (item.getDiscountPercent() > 0) discountedCount++;

            long daysToExpiry = ChronoUnit.DAYS.between(today, item.getExpiryDate());
            if (daysToExpiry >= 0 && daysToExpiry <= 7) expiringSoonCount++;

            itemLines.append(String.format("%-8s %-30s %-4s %7d  LKR %10.2f  %6.1f%%  %s%n",
                    item.getId(), truncate(item.getName(), 30), item.getCategory(), item.getUnitNo(),
                    item.getFinalSellingPrice(), item.getDiscountPercent(), item.getExpiryDate()));
        }

        StringBuilder report = new StringBuilder();
        report.append("=====================================================================\n");
        report.append("        RETAIL INVENTORY CONTROL SYSTEM - END OF MONTH REPORT\n");
        report.append("=====================================================================\n");
        report.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");

        report.append("SUMMARY\n");
        report.append("---------------------------------------------------------------------\n");
        report.append(String.format("Total Active Batches:            %d%n", currentItemCount));
        report.append(String.format("Total Inventory Value (cost):    LKR %.2f%n", totalInventoryCost));
        report.append(String.format("Total Potential Revenue:         LKR %.2f%n", totalPotentialRevenue));
        report.append(String.format("Projected Gross Margin:          LKR %.2f%n", (totalPotentialRevenue - totalInventoryCost)));
        report.append(String.format("Batches Below Minimum Stock:     %d%n", lowStockCount));
        report.append(String.format("Batches Above Optimal Stock:     %d%n", overstockCount));
        report.append(String.format("Batches Currently Discounted:    %d%n", discountedCount));
        report.append(String.format("Batches Expiring within 7 Days:  %d%n", expiringSoonCount));

        report.append("\nITEM DETAIL\n");
        report.append("---------------------------------------------------------------------\n");
        report.append(String.format("%-8s %-30s %-4s %7s  %14s  %7s  %s%n",
                "ID", "Name", "Cat", "Units", "Final Price", "Disc%", "Expiry"));
        report.append(itemLines);
        report.append("=====================================================================\n");

        try (FileWriter fw = new FileWriter(fileName, false)) {
            fw.write(report.toString());
            System.out.println("\nEnd-of-month report generated: " + fileName);
        } catch (IOException e) {
            System.out.println("Warning: Could not write report file: " + e.getMessage());
        }

        System.out.println("\n" + report);
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen - 1) + "\u2026";
    }
}
