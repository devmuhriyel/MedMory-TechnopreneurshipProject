package Controller.Admin;

/**
 * UserRow - Model class for displaying users in TableView
 * Represents a row in the user management table
 */
public class UserRow {
    private int id;
    private String name;
    private String role;
    private String status;
    private String email;
    private String phone;
    private String address;
    private String password;
    private String birthday;

    // Constructors
    public UserRow() {
    }

    public UserRow(int id, String name, String role, String status, String email,
                   String phone, String address, String password, String birthday) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.status = status;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.password = password;
        this.birthday = birthday;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "UserRow{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}