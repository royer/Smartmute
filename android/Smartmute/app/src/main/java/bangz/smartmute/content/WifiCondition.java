package bangz.smartmute.content;

/**
 * Created by royerwang on 2014-09-23.
 */
public class WifiCondition extends Condition {

    public static final String TAG = WifiCondition.class.getSimpleName();

    public static final String strMatch = "(?i)\\s*wifi\\s*:\\s*\\S+\\s*";

    private String SSID ;

    public WifiCondition() {
        setType(Condition.RT_WIFI);
    }

    public WifiCondition(String strcondition) {

        setType(Condition.RT_WIFI);

        SSID = getParamString(strcondition);

        setConditionString(BuildConditionString());

    }

    @Override
    public boolean isValidConditionString(String strcondition) {
        return checkStringFormat(strcondition);
    }

    public static boolean checkStringFormat(String strcondition) {
        return strcondition.matches(strMatch);
    }
    @Override
    public String getParamString(String strcondition) {
        return strcondition.replaceAll("(?i)\\s*wifi\\s*:\\s*","").trim();
    }

    @Override
    public String BuildConditionString() {
        return "wifi: " + SSID ;
    }

    public String getSSID() {
        return SSID ;
    }

    public void setSSID(String ssid) {
        SSID = ssid ;
    }
}
