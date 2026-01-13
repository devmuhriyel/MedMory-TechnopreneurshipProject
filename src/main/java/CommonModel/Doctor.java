package CommonModel;
import CommonModel.*;


public class Doctor extends UserModel {

    private String specialization;
    private String licenseNumber;
    private int yearsOfExperience;
    private String phoneNumber;

    // Constructors
    public Doctor() {
        super();
    }

    public Doctor(int id, String username, String password, String email,
                  String specialization, String licenseNumber,
                  int yearsOfExperience, String phoneNumber) {

        super(id, username, password, email, "DOCTOR");
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.yearsOfExperience = yearsOfExperience;
        this.phoneNumber = phoneNumber;
    }

    // Getters & Setters
    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

