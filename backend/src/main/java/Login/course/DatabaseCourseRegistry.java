package Login.course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseCourseRegistry implements CourseRegistry {

    // Tabellen courses lages i database/migrations/003_kurs_og_pamelding.sql (S-010).
    private static final String SQL_BASE =
            "SELECT id, tittel, beskrivelse, lokallag, sted, dato, starttid, kapasitet, pris, nivaa "
                    + "FROM courses";

    private final String url;
    private final String dbUser;
    private final String dbPassword;

    public DatabaseCourseRegistry(String url, String dbUser, String dbPassword) {
        this.url = url;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    @Override
    public List<Course> findUpcomingCourses() {
        return findCourses(new CourseFilter(null, null, null, null, null, null, null, false));
    }

    // Bygger WHERE-delen ut fra hvilke filtre som er satt (null = ikke filtrer).
    // Alle verdier settes inn med ?, aldri limt rett inn i teksten.
    @Override
    public List<Course> findCourses(CourseFilter f) {
        List<String> where = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (f.lokallag() != null) { where.add("lokallag LIKE ?"); params.add(f.lokallag() + "%"); }
        if (f.sted() != null)     { where.add("sted LIKE ?");     params.add("%" + f.sted() + "%"); }
        if (f.nivaa() != null)    { where.add("nivaa = ?");       params.add(f.nivaa()); }
        if (f.fra() != null)      { where.add("dato >= ?");       params.add(Date.valueOf(f.fra())); }
        if (f.til() != null)      { where.add("dato <= ?");       params.add(Date.valueOf(f.til())); }
        if (f.prisMin() != null)  { where.add("pris >= ?");       params.add(f.prisMin()); }
        if (f.prisMax() != null)  { where.add("pris <= ?");       params.add(f.prisMax()); }
        if (!f.inkluderFerdige() && f.fra() == null) { where.add("dato >= CURDATE()"); }

        String sql = SQL_BASE
                + (where.isEmpty() ? "" : " WHERE " + String.join(" AND ", where))
                + " ORDER BY dato < CURDATE(), dato, starttid";

        List<Course> courses = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Date dato = rs.getDate("dato");
                Time starttid = rs.getTime("starttid");

                courses.add(new Course(
                        rs.getInt("id"),
                        rs.getString("tittel"),
                        rs.getString("beskrivelse"),
                        rs.getString("lokallag"),
                        rs.getString("sted"),
                        dato == null ? null : dato.toLocalDate().toString(),
                        starttid == null ? null : starttid.toLocalTime().toString(),
                        rs.getInt("kapasitet"),
                        rs.getBigDecimal("pris"),
                        rs.getString("nivaa")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while fetching courses: " + e.getMessage(),
                    e
            );
        }

        return courses;
    }
}