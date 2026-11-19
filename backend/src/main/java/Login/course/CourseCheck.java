package Login.course;

import Login.DbConfig;

public class CourseCheck {
    public static void main(String[] args) {
        System.out.println("Arbeidsmappe: " + System.getProperty("user.dir"));
        try {
            var courses = DbConfig.load().createCourseRegistry().findUpcomingCourses();
            System.out.println("Fant " + courses.size() + " kurs:");
            courses.forEach(k -> System.out.println(" - " + k.dato() + "  " + k.tittel()));
        } catch (Exception e) {
            System.out.println("FEIL: " + e.getMessage());
        }
    }
}
