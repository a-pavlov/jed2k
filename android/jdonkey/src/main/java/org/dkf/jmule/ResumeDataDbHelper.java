package org.dkf.jmule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dkf.jed2k.AddTransferParams;
import org.dkf.jed2k.Pair;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.Iterator;

public class ResumeDataDbHelper extends SQLiteOpenHelper implements Iterable {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ResumeDataDbHelper.class);

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "resume_data.db";
    private static final String TABLE_NAME  = "resume_data";
    private static final String CNAME_HASH  = "hash";
    private static final String CNAME_RD    = "rd";
    private static final String CREATE_RD_TABLE = "CREATE TABLE " + TABLE_NAME
            + "(" + CNAME_HASH + " TEXT PRIMARY KEY," + CNAME_RD + " BLOB)";

    private Cursor currentCursor = null;

    public ResumeDataDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_RD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // do nothing here
    }

    public void saveResumeData(AddTransferParams atp) throws JED2KException {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CNAME_HASH, atp.getHash().toString());
        ByteBuffer bb = ByteBuffer.allocate(atp.bytesCount());
        atp.put(bb);
        bb.flip();
        contentValues.put(CNAME_RD, bb.array());

        long result = db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        if (result == -1) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }
    }

    public void removeResumeData(Hash hash) {
        SQLiteDatabase db = getWritableDatabase();
        int count = db.delete(TABLE_NAME, CNAME_HASH + " = ?", new String[]{hash.toString()});
        log.debug("delete {} rows", count);
    }

    private class ATPIterator implements Iterator<AddTransferParams> {
        private Cursor cur;
        private boolean hasData;

        public ATPIterator(Cursor cur) {
            this.cur = cur;
            hasData = this.cur.moveToFirst();
        }

        @Override
        public boolean hasNext() {
            return hasData;
        }

        @Override
        public AddTransferParams next() {
            try {
                byte[] data = cur.getBlob(1);
                ByteBuffer bb = ByteBuffer.wrap(data);
                AddTransferParams atp = new AddTransferParams();
                atp.get(bb);
                hasData = cur.moveToNext();
                return atp;
            } catch (JED2KException e) {
                log.warn("can not restore atp {}", e.getMessage());
            }

            return null;
        }
    }

    @NonNull
    @Override
    public Iterator iterator() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT " + CNAME_HASH + ", " + CNAME_RD + " FROM " + TABLE_NAME, null);
        return new ATPIterator(cur);
    }
}
