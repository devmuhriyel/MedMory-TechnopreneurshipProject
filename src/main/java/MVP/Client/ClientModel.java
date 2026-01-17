package MVP.Client;

public class ClientModel {
    // Basic Information
    public int id;
    public String name; // Used as First Name
    public String lastName;

    // Contact & Personal Details
    public String email;
    public String phoneNumber;
    public String birthday;
    public String location;

    // Default Constructor (Required for many DAO mapping styles)
    public ClientModel() {}

    // Minimal Constructor (for legacy code compatibility)
    public ClientModel(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Full Constructor (to initialize all fields at once)
    public ClientModel(int id, String name, String lastName, String email,
                       String phoneNumber, String birthday, String location) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.birthday = birthday;
        this.location = location;
    }

    // --- Optional: Getters and Setters ---
    // These are helpful if you prefer calling user.getFirstName()
    // instead of user.name directly.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}