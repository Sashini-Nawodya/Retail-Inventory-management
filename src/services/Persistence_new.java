package services;

import models.Item_new;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Handles saving the stockroom to disk (CSV) and reloading it on the next
 * run, so data entered in one session is still there the next time the
 * program starts. save() is called automatically by Management_new whenever an
 * item is added or a price/discount changes.
 */
public class Persistence_new {
    private static final String DATA_FILE = "inventory_data.csv";
    private static final String HEADER = "ID,Name,Category,PurchasingPrice,SellingPrice,BatchNo,UnitNo,ExpiryDate,MinStock,OptimalStock,DiscountPercent";

    public void saveInventory(Item_new[] stockroom, int currentItemCount) {
        try (FileWriter fw = new FileWriter(DATA_FILE, false)) {
            fw.write(HEADER + "\n");
            for (int i = 0; i < currentItemCount; i++) {
                Item_new item = stockroom[i];
                if (item == null) continue;
                fw.write(String.join(",",
                        item.getId(),
                        item.getName(),
                        item.getCategory(),
                        String.valueOf(item.getPurchasingPrice()),
                        String.valueOf(item.getSellingPrice()),
                        String.valueOf(item.getBatchNo()),
                        String.valueOf(item.getUnitNo()),
                        item.getExpiryDate().toString(),
                        String.valueOf(item.getMinStock()),
                        String.valueOf(item.getOptimalStock()),
                        String.valueOf(item.getDiscountPercent())
                ));
                fw.write("\n");
            }
            System.out.println("[AUTO-SAVE] Inventory saved to " + DATA_FILE + " (" + LocalDateTime.now() + ")");
        } catch (IOException e) {
            System.out.println("Warning: Auto-save failed: " + e.getMessage());
        }
    }

    /**
     * Attempts to reload a previously saved stockroom. Returns null if no
     * save file exists yet (e.g. first ever run), so the caller can fall
     * back to seed/demo data.
     */
    public Item_new[] loadInventory(int capacity) {
        if (!Files.exists(Paths.get(DATA_FILE))) {
            return null;
        }

        Item_new[] loaded = new Item_new[capacity];
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 11) continue; // skip malformed rows rather than crash

                try {
                    String id = parts[0];
                    String name = parts[1];
                    String category = parts[2];
                    double purchasingPrice = Double.parseDouble(parts[3]);
                    double sellingPrice = Double.parseDouble(parts[4]);
                    int batchNo = Integer.parseInt(parts[5]);
                    int unitNo = Integer.parseInt(parts[6]);
                    LocalDate expiryDate = LocalDate.parse(parts[7]);
                    int minStock = Integer.parseInt(parts[8]);
                    int optimalStock = Integer.parseInt(parts[9]);
                    double discountPercent = Double.parseDouble(parts[10]);

                    Item_new item = new Item_new(id, name, category, purchasingPrice, sellingPrice,
                            batchNo, unitNo, expiryDate, minStock, optimalStock);
                    item.setDiscountPercent(discountPercent);

                    if (count < capacity) {
                        loaded[count] = item;
                        count++;
                    }
                } catch (NumberFormatException | java.time.format.DateTimeParseException parseError) {
                    System.out.println("Warning: skipped a corrupted row in " + DATA_FILE);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not read saved inventory (" + e.getMessage() + "). Starting with seed data instead.");
            return null;
        }

        this.lastLoadedCount = count;
        return loaded;
    }

    // Small helper so callers can find out how many rows were actually parsed.
    private int lastLoadedCount = 0;

    public int getLastLoadedCount() {
        return lastLoadedCount;
    }
}
