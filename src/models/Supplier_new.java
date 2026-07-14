package models;

public class Supplier_new {
    private String supplierName;
    private String categoryServed;
    private String supplierId;
    private String contactEmail;


    public Supplier_new(String supplierId, String supplierName, String categoryHandled, String contactEmail) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.categoryServed = categoryHandled;
        this.contactEmail = contactEmail;
        }

        // 3. Getter methods for your business services to read this data
        public String getSupplierId() { return supplierId; }
        public String getSupplierName() { return supplierName; }
        public String getCategoryHandled() { return categoryServed; }
        public String getContactEmail() { return contactEmail; }
    }

