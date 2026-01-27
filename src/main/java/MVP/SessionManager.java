package MVP;

import java.util.HashMap;
import java.util.Map;

/**
 * SessionManager - Manages user sessions for the medical system
 * Stores active user sessions and provides methods to access session data
 */
public class SessionManager {

    // Session storage
    private static Integer currentUserId = null;
    private static Integer currentPatientId = null;
    private static String currentUserRole = null;
    private static String currentUserName = null;
    private static String currentUserEmail = null;

    // Additional session data storage
    private static Map<String, Object> sessionData = new HashMap<>();

    // ========================================
    // SESSION MANAGEMENT
    // ========================================

    /**
     * Set session for a doctor user
     * @param doctorId The doctor's user ID
     * @param patientId The patient ID (use -1 if not viewing a specific patient)
     */
    public static void setDoctorSession(int doctorId, int patientId) {
        currentUserId = doctorId;
        currentPatientId = patientId;
        currentUserRole = "DOCTOR";
        sessionData.clear();
    }

    /**
     * Set session for a doctor with full details
     */
    public static void setDoctorSession(int doctorId, int patientId, String name, String email) {
        currentUserId = doctorId;
        currentPatientId = patientId;
        currentUserRole = "DOCTOR";
        currentUserName = name;
        currentUserEmail = email;
        sessionData.clear();
    }

    /**
     * Set session for a patient user
     * @param patientId The patient's user ID
     */
    public static void setPatientSession(int patientId) {
        currentUserId = patientId;
        currentPatientId = patientId;
        currentUserRole = "PATIENT";
        sessionData.clear();
    }

    /**
     * Set session for a patient with full details
     */
    public static void setPatientSession(int patientId, String name, String email) {
        currentUserId = patientId;
        currentPatientId = patientId;
        currentUserRole = "PATIENT";
        currentUserName = name;
        currentUserEmail = email;
        sessionData.clear();
    }

    /**
     * Set session for a client (can be patient or admin)
     * @param userId The user ID
     */
    public static void setClientSession(int userId) {
        currentUserId = userId;
        currentPatientId = userId;
        currentUserRole = "CLIENT"; // Generic role
        sessionData.clear();
    }

    /**
     * Set session for an admin user
     */
    public static void setAdminSession(int adminId) {
        currentUserId = adminId;
        currentPatientId = -1;
        currentUserRole = "ADMIN";
        sessionData.clear();
    }

    /**
     * Set session for an admin with full details
     */
    public static void setAdminSession(int adminId, String name, String email) {
        currentUserId = adminId;
        currentPatientId = -1;
        currentUserRole = "ADMIN";
        currentUserName = name;
        currentUserEmail = email;
        sessionData.clear();
    }

    /**
     * Set session with complete user information
     */
    public static void setSession(int userId, String role, String name, String email) {
        currentUserId = userId;
        currentUserRole = role;
        currentUserName = name;
        currentUserEmail = email;

        // Set patientId based on role
        if ("PATIENT".equals(role)) {
            currentPatientId = userId;
        } else {
            currentPatientId = -1;
        }

        sessionData.clear();
    }

    /**
     * Update the current patient ID (useful for doctors viewing different patients)
     */
    public static void setCurrentPatientId(int patientId) {
        currentPatientId = patientId;
    }

    /**
     * Clear all session data (logout)
     */
    public static void clearSession() {
        currentUserId = null;
        currentPatientId = null;
        currentUserRole = null;
        currentUserName = null;
        currentUserEmail = null;
        sessionData.clear();
    }

    // ========================================
    // SESSION GETTERS
    // ========================================

    /**
     * Get the current logged-in user ID
     * @return User ID or null if not logged in
     */
    public static Integer getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Get the current patient ID being viewed
     * @return Patient ID or null if not set
     */
    public static Integer getCurrentPatientId() {
        return currentPatientId;
    }

    /**
     * Get the current user's role
     * @return Role (ADMIN, DOCTOR, PATIENT) or null if not logged in
     */
    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    /**
     * Get the current user's name
     * @return User name or null if not set
     */
    public static String getCurrentUserName() {
        return currentUserName;
    }

    /**
     * Get the current user's email
     * @return User email or null if not set
     */
    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }

    // ========================================
    // SESSION STATE CHECKS
    // ========================================

    /**
     * Check if a user is currently logged in
     * @return true if logged in, false otherwise
     */
    public static boolean isLoggedIn() {
        return currentUserId != null;
    }

    /**
     * Check if the current user is a doctor
     * @return true if doctor, false otherwise
     */
    public static boolean isDoctor() {
        return "DOCTOR".equals(currentUserRole);
    }

    /**
     * Check if the current user is a patient
     * @return true if patient, false otherwise
     */
    public static boolean isPatient() {
        return "PATIENT".equals(currentUserRole);
    }

    /**
     * Check if the current user is an admin
     * @return true if admin, false otherwise
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(currentUserRole);
    }

    /**
     * Check if a patient is currently selected (for doctors)
     * @return true if patient selected, false otherwise
     */
    public static boolean hasPatientSelected() {
        return currentPatientId != null && currentPatientId > 0;
    }

    // ========================================
    // ADDITIONAL SESSION DATA
    // ========================================

    /**
     * Store additional data in the session
     * @param key The key to store data under
     * @param value The value to store
     */
    public static void setSessionData(String key, Object value) {
        sessionData.put(key, value);
    }

    /**
     * Retrieve additional data from the session
     * @param key The key to retrieve
     * @return The stored value or null if not found
     */
    public static Object getSessionData(String key) {
        return sessionData.get(key);
    }

    /**
     * Remove specific data from the session
     * @param key The key to remove
     */
    public static void removeSessionData(String key) {
        sessionData.remove(key);
    }

    /**
     * Check if a key exists in session data
     * @param key The key to check
     * @return true if key exists, false otherwise
     */
    public static boolean hasSessionData(String key) {
        return sessionData.containsKey(key);
    }

    /**
     * Clear all additional session data (keeps user ID and role)
     */
    public static void clearSessionData() {
        sessionData.clear();
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Get a string representation of the current session
     * @return Session details as string
     */
    public static String getSessionInfo() {
        if (!isLoggedIn()) {
            return "No active session";
        }

        return String.format(
                "Session Info: UserId=%d, PatientId=%d, Role=%s, Name=%s, Email=%s",
                currentUserId,
                currentPatientId != null ? currentPatientId : -1,
                currentUserRole != null ? currentUserRole : "Unknown",
                currentUserName != null ? currentUserName : "Not set",
                currentUserEmail != null ? currentUserEmail : "Not set"
        );
    }

    /**
     * Validate that the current user has permission for an action
     * @param requiredRole The role required for the action
     * @return true if user has required role, false otherwise
     */
    public static boolean hasRole(String requiredRole) {
        if (!isLoggedIn()) {
            return false;
        }
        return requiredRole.equals(currentUserRole);
    }

    /**
     * Check if the current user can access a specific patient's data
     * @param patientId The patient ID to check access for
     * @return true if access allowed, false otherwise
     */
    public static boolean canAccessPatient(int patientId) {
        if (!isLoggedIn()) {
            return false;
        }

        // Admins can access all patients
        if (isAdmin()) {
            return true;
        }

        // Patients can only access their own data
        if (isPatient()) {
            return currentUserId != null && currentUserId == patientId;
        }

        // Doctors can access any patient (you might want to add assignment checking)
        if (isDoctor()) {
            return true;
        }

        return false;
    }

    /**
     * Set user name (useful when updating profile)
     */
    public static void setCurrentUserName(String name) {
        currentUserName = name;
    }

    /**
     * Set user email (useful when updating profile)
     */
    public static void setCurrentUserEmail(String email) {
        currentUserEmail = email;
    }

    // ========================================
    // DEBUG METHODS
    // ========================================

    /**
     * Print current session information (for debugging)
     */
    public static void printSessionInfo() {
        System.out.println("=== Session Information ===");
        System.out.println("User ID: " + currentUserId);
        System.out.println("Patient ID: " + currentPatientId);
        System.out.println("Role: " + currentUserRole);
        System.out.println("Name: " + currentUserName);
        System.out.println("Email: " + currentUserEmail);
        System.out.println("Logged In: " + isLoggedIn());
        System.out.println("Session Data: " + sessionData);
        System.out.println("===========================");
    }
}