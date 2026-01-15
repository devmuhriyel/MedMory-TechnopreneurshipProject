package CommonModel;

public class DoctorCase {
    public int caseID;
    public int patientID;
    public String patientName;
    public String diagnosis;
    public String treatment;
    public String caseDate;      // when case started
    public String dateCompleted; // when case finished

    // Corrected constructor
    public DoctorCase(int caseID, int patientID, String patientName, String dateCompleted, String diagnosis, String treatment) {
        this.caseID = caseID;
        this.patientID = patientID;
        this.patientName = patientName;
        this.dateCompleted = dateCompleted;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
    }

    public DoctorCase() {}

    public int getCaseID() { return caseID; }
    public void setCaseID(int caseID) { this.caseID = caseID; }

    public int getPatientID() { return patientID; }
    public void setPatientID(int patientID) { this.patientID = patientID; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getCaseDate() { return caseDate; }
    public void setCaseDate(String caseDate) { this.caseDate = caseDate; }

    public String getDateCompleted() { return dateCompleted; }
    public void setDateCompleted(String dateCompleted) { this.dateCompleted = dateCompleted; }
}
