package bangz.smartmute.test;

import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestCase;

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


    }

}
