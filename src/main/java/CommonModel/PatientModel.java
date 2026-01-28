package CommonModel;

/**
 * PatientModel.java - Patient model extending user information
 * Maps to users table joined with patients table
 */
public class PatientModel {
    // From users table (via patient_id -> user_id)
    private int patientId;      // Same as user_id
    private String name;
    private String email;
    private String phoneNumber;
    private String birthday;
    private String passwordHash;
    private String createdAt;

    // Additional patient-specific fields
    private String location;
    private int age;

    // Constructors
    public PatientModel() {
    }

    public PatientModel(int patientId, String name, String email, String phoneNumber, String birthday) {
        this.patientId = patientId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.birthday = birthday;
    }

    // Full constructor
    public PatientModel(int patientId, String name, String email, String phoneNumber,
                        String birthday, String location, String createdAt) {
        this.patientId = patientId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.birthday = birthday;
        this.location = location;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Calculate age from birthday
     */
    public int calculateAge() {
        if (birthday == null || birthday.isEmpty()) {
            return 0;
        }
        try {
            java.time.LocalDate birthDate = java.time.LocalDate.parse(birthday);
            java.time.LocalDate currentDate = java.time.LocalDate.now();
            return java.time.Period.between(birthDate, currentDate).getYears();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "PatientModel{" +
                "patientId=" + patientId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", birthday='" + birthday + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}