package services;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logs every price change (manual price updates AND discount-driven price
 * changes) so there's a durable record of old price -> new price, who made
 * the change, and when. Mirrors the pattern used by EmailAlertService_new:
 * console output + an append-only text log file.
 */
public class PriceAuditLog_new {
    private static final String LOG_FILE = "price_audit_log.txt";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void logPriceChange(String itemId, String itemName, double oldPrice, double newPrice,
                                String changedBy, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP);
        String line = String.format(
                "[%s] Item: %-8s %-30s | LKR %10.2f -> LKR %10.2f | By: %-20s | Reason: %s",
                timestamp, itemId, itemName, oldPrice, newPrice, changedBy, reason
        );

        System.out.println("[PRICE AUDIT] " + line);

        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(line + "\n");
        } catch (IOException e) {
            System.out.println("Warning: Could not write to price audit log: " + e.getMessage());
        }
    }
}
