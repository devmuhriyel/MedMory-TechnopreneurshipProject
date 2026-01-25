package CommonModel;

public class UserRow {

    private int id;
    private String role; // PATIENT or DOCTOR
    private String name;
    private String status;
    private String email;
    private String phone;
    private String address;
    private String password;
    private String bday;

    // Full constructor
    public UserRow(int id, String name, String role, String status, String email, String phone, String address, String password, String bday) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.status = status;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.password = password;
        this.bday = bday;
    }

    // Simple constructor for patients
    public UserRow(int patientID, String patientName) {
        this.id = patientID;
        this.name = patientName;
        this.role = "PATIENT";
        this.status = "Active";
    }


    public String getBday() { return bday; }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getPassword() { return password; }

}
