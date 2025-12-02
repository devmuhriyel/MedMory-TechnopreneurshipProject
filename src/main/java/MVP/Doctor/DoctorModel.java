package MVP.Doctor;

public class DoctorModel {

    public String name;
    public String specialization;
    public String email;
    public String phone;
    public String bio;

    public DoctorModel(String name, String specialization, String email, String phone, String bio) {
        this.name = name;
        this.specialization = specialization;
        this.email = email;
        this.phone = phone;
        this.bio = bio;
    }
}
