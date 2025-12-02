package MVP.Client;

public class ClientMedicalRecord {

    public int id;
    public int patientID;
    public String name;
    public String age;

    public String subj;
    public String cc;
    public String hpi;
    public String onset;
    public String location;
    public String duration;
    public String character;
    public String factors;
    public String radiation;
    public String severity;
    public String pmi;
    public String med;
    public String allergies;
    public String fh;
    public String sh;
    public String ros;

    public ClientMedicalRecord(int id, int patientID, String name, String age, String subj, String cc,
                               String hpi, String onset, String location, String duration, String character,
                               String factors, String radiation, String severity, String pmi, String med,
                               String allergies, String fh, String sh, String ros) {

        this.id = id;
        this.patientID = patientID;
        this.name = name;
        this.age = age;

        this.subj = subj;
        this.cc = cc;
        this.hpi = hpi;
        this.onset = onset;
        this.location = location;
        this.duration = duration;
        this.character = character;
        this.factors = factors;
        this.radiation = radiation;
        this.severity = severity;
        this.pmi = pmi;
        this.med = med;
        this.allergies = allergies;
        this.fh = fh;
        this.sh = sh;
        this.ros = ros;
    }
}
