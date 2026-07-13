package services;
import models.Staff;

public class Login {
    public Staff[] staffList;
    private int currentStaffCount;

    public Login() {
        this.staffList = new Staff[10];
        this.currentStaffCount = 0;
        initializeStaff(); // Load authorized users
    }


    private void initializeStaff() {
        staffList[0] = new Staff("EMP-01", "Anuhas", "Manager", "1234");
        staffList[1] = new Staff("EMP-02", "Kasun", "Cashier", "5678");
        this.currentStaffCount = 2;
    }

    public Staff loginVerify(String staffId, String pin) {
        for (int i = 0; i < currentStaffCount; i++) {
            Staff employee = staffList[i];

            if (employee.getStaffId().equals(staffId) && employee.getPin().equals(pin)) {
                return employee; // Access granted!
            }

        }
            return null; // Access denied
        }


}

