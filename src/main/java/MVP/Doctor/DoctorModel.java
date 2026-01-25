package MVP.Doctor;

public class DoctorModel {

    public int id;
    public String name;
    public String specialization;
    public String email;
    public String phone;
    public String birthday;
    public String address;
    public String password;
    public String biography;

    public DoctorModel() {}

    public DoctorModel(
            int id,
            String name,
            String specialization,
            String email,
            String phone,
            String password,
            String birthday,
            String address,
            String biography
    ) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.birthday = birthday;
        this.address = address;
        this.biography = biography;
    }
}
