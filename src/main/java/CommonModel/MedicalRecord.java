package CommonModel;

public class MedicalRecord {
    public int idrecords;
    public int patientID;
    public String patientName;
    public int age;

    public String subj;        // subjective
    public String cc;          // chief complaint
    public String hpi;
    public String onset;
    public String location;
    public String duration;
    public String charac;      // characteristics
    public String factors;
    public String radiation;
    public String severity;

    public String pmi;
    public String med;         // medications
    public String allergies;
    public String fh;          // family history
    public String sh;          // social history
    public String ros;

    public int doctorID;       // ADDED

    // Existing getters
    public int getAge() {
        return age;
    }

    public int getIdrecords() {
        return idrecords;
    }

    public int getPatientID() {
        return patientID;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getSubj() {
        return subj;
    }

    public String getCc() {
        return cc;
    }

    public String getHpi() {
        return hpi;
    }

    public String getOnset() {
        return onset;
    }

    public String getLocation() {
        return location;
    }

    public String getDuration() {
        return duration;
    }

    public String getCharacter() {
        return charac;
    }

    public String getFactors() {
        return factors;
    }

    public String getRadiation() {
        return radiation;
    }

    public String getSeverity() {
        return severity;
    }

    public String getPmi() {
        return pmi;
    }

    public String getMed() {
        return med;
    }

    public String getAllergies() {
        return allergies;
    }

    public String getFh() {
        return fh;
    }

    public String getSh() {
        return sh;
    }

    public String getRos() {
        return ros;
    }

    // ADDED
    public int getDoctorID() {
        return doctorID;
    }

    // Existing setters
    public void setAge(int age) {
        this.age = age;
    }

    public void setIdrecords(int idrecords) {
        this.idrecords = idrecords;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setSubj(String subject) {
        this.subj = subject;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public void setHpi(String hpi) {
        this.hpi = hpi;
    }

    public void setOnset(String onset) {
        this.onset = onset;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setCharacter(String character) {
        this.charac = character;
    }

    public void setFactors(String factors) {
        this.factors = factors;
    }

    public void setRadiation(String radiation) {
        this.radiation = radiation;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setPmi(String pmi) {
        this.pmi = pmi;
    }

    public void setMed(String med) {
        this.med = med;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public void setFh(String fh) {
        this.fh = fh;
    }

    public void setSh(String sh) {
        this.sh = sh;
    }

    public void setRos(String ros) {
        this.ros = ros;
    }

    // ADDED
    public void setDoctorID(int doctorID) {
        this.doctorID = doctorID;
    }
}