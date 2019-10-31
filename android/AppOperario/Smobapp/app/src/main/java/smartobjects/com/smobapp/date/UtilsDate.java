package smartobjects.com.smobapp.date;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Andres Rubiano on 20/11/2015.
 */
public class UtilsDate {
    private static UtilsDate ourInstance = new UtilsDate();

    public static UtilsDate getInstance() {
        return ourInstance;
    }

    private UtilsDate() {
    }

    public String getDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Calendar calendar = Calendar.getInstance();
        return sdf.format(calendar.getTime());
    }

}
