package digital.fact.saver.data.legacyDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LegacyDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 23;
    public static final String DATABASE_NAME = "legacy.db";

    public LegacyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    @Override
    public void onCreate(SQLiteDatabase db) { }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.disableWriteAheadLogging();
    }

}