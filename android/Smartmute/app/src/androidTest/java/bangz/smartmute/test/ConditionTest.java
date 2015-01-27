package bangz.smartmute.test;

import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.GregorianCalendar;

import bangz.smartmute.content.Condition;
import bangz.smartmute.content.ConditionFactory;
import bangz.smartmute.content.LocationCondition;
import bangz.smartmute.content.TimeCondition;
import bangz.smartmute.content.WifiCondition;

/**
 * Created by royerwang on 2014-09-26.
 */
public class ConditionTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @SmallTest
    public void testLocationCondition() {

        String strlocation = "location: 45.678, 78.334" ;
        Condition condition = ConditionFactory.CreateCondition(strlocation);
        assertNotNull(condition);
        assertEquals(Condition.RT_LOCATION, condition.getType());
        assertEquals("location: 45.678, 78.334", condition.toString());
        assertEquals("location: 45.678, 78.334", ((LocationCondition)condition).BuildConditionString());
        assertEquals(45.678f, ((LocationCondition) condition).getLatitude());
        assertEquals(78.334f, ((LocationCondition)condition).getLongitude());
        assertEquals(0, ((LocationCondition)condition).getRadar());
        assertEquals(true, condition.isValidConditionString(strlocation));


        strlocation = "location: 45.234, -45.221, 56";
        condition = ConditionFactory.CreateCondition(strlocation);
        assertNotNull(condition);
        assertEquals(Condition.RT_LOCATION, condition.getType());
        assertEquals("location: 45.234, -45.221, 56", condition.toString());
        assertEquals("location: 45.234, -45.221, 56", ((LocationCondition)condition).BuildConditionString());
        assertEquals(45.234f, ((LocationCondition)condition).getLatitude());
        assertEquals(-45.221f, ((LocationCondition)condition).getLongitude());
        assertEquals(56, ((LocationCondition)condition).getRadar());
        assertEquals(true, condition.isValidConditionString(strlocation));


        strlocation = "  location:  45.234,    -45.221,56";
        condition = ConditionFactory.CreateCondition(strlocation);
        assertNotNull(condition);
        assertEquals(Condition.RT_LOCATION, condition.getType());
        assertEquals("location: 45.234, -45.221, 56", condition.toString());
        assertEquals("location: 45.234, -45.221, 56", ((LocationCondition)condition).BuildConditionString());
        assertEquals(45.234f, ((LocationCondition)condition).getLatitude());
        assertEquals(-45.221f, ((LocationCondition)condition).getLongitude());
        assertEquals(56, ((LocationCondition)condition).getRadar());
        assertEquals(true, condition.isValidConditionString(strlocation));

        strlocation = "Location: 45.556, 32.12, 200";
        assertEquals(true, LocationCondition.checkStringFormat(strlocation));


    }

    public void testWifiCondition() {
        String strWifi ;
        Condition condition ;
        strWifi = "  Wifi: Wddtte  ";
        condition = ConditionFactory.CreateCondition(strWifi);
        assertEquals(Condition.RT_WIFI, condition.getType());
        assertEquals(true, condition.isValidConditionString(strWifi));
        assertEquals("Wddtte", ((WifiCondition)condition).getSSID());
        assertEquals("wifi: Wddtte", condition.toString());

        strWifi = "dfdfdfd";
        assertEquals(false, WifiCondition.checkStringFormat(strWifi));

        strWifi = "  WiFi  :  ddftef";
        condition = ConditionFactory.CreateCondition(strWifi);
        assertEquals(Condition.RT_WIFI, condition.getType());
        assertEquals(true, condition.isValidConditionString(strWifi));
        assertEquals("ddftef", ((WifiCondition)condition).getSSID());

    }
    @SmallTest
    public void testTimeCondition() {

        String strttt = "\"test\"";
        strttt = strttt.replaceAll("[\\^\\\"\\\"\\$]","");
        assertEquals(strttt,"test");

        String strwhichday = "1001111";

        assertEquals(Integer.parseInt("1111001",2), TimeCondition.parseWhichdayString(strwhichday));

        strwhichday = "0100100";
        int wd = Integer.parseInt("0010010",2);
        assertEquals(strwhichday, TimeCondition.whichdaysToString(wd));

        String strcondition;
        TimeCondition condition ;

        strcondition = "time: 23:00, 7:15, 0100110";
        condition = (TimeCondition)ConditionFactory.CreateCondition(strcondition);
        assertEquals(Condition.RT_TIME, condition.getType());
        assertEquals("time: 23:00, 07:15, 0100110",condition.toString());
        assertEquals(false, condition.isEnableOnThisDay(TimeCondition.IDX_SUNDAY));
        assertEquals(true, condition.isEnableOnThisDay(TimeCondition.IDX_MONDAY));
        assertEquals(false, condition.isEnableOnThisDay(TimeCondition.IDX_TUESDAY));
        assertEquals(false, condition.isEnableOnThisDay(TimeCondition.IDX_WEDNESDAY));
        assertEquals(true, condition.isEnableOnThisDay(TimeCondition.IDX_THURSDAY));
        assertEquals(true, condition.isEnableOnThisDay(TimeCondition.IDX_FRIDAY));
        assertEquals(false, condition.isEnableOnThisDay(TimeCondition.IDX_SATURDAY));

        Calendar testtoday = new GregorianCalendar(2014,Calendar.DECEMBER,22);
        assertEquals(Calendar.MONDAY,testtoday.get(Calendar.DAY_OF_WEEK));
        assertEquals(true, condition.isEnableToday(testtoday));

        testtoday.add(Calendar.DAY_OF_YEAR,1); //Tuesday
        assertEquals(Calendar.TUESDAY,testtoday.get(Calendar.DAY_OF_WEEK));
        assertEquals(false, condition.isEnableToday(testtoday));

        testtoday.add(Calendar.DATE, 1); //Wednesday
        assertEquals(Calendar.WEDNESDAY,testtoday.get(Calendar.DAY_OF_WEEK));
        assertEquals(false, condition.isEnableToday(testtoday));

        testtoday.add(Calendar.DATE, 1);//THURSDAY
        assertEquals(Calendar.THURSDAY,testtoday.get(Calendar.DAY_OF_WEEK));
        assertEquals(true, condition.isEnableToday(testtoday));

        testtoday.add(Calendar.DATE, 1);//FRIDAY
        assertEquals(Calendar.FRIDAY, testtoday.get(Calendar.DAY_OF_WEEK));
        assertEquals(true, condition.isEnableToday(testtoday));

        testtoday.add(Calendar.DATE, 1);//SATURDAY
        assertEquals(Calendar.SATURDAY,testtoday.get(Calendar.DAY_OF_WEEK));
        assertEquals(false, condition.isEnableToday(testtoday));


        // Start testgetNextMuteTime
        testtoday.set(2014,Calendar.DECEMBER,21,13,23,59);
        Calendar nextday = new GregorianCalendar(2014,Calendar.DECEMBER,22,23,00);
        Calendar cr = condition.getNextMuteTime(testtoday);
        assertEquals(0, cr.compareTo(nextday));

        testtoday.set(2014, Calendar.DECEMBER, 22, 13, 23,22);
        nextday.set(2014,Calendar.DECEMBER,22,23,00);
        cr = condition.getNextMuteTime(testtoday);
        assertEquals(0, cr.compareTo(nextday));

        testtoday.set(2014,Calendar.DECEMBER,22,23,14,33);
        nextday.set(2014,Calendar.DECEMBER,25,23,00);
        cr = condition.getNextMuteTime(testtoday);
        assertEquals(0, cr.compareTo(nextday));

        testtoday.set(2014,Calendar.DECEMBER,23,22,59);
        cr = condition.getNextMuteTime(testtoday);
        assertEquals(0, cr.compareTo(nextday));

        testtoday.set(2014,Calendar.DECEMBER,26,13,22);
        nextday.set(2014,Calendar.DECEMBER,26,23,00);
        cr = condition.getNextMuteTime(testtoday);
        assertEquals(0, cr.compareTo(nextday));

        testtoday.set(2014,Calendar.DECEMBER,27,22,59);
        nextday.set(2014, Calendar.DECEMBER,29,23,00);
        cr = condition.getNextMuteTime(testtoday);
        assertEquals(0, cr.compareTo(nextday));

        condition = (TimeCondition)ConditionFactory.CreateCondition("time: 23:00, 7:15, 0000000");
        cr = condition.getNextMuteTime(testtoday);
        assertEquals(null,cr);

    }

}
