package models;

public class Staff {
     private String staffId;
     private String name;
     private String role;      // e.g., "Manager" / "Cashier"
     private String pin;

     public Staff(String staffId, String name, String role, String pin) {
          this.staffId = staffId;
          this.name = name;
          this.role = role;
          this.pin =pin;
     }

     // 3. Getter methods
     public String getStaffId() { return staffId; }
     public String getName() { return name; }
     public String getRole() { return role; }
     public String getPin() { return pin; }
}



