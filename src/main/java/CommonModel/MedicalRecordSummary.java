package CommonModel;

public class MedicalRecordSummary {
    public int recordID;
    public int patientID;
    public String patientName;
    public String diagnosis;
    public String recordDate;

    public MedicalRecordSummary(int recordID, int patientID, String patientName, String diagnosis, String recordDate) {
        this.recordID = recordID;
        this.patientID = patientID;
        this.patientName = patientName;
        this.diagnosis = diagnosis;
        this.recordDate = recordDate;
    }
}
