package services;
import models.Staff_new;

public class Login_new {
    public Staff_new[] staffList;
    private int currentStaffCount;

    public Login_new() {
        this.staffList = new Staff_new[10];
        this.currentStaffCount = 0;
        initializeStaff(); // Load authorized users
    }


    private void initializeStaff() {
        staffList[0] = new Staff_new("EMP-01", "Anuhas", "Manager", "1234");
        staffList[1] = new Staff_new("EMP-02", "Kasun", "Cashier", "5678");
        this.currentStaffCount = 2;
    }

    public Staff_new loginVerify(String staffId, String pin) {
        for (int i = 0; i < currentStaffCount; i++) {
            Staff_new employee = staffList[i];

            if (employee.getStaffId().equals(staffId) && employee.getPin().equals(pin)) {
                return employee; // Access granted!
            }

        }
            return null; // Access denied
        }


}

