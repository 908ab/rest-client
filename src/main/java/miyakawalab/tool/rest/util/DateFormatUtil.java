package miyakawalab.tool.rest.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtil {
    private DateFormatUtil() {}

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    public static String toString(Date date) {
        return FORMAT.format(date);
    }

    public static Date toDate(String string) throws ParseException {
        return FORMAT.parse(string);
    }
}
