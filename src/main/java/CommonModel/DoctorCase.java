package CommonModel;

public class DoctorCase {
    public int caseID;
    public int patientID;
    public String patientName;
    public String diagnosis;
    public String treatment;
    public String dateCompleted;

    public int getPatientID() {
        return patientID;
    }
    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }
    public int getCaseID() {
        return caseID;
    }
    public void setCaseID(int caseID) {
        this.caseID = caseID;
    }
    public String getPatientName() {
        return patientName;
    }
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    public String getDiagnosis() {
        return diagnosis;
    }
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    public String getTreatment() {
        return treatment;
    }
    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }
    public String getDateCompleted() {
        return dateCompleted;
    }
    public void setDateCompleted(String dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

}

