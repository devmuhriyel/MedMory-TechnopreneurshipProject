package CommonModel;

public class UserRow {

    private int id;
    private String role; // PATIENT or DOCTOR
    private String name;
    private String status;

    // Full constructor
    public UserRow(int id, String name, String role, String status) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.status = status;
    }

    // Simple constructor for patients
    public UserRow(int patientID, String patientName) {
        this.id = patientID;
        this.name = patientName;
        this.role = "PATIENT";
        this.status = "Active";
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
}
