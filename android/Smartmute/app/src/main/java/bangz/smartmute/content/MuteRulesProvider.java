package bangz.smartmute.content;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MuteRulesProvider extends ContentProvider {

    private static final String TAG = MuteRulesProvider.class.getSimpleName();

    public static final String AUTHORITY = "com.bangz.smartmute.provider";
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "smartmute.db";


    enum UrlType {
        RULES, RULES_ID
    }


    private final UriMatcher uriMatcher ;


    private DatabaseHelper  mOpenHelper ;
    private SQLiteDatabase mDb;

    public MuteRulesProvider() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, RulesColumns.TABLE_NAME,UrlType.RULES.ordinal());
        uriMatcher.addURI(AUTHORITY,RulesColumns.TABLE_NAME + "/#",UrlType.RULES_ID.ordinal());
    }

    /**
     *  Gets the {@link UrlType} for a url
     *
     * @param uri the uri
     * @return
     */
    private UrlType getUrlType(Uri uri) {
        return UrlType.values()[uriMatcher.match(uri)];
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        //throw new UnsupportedOperationException("Not yet implemented");
        Log.d(TAG, "Deleting record.");
        int count ;
        try {
            mDb.beginTransaction();
            count = mDb.delete(RulesColumns.TABLE_NAME, selection, selectionArgs);
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }

        getContext().getContentResolver().notifyChange(uri,null,false);

        return count ;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (values == null) {
            values = new ContentValues();
        }

        Uri result = null ;
        try {
            mDb.beginTransaction();
            long rowid = mDb.insert(RulesColumns.TABLE_NAME, RulesColumns._ID, values);
            if (rowid >= 0) {
                result = ContentUris.appendId(RulesColumns.CONTENT_URI.buildUpon(), rowid).build();
            } else {
                throw new SQLiteException("Failed to insert a rule. " + uri);
            }
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }

        getContext().getContentResolver().notifyChange(uri, null, false);

        return result ;
    }

    @Override
    public boolean onCreate() {
        return onCreate(getContext()) ;
    }

    /**
     * Helper method to make onCreate is testable
     * @param context context to creates database
     * @return true means run successfully
     */
    boolean onCreate(Context context) {

        mOpenHelper = new DatabaseHelper(context) ;
        try {
            mDb = mOpenHelper.getWritableDatabase() ;
        } catch (SQLiteException e) {
            Log.e(TAG, "Unable to open database for writeing.", e);
        }

        return mDb != null ;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(RulesColumns.TABLE_NAME);
        String sort = null ;
        switch(getUrlType(uri)) {
            case RULES:
                sort = (sortOrder != null)? sortOrder : RulesColumns.DEFAULT_SORT_ORDER;
                break;
            case RULES_ID:
                queryBuilder.appendWhere("_id=" + uri.getPathSegments().get(1));
                break;
        }

        Cursor cursor = queryBuilder.query(mDb, projection, selection, selectionArgs,
                null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor ;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        String where ;

        switch(getUrlType(uri)) {
            case RULES:
                where = selection ;
                break;
            case RULES_ID:
                where = "_ID = " + uri.getPathSegments().get(1);
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND ( " + selection + " )";
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown Url: " + uri);
        }

        int count ;
        try {
            mDb.beginTransaction();
            count = mDb.update(RulesColumns.TABLE_NAME,values, where, selectionArgs);
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null, false);
        return count;
    }


    public static class DatabaseHelper extends SQLiteOpenHelper {


        public DatabaseHelper(Context context, String name) {
            super(context, name, null, DATABASE_VERSION);
        }

        public DatabaseHelper(Context context) {
            this(context, DATABASE_NAME) ;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(RulesColumns.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.w(TAG, "Upgradeing databse from version " + oldVersion + " to " + newVersion);
        }
    }

    public DatabaseHelper getOpenHelperForTest() {
        return mOpenHelper ;
    }
}
