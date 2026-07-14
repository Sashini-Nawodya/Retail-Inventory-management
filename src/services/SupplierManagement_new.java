package services;

import models.Supplier_new;

public class SupplierManagement_new {
    private Supplier_new[] suppliers;
    private int currentSupplierCount;

    public SupplierManagement_new() {
        this.suppliers = new Supplier_new[10];
        this.currentSupplierCount = 0;
        initializeSuppliers();
    }

    // Seed data mirrors the category structure already used in Management_new's
    // initializeInventory() (A = Dairy, B = Biscuits, C = Rice).
    private void initializeSuppliers() {
        suppliers[0] = new Supplier_new("SUP-A01", "Kothmale Dairy Distributors", "A", "orders@kothmaledairy.lk");
        suppliers[1] = new Supplier_new("SUP-B01", "Maliban Biscuits (Pvt) Ltd", "B", "supply@maliban.lk");
        suppliers[2] = new Supplier_new("SUP-C01", "Araliya Rice Millers", "C", "orders@araliyarice.lk");
        this.currentSupplierCount = 3;
    }

    public Supplier_new getSupplierForCategory(String category) {
        for (int i = 0; i < currentSupplierCount; i++) {
            if (suppliers[i].getCategoryHandled().equalsIgnoreCase(category)) {
                return suppliers[i];
            }
        }
        return null;
    }

    // NEW: lets staff register a new supplier at runtime instead of only
    // relying on the hardcoded seed data.
    public boolean addSupplier(String supplierId, String supplierName, String categoryHandled, String contactEmail) {
        // Reject duplicate supplier IDs.
        for (int i = 0; i < currentSupplierCount; i++) {
            if (suppliers[i].getSupplierId().equalsIgnoreCase(supplierId)) {
                System.out.println("WARNING: Supplier REJECTED! Supplier ID [" + supplierId + "] already exists.");
                return false;
            }
        }

        if (currentSupplierCount >= suppliers.length) {
            System.out.println("WARNING: Supplier REJECTED! Supplier list is full (" + suppliers.length + " max).");
            return false;
        }

        Supplier_new existing = getSupplierForCategory(categoryHandled);
        if (existing != null) {
            System.out.println("NOTE: Category '" + categoryHandled + "' is already served by "
                    + existing.getSupplierName() + ". This new supplier will be added, but restock/overstock "
                    + "alerts will keep going to " + existing.getSupplierName() + " (the first match for that category).");
        }

        suppliers[currentSupplierCount] = new Supplier_new(supplierId, supplierName, categoryHandled, contactEmail);
        currentSupplierCount++;
        System.out.println("Supplier added successfully! [" + supplierId + "] " + supplierName
                + " now registered for category '" + categoryHandled + "'.");
        return true;
    }

    public Supplier_new[] getSuppliers() {
        return this.suppliers;
    }

    public int getCurrentSupplierCount() {
        return this.currentSupplierCount;
    }
}
