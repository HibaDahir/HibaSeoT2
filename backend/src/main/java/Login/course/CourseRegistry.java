package Login.course;

import java.util.List;


public interface CourseRegistry {

    /** Kurs som ikke har startet ennå, sortert med det første kurset øverst. */
    List<Course> findUpcomingCourses();

    /** Alle kurs, kommende først og så de som er ferdige. null betyr "ikke filtrer på dette". */
    List<Course> findCourses(CourseFilter filter);
}
