package com.bangz.smartmute.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.bangz.smartmute.content.MuteRulesProvider;
import com.bangz.smartmute.content.RulesColumns;

/**
 * Created by royerwang on 2014-09-30.
 */
public class MuteRulesProviderTest extends ProviderTestCase2<MuteRulesProvider> {

    private MockContentResolver mMockResolver ;
    private SQLiteDatabase mDb ;

    private final RULE[] TEST_RULES = {
        new RULE("TestLocation01", 1, RulesColumns.RT_LOCATION,"location: 45.223, -56.764",
                45.223, -56.764, 20.0f,"",RulesColumns.RM_SILENT),
        new RULE("TestLocation02", 1, RulesColumns.RT_LOCATION,"Location:-89.45, 65.667",
                -89.45, 65.667, 50.4f,"", RulesColumns.RM_VIBRATE),
        new RULE("TestWifi01",1, RulesColumns.RT_WIFI,"Wifi: netgear44",0,0,0,"",RulesColumns.RM_VIBRATE),
        new RULE("TestWifi02",1, RulesColumns.RT_WIFI, "WIFI: ROYERnono",0,0,0,"", RulesColumns.RM_SILENT),
        new RULE("TestTime01",1,RulesColumns.RT_TIME,"TIME: 23:15, 7:20,1111111",0,0,0,"",RulesColumns.RM_SILENT),
        new RULE("TestTime02",1,RulesColumns.RT_TIME,"TIME: 13:00, 16:00,0111110",0,0,0,"",RulesColumns.RM_SILENT)
    };

    public MuteRulesProviderTest() {
        super(MuteRulesProvider.class, MuteRulesProvider.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mMockResolver = getMockContentResolver();
        mDb = getProvider().getOpenHelperForTest().getWritableDatabase();

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private void insertData() {

        for (int i = 0; i < TEST_RULES.length; i++) {
            mDb.insertOrThrow(RulesColumns.TABLE_NAME,null, TEST_RULES[i].getContentValues());
        }
    }

    /**
     * Test insert into the data model
     */
    public void testInserts() {
        RULE rule = new RULE("Single Test", 1, RulesColumns.RT_LOCATION,
                "location: 34.4522, -22.5443",
                34.4522,-22.5443,30.0f,"",RulesColumns.RM_SILENT);

        Uri  rowUri = mMockResolver.insert(RulesColumns.CONTENT_URI, rule.getContentValues());
        long rowid = ContentUris.parseId(rowUri);

        // Does a full query on the table. Since InsertData() hasn't yet been called, the
        // table should only contain the record just inserted.
        Cursor cursor = mMockResolver.query(
                RulesColumns.CONTENT_URI,   // main table URI
                null,                       // no projection, return all the columns
                null,                       // no selection criteria, return all row in the model
                null,                       // no selection argument
                null                        // default sort order
        );

        assertEquals(1, cursor.getCount());

        assertTrue(cursor.moveToFirst());

        int idxName = cursor.getColumnIndex(RulesColumns.NAME);
        int idxActivated = cursor.getColumnIndex(RulesColumns.ACTIVATED);
        int idxRuletype = cursor.getColumnIndex(RulesColumns.RULETYPE);
        int idxCondition = cursor.getColumnIndex(RulesColumns.CONDITION);
        int idxLatitude = cursor.getColumnIndex(RulesColumns.LATITUDE);
        int idxLongitude = cursor.getColumnIndex(RulesColumns.LONGITUDE);
        int idxRadius    = cursor.getColumnIndex(RulesColumns.RADIUS);
        int idxSecondCondition = cursor.getColumnIndex(RulesColumns.SECONDCONDITION);
        int idxRingmode = cursor.getColumnIndex(RulesColumns.RINGMODE);

        assertEquals(rule.name, cursor.getString(idxName));
        assertEquals(rule.actived, cursor.getInt(idxActivated));
        assertEquals(rule.ruletype, cursor.getInt(idxRuletype));
        assertEquals(rule.condition, cursor.getString(idxCondition));
        assertEquals(rule.latitude, cursor.getDouble(idxLatitude));
        assertEquals(rule.longitude, cursor.getDouble(idxLongitude));
        assertEquals(rule.radius, cursor.getFloat(idxRadius));
        assertEquals(rule.secondcondition, cursor.getString(idxSecondCondition));
        assertEquals(rule.ringmode, cursor.getInt(idxRingmode));

        //insert subtest 2
        //Test that we cann't insert a record whose id value already exists
        ContentValues values = rule.getContentValues();
        values.put(RulesColumns._ID, rowid);

        // Tries to insert this record into the table. This should fail and drop into the
        // catch block. If it succeeds, issue a failure message.
        try {
            rowUri = mMockResolver.insert(RulesColumns.CONTENT_URI, values);
            fail("Excepted insert failure for existing record, but insert succeeded.");
        } catch(Exception e) {
            // do nothing
        }
    }

    public void testDeletes() {

        // subtest 1
        // Tries to delete record from empty table

        final String SELECTION_COLUMNS = RulesColumns.ACTIVATED + " = " + "?";

        // Sets selection argument "1"
        final String[] SELECTION_ARGS = {"1"};

        // Tries to delete rows matching the selection criteria from the table
        int rowdeleted = mMockResolver.delete(RulesColumns.CONTENT_URI,SELECTION_COLUMNS,SELECTION_ARGS);

        assertEquals(0, rowdeleted);

        // subtest 2
        insertData();

        rowdeleted = mMockResolver.delete(RulesColumns.CONTENT_URI,SELECTION_COLUMNS, SELECTION_ARGS);
        assertEquals(6, rowdeleted);

        Cursor cursor = mMockResolver.query(
                RulesColumns.CONTENT_URI,
                null,
                SELECTION_COLUMNS,
                SELECTION_ARGS,
                null
        );

        assertEquals(0,cursor.getCount());
    }

    public void testUpdates() {

        // Subtest 1
        final String SELECTION_COLUMNS = RulesColumns.NAME + " = ?";
        final String[] SELECTION_ARGS = {"TestLocation01"};

        ContentValues values = new ContentValues();

        values.put(RulesColumns.ACTIVATED, 0);

        int rowupdated = mMockResolver.update(
                RulesColumns.CONTENT_URI,
                values,
                SELECTION_COLUMNS,
                SELECTION_ARGS
        );
        assertEquals(0, rowupdated);

        insertData();
        rowupdated = mMockResolver.update(
                RulesColumns.CONTENT_URI,
                values,
                SELECTION_COLUMNS,
                SELECTION_ARGS
        );
        assertEquals(1, rowupdated);

    }

    public void testQueriesOnRuleTableUri() {

        final String[] TEST_PROJECTION = {
                RulesColumns.NAME,
                RulesColumns.RULETYPE,
                RulesColumns.CONDITION,
                RulesColumns.ACTIVATED,
                RulesColumns.SECONDCONDITION,
                RulesColumns.RINGMODE,
        };

        final String NAME_SELECTION = RulesColumns.NAME + " = ?";
        final String SELECTION_COLUMNS =
                NAME_SELECTION + " OR " + NAME_SELECTION + " OR " + NAME_SELECTION;
        final String[] SELECTION_ARGS = {
                "TestLocation01",
                "TestWifi01",
                "TestTime02"
        };

        final String SORT_ORDER = RulesColumns.NAME + " ASC";

        Cursor cursor = mMockResolver.query(
                RulesColumns.CONTENT_URI,
                null,null,null,null);
        assertEquals(0, cursor.getCount());

        insertData();
        cursor = mMockResolver.query(RulesColumns.CONTENT_URI,null,null,null,null);
        assertEquals(TEST_RULES.length, cursor.getCount());

        Cursor projectionCursor = mMockResolver.query(
                RulesColumns.CONTENT_URI,
                TEST_PROJECTION,
                null,
                null,
                null
        );
        assertEquals(TEST_PROJECTION.length, projectionCursor.getColumnCount());

        assertEquals(TEST_PROJECTION[0], projectionCursor.getColumnName(0));
        assertEquals(TEST_PROJECTION[1], projectionCursor.getColumnName(1));
        assertEquals(TEST_PROJECTION[2], projectionCursor.getColumnName(2));
        assertEquals(TEST_PROJECTION[3], projectionCursor.getColumnName(3));
        assertEquals(TEST_PROJECTION[4], projectionCursor.getColumnName(4));
        assertEquals(TEST_PROJECTION[5], projectionCursor.getColumnName(5));

        projectionCursor = mMockResolver.query(
                RulesColumns.CONTENT_URI,
                TEST_PROJECTION,
                SELECTION_COLUMNS,
                SELECTION_ARGS,
                null
        );
        assertEquals(SELECTION_ARGS.length, projectionCursor.getCount());

        int index = 0;

        while(projectionCursor.moveToNext()) {
            assertEquals(SELECTION_ARGS[index], projectionCursor.getString(0));
            index++;
        }

        assertEquals(SELECTION_ARGS.length, index);

    }

    public void testQueriesOnRuleIDUri() {

        final String SELECTION_COLUMNS = RulesColumns.NAME + " = ?";
        final String[] SELECTION_ARGS = {"TestLocation01"};

        final String[] TEST_PROJECTION = {
                RulesColumns._ID,
                RulesColumns.NAME
        };

        Uri RuleIdUri = ContentUris.withAppendedId(RulesColumns.CONTENT_ID_URI_BASE,1);
        Cursor cursor = mMockResolver.query(RuleIdUri,
                null,
                null,
                null,
                null);
        assertEquals(0, cursor.getCount());

        insertData();
        cursor = mMockResolver.query(RulesColumns.CONTENT_URI,
                TEST_PROJECTION,
                SELECTION_COLUMNS,
                SELECTION_ARGS,
                null);
        assertEquals(1, cursor.getCount());

        assertTrue(cursor.moveToFirst());
        int Id = cursor.getInt(0);
        RuleIdUri = ContentUris.withAppendedId(RulesColumns.CONTENT_ID_URI_BASE, Id);
        cursor = mMockResolver.query(RuleIdUri,
                TEST_PROJECTION,
                SELECTION_COLUMNS,
                SELECTION_ARGS,
                null);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals(Id, cursor.getInt(0));
    }

    private static class RULE {
        String  name ;
        int     actived;
        int     ruletype;
        String  condition;
        double  latitude;
        double  longitude;
        float   radius;
        String  secondcondition;
        int     ringmode;

        public RULE(String n, int a, int rt, String mcondition,
                    double lati, double longi, float r,String scondition, int rm) {
            name = n;
            actived = a;
            ruletype = rt;
            condition = mcondition;
            latitude = lati;
            longitude = longi;
            radius = r;
            secondcondition = scondition;
            ringmode = rm;
        }

        public ContentValues getContentValues() {

            ContentValues values = new ContentValues();

            values.put(RulesColumns.NAME, name);
            values.put(RulesColumns.ACTIVATED, actived);
            values.put(RulesColumns.RULETYPE, ruletype);
            values.put(RulesColumns.CONDITION, condition);
            values.put(RulesColumns.LATITUDE, latitude);
            values.put(RulesColumns.LONGITUDE, longitude);
            values.put(RulesColumns.RADIUS, radius);
            values.put(RulesColumns.SECONDCONDITION, secondcondition);
            values.put(RulesColumns.RINGMODE, ringmode);
            return values;

        }
    }
}
