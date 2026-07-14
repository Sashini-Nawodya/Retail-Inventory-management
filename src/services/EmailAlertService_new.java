package services;

import models.Item_new;
import models.Supplier_new;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * IMPORTANT (read this before grading/demo):
 * This class SIMULATES sending an email. Real SMTP delivery needs mail-server
 * credentials and outbound network access that this environment does not have
 * (and a coursework submission shouldn't hardcode real credentials anyway).
 * Every "sent" alert is printed to the console AND appended to
 * email_alerts_log.txt so you have a durable, demonstrable record of every
 * alert the system generated.
 *
 * To wire this up to a real mailbox later, swap the body of dispatch() for a
 * javax.mail / Jakarta Mail Transport.send(...) call using an SMTP host,
 * port, and authenticated sender account.
 */
public class EmailAlertService_new {
    private static final String LOG_FILE = "email_alerts_log.txt";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void sendLowStockAlert(Supplier_new supplier, Item_new item, int currentStock) {
        if (supplier == null) {
            System.out.println("[EMAIL ALERT SKIPPED] No supplier registered for category '"
                    + item.getCategory() + "'. Cannot notify anyone about low stock on " + item.getId());
            return;
        }
        String subject = "URGENT: Reorder Required - " + item.getName() + " (" + item.getId() + ")";
        String body = "Dear " + supplier.getSupplierName() + ",\n\n"
                + "Our records show stock for item [" + item.getId() + "] " + item.getName()
                + " has fallen to " + currentStock + " units, which is below our minimum threshold of "
                + item.getMinStock() + " units.\n"
                + "Please arrange a new delivery as soon as possible.\n\n"
                + "Regards,\nRetail Inventory Control System";
        dispatch(supplier.getContactEmail(), subject, body);
    }

    public void sendOverstockAlert(Supplier_new supplier, Item_new item, int currentStock) {
        if (supplier == null) {
            System.out.println("[EMAIL ALERT SKIPPED] No supplier registered for category '"
                    + item.getCategory() + "'. Cannot notify anyone about overstock on " + item.getId());
            return;
        }
        String subject = "NOTICE: Hold Future Deliveries - " + item.getName() + " (" + item.getId() + ")";
        String body = "Dear " + supplier.getSupplierName() + ",\n\n"
                + "Our records show stock for item [" + item.getId() + "] " + item.getName()
                + " has reached " + currentStock + " units, exceeding our optimal maximum of "
                + item.getOptimalStock() + " units.\n"
                + "Please HOLD further shipments of this item until we notify you otherwise.\n\n"
                + "Regards,\nRetail Inventory Control System";
        dispatch(supplier.getContactEmail(), subject, body);
    }

    private void dispatch(String toEmail, String subject, String body) {
        System.out.println("\n[EMAIL ALERT DISPATCHED]");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("---------------------------------------------");
        System.out.println(body);
        System.out.println("---------------------------------------------");

        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write("[" + LocalDateTime.now().format(TIMESTAMP) + "] To: " + toEmail + " | Subject: " + subject + "\n");
            fw.write(body + "\n");
            fw.write("===============================================\n");
        } catch (IOException e) {
            System.out.println("Warning: Could not write to email log file: " + e.getMessage());
        }
    }
}
