package MVP.Doctor;

public class DoctorModel {

    public int id;
    public String name;
    public String specialization;
    public String email;
    public String phone;
    public String biography; // Match DB Column

    public DoctorModel() {
        // Default constructor required for DAO mapping
    }

    public DoctorModel(int id, String name, String specialization, String email, String phone, String biography) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.email = email;
        this.phone = phone;
        this.biography = biography;
    }
}
