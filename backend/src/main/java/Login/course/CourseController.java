package Login.course;

import io.javalin.http.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * GET /api/courses
 * Filtre (alle valgfrie): lokallag, sted, nivaa, fra, til (yyyy-MM-dd),
 * prisMin, prisMax, pris (gratis/betalt), inkluderFerdige (true/false)
 */
public class CourseController {

    private final CourseRegistry registry;

    public CourseController(CourseRegistry registry) {
        this.registry = registry;
    }

    // Tom verdi eller "alle" betyr at vi ikke filtrerer
    private static String text(String value) {
        return value == null || value.isBlank() || value.equalsIgnoreCase("alle")
                ? null : value.trim().toLowerCase();
    }

    private static LocalDate date(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value.trim());
    }

    private static BigDecimal number(String value) {
        return value == null || value.isBlank() ? null : new BigDecimal(value.trim());
    }

    public void getCourses(Context ctx) {
        try {
            // Bakoverkompatibelt: ?pris=gratis gir prisMax=0, ?pris=betalt gir prisMin=0.01
            String pris = text(ctx.queryParam("pris"));
            BigDecimal prisMin = number(ctx.queryParam("prisMin"));
            BigDecimal prisMax = number(ctx.queryParam("prisMax"));
            if ("gratis".equals(pris)) prisMax = BigDecimal.ZERO;
            if ("betalt".equals(pris)) prisMin = new BigDecimal("0.01");

            CourseFilter filter = new CourseFilter(
                    text(ctx.queryParam("lokallag")),
                    text(ctx.queryParam("sted")),
                    text(ctx.queryParam("nivaa")),
                    date(ctx.queryParam("fra")),
                    date(ctx.queryParam("til")),
                    prisMin,
                    prisMax,
                    "true".equals(ctx.queryParam("inkluderFerdige")));

            List<Course> courses = registry.findCourses(filter);

            ctx.status(200).json(Map.of(
                    "success", true,
                    "message", courses.size() + " courses found.",
                    "courses", courses
            ));

        } catch (DateTimeParseException | NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", "Invalid filter value. Use yyyy-MM-dd for dates and numbers for prices."
            ));

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());

            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", "Courses are unavailable right now. Please try again later."
            ));
        }
    }
}