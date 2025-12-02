package MVP.Client;

public class ClientModel {
    public int id;
    public String name;
    public String email;
    public String phone;

    public ClientModel(int id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}
