package com.mycompany.sis;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupabaseService {

    // ─── LOGIN ───────────────────────────────────────────────────────────────
    public static String login(String username, String password) {
        String query = "SELECT role FROM login WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("role");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ─── GET ALL USERS (for admin roles tab) ─────────────────────────────────
    public static List<String[]> getAllUsers() {
        List<String[]> list = new ArrayList<>();
        String query = "SELECT username, role FROM login ORDER BY username";
        try (Connection conn = DBConnection.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new String[]{ rs.getString("username"), rs.getString("role") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ─── SET USER ROLE (master admin only) ───────────────────────────────────
    public static boolean setUserRole(String username, String newRole) {
        if ("admin".equalsIgnoreCase(username)) return false;
        String query = "UPDATE login SET role = ? WHERE username = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newRole);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ─── GET STUDENTS ────────────────────────────────────────────────────────
    // Returns [student_id, full_name, gender, email, phone_no,
    //          year_level, section, academic_status, is_deleted, course_code, course_name]
    public static List<String[]> getStudents() {
        String query = """
            SELECT
                p.student_id,
                p.full_name,
                p.gender,
                c.email,
                c.phone_no,
                a.year_level,
                a.section,
                a.academic_status,
                CAST(a.is_deleted AS TEXT) AS is_deleted,
                co.course_code,
                co.course_name
            FROM personal_info p
            JOIN contact_info  c  ON p.student_id = c.student_id
            JOIN academic_info a  ON p.student_id = a.student_id
            LEFT JOIN courses  co ON a.course_id  = co.id
        """;
        List<String[]> rows = new ArrayList<>();
        try (Connection conn = DBConnection.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(query)) {
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString("student_id"),       // [0]
                    rs.getString("full_name"),         // [1]
                    rs.getString("gender"),            // [2]
                    rs.getString("email"),             // [3]
                    rs.getString("phone_no"),          // [4]
                    rs.getString("year_level"),        // [5]
                    rs.getString("section"),           // [6]
                    rs.getString("academic_status"),   // [7]
                    rs.getString("is_deleted"),        // [8]
                    rs.getString("course_code"),       // [9]
                    rs.getString("course_name")        // [10]
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return rows;
    }

    // ─── GET STUDENT COURSE ID (for filtering subjects by course) ────────────
    // Returns the course_id for a given student username, or -1 if not found.
    public static int getCourseIdForUser(String username) {
        String query = """
            SELECT a.course_id
            FROM login l
            JOIN academic_info a ON l.student_id = a.student_id
            WHERE l.username = ?
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("course_id");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    // ─── GET SUBJECTS (all — used by admin) ───────────────────────────────────
    public static List<String[]> getSubjects() {
        List<String[]> list = new ArrayList<>();
        String query = "SELECT subject_code, subject_name, units FROM subjects ORDER BY subject_name";
        try (Connection conn = DBConnection.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("subject_code"),
                    rs.getString("subject_name"),
                    rs.getString("units")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ─── GET SUBJECTS BY COURSE (used by student view) ────────────────────────
    // Returns subjects linked to a specific course via course_subjects junction table.
    public static List<String[]> getSubjectsByCourse(int courseId) {
        List<String[]> list = new ArrayList<>();
        String query = """
            SELECT s.subject_code, s.subject_name, s.units
            FROM subjects s
            JOIN course_subjects cs ON s.id = cs.subject_id
            WHERE cs.course_id = ?
            ORDER BY s.subject_code
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        rs.getString("subject_code"),
                        rs.getString("subject_name"),
                        rs.getString("units")
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ─── ENROLL STUDENT ──────────────────────────────────────────────────────
    public static boolean enrollStudent(
            String id, String name, String gender,
            String address, String email, String phone, String emergency,
            int courseId, String year, String section,
            String status, String scholarship) {
        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(
                    "INSERT INTO personal_info(student_id,full_name,gender) VALUES(?,?,?)")) {
                p1.setString(1, id); p1.setString(2, name); p1.setString(3, gender);
                p1.executeUpdate();
            }
            try (PreparedStatement p2 = conn.prepareStatement(
                    "INSERT INTO contact_info(student_id,home_address,email,phone_no,emergency_contact) VALUES(?,?,?,?,?)")) {
                p2.setString(1, id); p2.setString(2, address); p2.setString(3, email);
                p2.setString(4, phone); p2.setString(5, emergency);
                p2.executeUpdate();
            }
            try (PreparedStatement p3 = conn.prepareStatement(
                    "INSERT INTO academic_info(student_id,course_id,year_level,section,academic_status,scholarship_status,is_deleted) VALUES(?,?,?,?,?,?,false)")) {
                p3.setString(1, id); p3.setInt(2, courseId); p3.setString(3, year);
                p3.setString(4, section); p3.setString(5, status); p3.setString(6, scholarship);
                p3.executeUpdate();
            }
            String firstName = name.contains(" ")
                ? name.substring(0, name.indexOf(" "))
                : name;
            int    currentYear = java.time.Year.now().getValue();
            String username    = firstName + currentYear;
            String password    = firstName + "123";

            try (PreparedStatement p4 = conn.prepareStatement(
                    "INSERT INTO login(username,password,role,student_id) VALUES(?,?,'student',?)")) {
                p4.setString(1, username);
                p4.setString(2, password);
                p4.setString(3, id);
                p4.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Login created — username: " + username + "  password: " + password);
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ─── SOFT DELETE ─────────────────────────────────────────────────────────
    public static void dropStudent(String studentId) {
        String query = "UPDATE academic_info SET is_deleted=true, deleted_at=NOW() WHERE student_id=?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, studentId); stmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── RESTORE (within 30 days) ────────────────────────────────────────────
    public static void restoreStudent(String studentId) {
        String query = """
            UPDATE academic_info
               SET is_deleted=false, deleted_at=NULL
             WHERE student_id=?
               AND deleted_at >= NOW() - INTERVAL '30 days'
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, studentId); stmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── GET COURSES ─────────────────────────────────────────────────────────
    public static List<String[]> getCourses() {
        List<String[]> courses = new ArrayList<>();
        String query = "SELECT id, course_code, course_name FROM courses ORDER BY course_name";
        try (Connection conn = DBConnection.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(query)) {
            while (rs.next()) {
                courses.add(new String[]{
                    rs.getString("id"), rs.getString("course_code"), rs.getString("course_name")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return courses;
    }

    // ─── GET ANNOUNCEMENTS ───────────────────────────────────────────────────
    public static List<String[]> getAnnouncements() {
        List<String[]> list = new ArrayList<>();
        String query = "SELECT title, content, posted_at FROM announcements ORDER BY posted_at DESC";
        try (Connection conn = DBConnection.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("title"), rs.getString("content"), rs.getString("posted_at")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ─── POST ANNOUNCEMENT ───────────────────────────────────────────────────
    public static boolean postAnnouncement(String title, String content) {
        String query = "INSERT INTO announcements(title,content,posted_at) VALUES(?,?,NOW())";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, title); stmt.setString(2, content);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ─── HARD DELETE (permanent) ─────────────────────────────────────────────
    public static boolean deleteStudentPermanently(String studentId) {
        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement d0 = conn.prepareStatement(
                    "DELETE FROM login WHERE student_id = ?")) {
                d0.setString(1, studentId);
                d0.executeUpdate();
            }
            try (PreparedStatement d1 = conn.prepareStatement(
                    "DELETE FROM academic_info WHERE student_id = ?")) {
                d1.setString(1, studentId);
                d1.executeUpdate();
            }
            try (PreparedStatement d2 = conn.prepareStatement(
                    "DELETE FROM contact_info WHERE student_id = ?")) {
                d2.setString(1, studentId);
                d2.executeUpdate();
            }
            try (PreparedStatement d3 = conn.prepareStatement(
                    "DELETE FROM personal_info WHERE student_id = ?")) {
                d3.setString(1, studentId);
                d3.executeUpdate();
            }
            conn.commit();
            System.out.println("🗑 Permanently deleted student: " + studentId);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Permanent delete failed for " + studentId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}