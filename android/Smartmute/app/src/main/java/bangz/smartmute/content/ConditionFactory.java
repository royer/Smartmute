package bangz.smartmute.content;

import android.util.Log;

/**
 * Created by royerwang on 2014-09-23.
 */
public class ConditionFactory {

    public static final String TAG=ConditionFactory.class.getSimpleName();

    public static Condition CreateCondition(String strrule) {

        Condition condition = null;

        strrule = strrule.replaceAll("[\\s\\t\\n]+"," ").trim();
        String strkey = "";

        try {
            strkey = strrule.substring(0, strrule.indexOf(':')).trim();

            if (strkey.compareToIgnoreCase(Condition.KEYLOCATION) == 0) {
                condition = new LocationCondition(strrule);
            } else if (strkey.compareToIgnoreCase(Condition.KEYWIFI) == 0) {
                condition = new WifiCondition(strrule);
            } else if (strkey.compareToIgnoreCase(Condition.KEYTIME) == 0) {
                condition = new TimeCondition(strrule);
            } else {
                return null ;
            }

        }catch (Exception e) {
            Log.e(TAG, e.toString());
            condition = null ;
        }


        return condition;
    }
}
