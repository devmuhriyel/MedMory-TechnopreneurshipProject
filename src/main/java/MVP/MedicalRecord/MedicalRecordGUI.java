package MVP.MedicalRecord;

import CommonModel.MedicalRecord;

import javax.swing.*;
import java.awt.*;

public class MedicalRecordGUI {

    private MedicalRecord record; // holds the DB data

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MedicalRecordGUI().createUI());
    }

    public MedicalRecordGUI() {
        // Retrieve medical record for userID = 1
        RetrieveRecord rr = new RetrieveRecord();
        record = rr.getRecordByUserID(1);

        if (record == null) {
            JOptionPane.showMessageDialog(null,
                    "No medical record found for the user.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void createUI() {
        JFrame frame = new JFrame("Medical Record");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // ---------------- LEFT SIDEBAR ----------------
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(0, 170, 90));
        sidebar.setPreferredSize(new Dimension(80, 0));
        sidebar.setLayout(new GridLayout(6, 1, 0, 15));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        sidebar.add(makeIconButton("Home"));
        sidebar.add(makeIconButton("Profile"));
        sidebar.add(makeIconButton("Records"));
        sidebar.add(makeIconButton("Calendar"));
        sidebar.add(makeIconButton("Bell"));
        sidebar.add(makeIconButton("Settings"));

        // ---------------- TOP BAR ----------------
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 245, 245));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        topBar.setPreferredSize(new Dimension(0, 60));

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 24));
        backBtn.setBackground(Color.WHITE);
        backBtn.setBorder(BorderFactory.createEmptyBorder());
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel avatarPanel = new JPanel();
        avatarPanel.setBackground(Color.WHITE);
        avatarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));
        JLabel avatar = new JLabel();
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(200, 200, 200));
        avatarPanel.add(avatar);

        topBar.add(backBtn, BorderLayout.WEST);
        topBar.add(avatarPanel, BorderLayout.EAST);

        // ---------------- CONTENT SECTIONS ----------------
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(240, 245, 245));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = createSection("Patient Details", null);

        // Patient Info Row (Dynamic)
        JPanel infoRow = new JPanel(new GridLayout(1, 3, 10, 0));
        infoRow.setBackground(Color.WHITE);
        infoRow.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoRow.add(createLabelPanel("ID: " + record.getPatientID()));
        infoRow.add(createLabelPanel("Name: " + record.getPatientName()));
        infoRow.add(createLabelPanel("Age: " + record.getAge()));

        // Dynamic fields from DB
        JPanel subjPanel = createSection("Subj:", record.getSubj());
        JPanel ccPanel = createSection("CC:", record.getCc());
        JPanel hpiPanel = createSection("HPI:", record.getHpi());

        // HPI Grid
        JPanel hpiGrid = new JPanel(new GridLayout(1, 4, 1, 0));
        hpiGrid.setBackground(Color.WHITE);
        hpiGrid.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        hpiGrid.add(createGridCell("Onset: " + record.getOnset()));
        hpiGrid.add(createGridCell("Location: " + record.getLocation()));
        hpiGrid.add(createGridCell("Duration: " + record.getDuration()));
        hpiGrid.add(createGridCell("Character: " + record.getCharacter()));

        // Aggravating, Radiation, Severity
        JPanel factorsPanel = new JPanel(new GridLayout(1, 3, 1, 0));
        factorsPanel.setBackground(Color.WHITE);
        factorsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        factorsPanel.add(createGridCell("Factors: " + record.getFactors()));
        factorsPanel.add(createGridCell("Radiation: " + record.getRadiation()));
        factorsPanel.add(createGridCell("Severity: " + record.getSeverity()));

        // PMI, Meds, Allergies
        JPanel pmiPanel = new JPanel(new GridLayout(1, 3, 1, 0));
        pmiPanel.setBackground(Color.WHITE);
        pmiPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        pmiPanel.add(createGridCell("PMI: " + record.getPmi()));
        pmiPanel.add(createGridCell("Medications: " + record.getMed()));
        pmiPanel.add(createGridCell("Allergies: " + record.getAllergies()));

        // FH, SH, ROS
        JPanel fhshPanel = new JPanel(new GridLayout(1, 3, 1, 0));
        fhshPanel.setBackground(Color.WHITE);
        fhshPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        fhshPanel.add(createGridCell("FH: " + record.getFh()));
        fhshPanel.add(createGridCell("SH: " + record.getSh()));
        fhshPanel.add(createGridCell("ROS: " + record.getRos()));

        // Add all to content
        contentPanel.add(headerPanel);
        contentPanel.add(infoRow);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(subjPanel);
        contentPanel.add(ccPanel);
        contentPanel.add(hpiPanel);
        contentPanel.add(hpiGrid);
        contentPanel.add(factorsPanel);
        contentPanel.add(pmiPanel);
        contentPanel.add(fhshPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JPanel createSection(String title, String content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(label, BorderLayout.NORTH);

        if (content != null) {
            JTextArea area = new JTextArea(content);
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setBackground(Color.WHITE);
            area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            panel.add(area, BorderLayout.CENTER);
        }

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        return panel;
    }

    private JPanel createLabelPanel(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        p.add(new JLabel(text), BorderLayout.CENTER);
        return p;
    }

    private JPanel createGridCell(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(Color.WHITE);
        p.add(area, BorderLayout.CENTER);
        return p;
    }

    private JButton makeIconButton(String text) {
        JButton b = new JButton(text.substring(0, 1));
        b.setBackground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}
