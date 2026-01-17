package CommonModel;

import java.util.ArrayList;
import java.util.List;

public class DoctorCase {

     public int caseID;
    public int patientID;
     public String patientName;
    public String treatment;
    public String diagnosis;
    public  String dateCompleted;
    public String caseDate;

    // Each case can have multiple attachments
    private List<Attachment> attachments = new ArrayList<>();

    // Getters & Setters
    public int getCaseID() { return caseID; }
    public void setCaseID(int caseID) { this.caseID = caseID; }

    public int getPatientID() { return patientID; }
    public void setPatientID(int patientID) { this.patientID = patientID; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getDateCompleted() { return dateCompleted; }
    public void setDateCompleted(String dateCompleted) { this.dateCompleted = dateCompleted; }

    public String getCaseDate() { return caseDate; }
    public void setCaseDate(String caseDate) { this.caseDate = caseDate; }

    public List<Attachment> getAttachments() { return attachments; }
}
