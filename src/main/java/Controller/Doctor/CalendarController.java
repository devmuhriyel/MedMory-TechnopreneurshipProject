package Controller.Doctor;

import MVP.Login;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarController {

    @FXML private Button accountButton;
    @FXML private Button records;
    @FXML private Button casesButton;
    @FXML private Button calendarButton;
    @FXML private Button notificationsButton;
    @FXML private Button settingsButton;

    @FXML private Label monthLabel;
    @FXML private GridPane calendarGrid;
    @FXML private VBox appointmentsListVBox;

    private YearMonth currentYearMonth;
    private LocalDate selectedDate;

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        updateCalendar();
        showAppointments(selectedDate);
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        monthLabel.setText(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + currentYearMonth.getYear());

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday=0

        int daysInMonth = currentYearMonth.lengthOfMonth();
        int row = 0;
        int col = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            Button dayButton = createDayButton(date);
            calendarGrid.add(dayButton, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private Button createDayButton(LocalDate date) {
        Button dayButton = new Button(String.valueOf(date.getDayOfMonth()));
        dayButton.setPrefSize(60, 60);

        // Default style
        dayButton.setStyle("-fx-background-color: transparent; -fx-font-weight: bold;");

        // Highlight today
        if (date.equals(LocalDate.now())) {
            dayButton.setStyle("-fx-background-color: #00BF63; -fx-text-fill: white; -fx-font-weight: bold;");
        }

        // Highlight selected date
        if (date.equals(selectedDate)) {
            dayButton.setStyle("-fx-background-color: #008060; -fx-text-fill: white; -fx-font-weight: bold;");
        }

        // Weekend style (optional)
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dayButton.setStyle(dayButton.getStyle() + " -fx-text-fill: #FF5555;");
        }

        // Hover effect
        dayButton.setOnMouseEntered(e -> dayButton.setStyle("-fx-background-color: #b0f0b0;"));
        dayButton.setOnMouseExited(e -> updateCalendar()); // refresh styles

        dayButton.setOnAction(e -> {
            selectedDate = date;
            updateCalendar();      // refresh highlight
            showAppointments(date); // show appointments for selected day
        });

        // Tooltip: show number of appointments
        int count = getAppointmentsForDate(date).size();
        if (count > 0) {
            Tooltip tooltip = new Tooltip(count + " appointment(s)");
            Tooltip.install(dayButton, tooltip);
        }

        return dayButton;
    }

    private void showAppointments(LocalDate date) {
        appointmentsListVBox.getChildren().clear();

        List<String> appointments = getAppointmentsForDate(date);
        if (appointments.isEmpty()) {
            Label lbl = new Label("No appointments for this day.");
            lbl.setStyle("-fx-padding: 10;");
            appointmentsListVBox.getChildren().add(lbl);
        } else {
            for (String app : appointments) {
                Label lbl = new Label(app);
                lbl.setStyle("-fx-background-color: #d0fae0; -fx-padding: 10; -fx-background-radius: 5;");
                appointmentsListVBox.getChildren().add(lbl);
            }
        }
    }

    private List<String> getAppointmentsForDate(LocalDate date) {
        List<String> list = new ArrayList<>();
        // Sample data
        if (date.equals(LocalDate.now())) {
            list.add("09:00 - Patient: John Doe");
            list.add("10:30 - Patient: Jane Smith");
        } else if (date.equals(LocalDate.now().plusDays(1))) {
            list.add("11:00 - Patient: Mike Brown");
        }
        return list;
    }

    // Month navigation
    @FXML private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    @FXML
    private void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String targetFxml = "";

        switch (clickedButton.getId()) {
            case "accountButton": targetFxml = "DoctorAccount.fxml"; break;
            case "casesButton":   targetFxml = "Patient.fxml"; break;
            case "calendarButton": targetFxml = "Calendar.fxml"; break;
            case "notificationsButton": targetFxml = "Chat.fxml"; break;
            case "settingsButton": targetFxml = "DoctorSettings.fxml"; break;
            default:
                System.out.println("Unknown button: " + clickedButton.getId());
                return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource(targetFxml));
            Scene scene = new Scene(fxmlLoader.load(), 1280, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
