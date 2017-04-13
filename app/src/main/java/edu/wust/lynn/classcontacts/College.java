package edu.wust.lynn.classcontacts;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by lynn on 14-8-21.
 */
public class College {
    public College() {}

    public static class Room implements BaseColumns {
        public static final String TABLE_NAME = "room";
        public static final String COLUMN_NAME_GENDER = "gender";

        public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " TEXT PRIMARY KEY,"
                + COLUMN_NAME_GENDER + " TEXT NOT NULL"
                + " CHECK (" + COLUMN_NAME_GENDER
                + " IN ('" + Student.VALUE_GENDER_BOY + "', '" + Student.VALUE_GENDER_GIRL + "'))"
                + ");";

        public static final Cursor findRoom(SQLiteDatabase db, String room) {
            String selection = Room._ID + " = ?";
            String[] selectionArgs = {room};
            Cursor cursor = db.query(
                    TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            return cursor;
        }

        public static final void deleteRoom(SQLiteDatabase db, String roomID) {
            Cursor cursor = Student.findStudentByRoomID(db, roomID);
            int studentIDIndex = cursor.getColumnIndex(Student._ID);
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                ContentValues values = new ContentValues();
                values.put(Student.COLUMN_NAME_ROOM_ID, "");
                String selection = Student._ID + " = ?";
                String[] selectionArgs = {cursor.getString(studentIDIndex)};
                db.update(Student.TABLE_NAME, values, selection, selectionArgs);
                cursor.moveToNext();
            }

            String selection = _ID + " = ?";
            String[] selectionArgs = {roomID};
            db.delete(TABLE_NAME, selection, selectionArgs);
        }
    }

    public static class Student implements BaseColumns {
        public static final String VALUE_GENDER_BOY = "Boy";
        public static final String VALUE_GENDER_GIRL = "Girl";

        public static final String TABLE_NAME = "student";
        public static final String COLUMN_NAME_PHOTO_NAME = "photo_name";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_GENDER = "gender";
        public static final String COLUMN_NAME_PHONE = "phone";
        public static final String COLUMN_NAME_ROOM_ID = "room_id";
        public static final String COLUMN_NAME_NOTES = "notes";

        public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " TEXT PRIMARY KEY,"
                + COLUMN_NAME_PHOTO_NAME + " TEXT NOT NULL,"
                + COLUMN_NAME_NAME + " TEXT,"
                + COLUMN_NAME_GENDER + " TEXT NOT NULL"
                + " CHECK (" + COLUMN_NAME_GENDER
                + " IN ('" + VALUE_GENDER_BOY + "', '" + VALUE_GENDER_GIRL + "')),"
                + COLUMN_NAME_PHONE + " TEXT,"
                + COLUMN_NAME_ROOM_ID + " INTEGER"
                + " REFERENCES " + Room.TABLE_NAME + "(" + Room._ID + ")"
                + " ON DELETE RESTRICT"
                + " DEFERRABLE INITIALLY DEFERRED,"
                + COLUMN_NAME_NOTES + " TEXT"
                + ");";

        public static final void InsertStudent(SQLiteDatabase db, String id, String photoName, String name,
                                               String gender, String phone, String roomID, String notes) {
            ContentValues values = new ContentValues();

            values.put(_ID, id);
            values.put(COLUMN_NAME_PHOTO_NAME, photoName);
            values.put(COLUMN_NAME_NAME, name);
            values.put(COLUMN_NAME_GENDER, gender);
            values.put(COLUMN_NAME_PHONE, phone);
            values.put(COLUMN_NAME_ROOM_ID, roomID);
            values.put(COLUMN_NAME_NOTES, notes);
            db.insert(TABLE_NAME, null, values);
        }

        public static final void updateStudent(SQLiteDatabase db, String id, String photoName, String name,
                                               String gender, String phone, String roomID, String notes) {
            ContentValues values = new ContentValues();

            values.put(_ID, id);
            values.put(COLUMN_NAME_PHOTO_NAME, photoName);
            values.put(COLUMN_NAME_NAME, name);
            values.put(COLUMN_NAME_GENDER, gender);
            values.put(COLUMN_NAME_PHONE, phone);
            values.put(COLUMN_NAME_ROOM_ID, roomID);
            values.put(COLUMN_NAME_NOTES, notes);

            String selection = _ID + " = ?";
            String[] selectionArgs = {id};
            db.update(TABLE_NAME, values, selection, selectionArgs);
        }

        public static final Cursor findStudentByStudentID(SQLiteDatabase db, String studentID) {
            String selection = Student._ID + " = ?";
            String[] selectionArgs = {studentID};
            Cursor cursor = db.query(
                    TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            return cursor;
        }

        public static final Cursor findStudentByRoomID(SQLiteDatabase db, String roomID) {
            String selection = Student.COLUMN_NAME_ROOM_ID + " = ?";
            String[] selectionArgs = {roomID};
            String sortOrder = _ID + " ASC";
            Cursor cursor = db.query(
                    TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
            return cursor;
        }

        public static final void deleteStudent(SQLiteDatabase db, String studentID) {
            String[] selectionArgs = {studentID};

            String selection = State.COLUMN_NAME_STUDENT_ID + " = ?";
            db.delete(State.TABLE_NAME, selection, selectionArgs);

            selection = Optional.COLUMN_NAME_STUDENT_ID + " = ?";
            db.delete(Optional.TABLE_NAME, selection, selectionArgs);

            selection = Student._ID + " = ?";
            db.delete(StudentFTS3.TABLE_NAME, selection, selectionArgs);

            selection = _ID + " = ?";
            db.delete(TABLE_NAME, selection, selectionArgs);
        }
    }

    public static class StudentFTS3 {
        public static final String TABLE_NAME = "studentFTS3";
        public static final String COLUMN_NAME_PARSE_NAME = "parse_name";
        public static final String COLUMN_NAME_PARSE_ID = "parse_id";
        public static final String COLUMN_NAME_PARSE_PHONE = "parse_phone";
        public static final String COLUMN_NAME_PARSE_ROOM = "parse_room";

        public static final String SQL_CREATE_ENTRIES = "CREATE VIRTUAL TABLE " + TABLE_NAME + " USING fts3 ("
                + COLUMN_NAME_PARSE_NAME + ", "
                + COLUMN_NAME_PARSE_ID + ", "
                + COLUMN_NAME_PARSE_PHONE + ", "
                + COLUMN_NAME_PARSE_ROOM + ", "
                + Student._ID + ", "
                + Student.COLUMN_NAME_PHOTO_NAME + ", "
                + Student.COLUMN_NAME_NAME + ", "
                + Student.COLUMN_NAME_PHONE
                + ");";

        private static String appendSpace(String str) {
            int length = str.length();
            char[] value = new char[length << 1];
            for (int i = 0, j = 0; i < length; ++i, j = i << 1) {
                value[j] = str.charAt(i);
                value[j + 1] = ' ';
            }
            return new String(value);
        }

        private static String appendStarSpace(String str) {
            int length = str.length();
            char[] value = new char[length * 3];
            for (int i = 0, j = 0; i < length; ++i, j = i * 3) {
                value[j] = str.charAt(i);
                value[j + 1] = '*';
                value[j + 2] = ' ';
            }
            return new String(value);
        }

        private static String parseString(String str) {
            FormatString formatString = new FormatString();
            String splitStr = appendSpace(str);
            String pinyinStr = formatString.getStringPinYin(str);
            String splitPinyinStr = formatString.getStringPinYin(appendSpace(str));
            return splitStr + " " + pinyinStr + " " + splitPinyinStr;
        }

        private static String parseNum(String num) {
            String splitNum = appendSpace(num);
            return splitNum;
        }

        public static final void insertStudentFTS3(SQLiteDatabase db, String id, String photoName, String name, String phone, String roomID) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_PARSE_NAME, parseString(name));
            values.put(COLUMN_NAME_PARSE_ID, parseNum(id));
            values.put(COLUMN_NAME_PARSE_PHONE, parseNum(phone));
            values.put(COLUMN_NAME_PARSE_ROOM, parseString(roomID));

            values.put(Student._ID, id);
            values.put(Student.COLUMN_NAME_PHOTO_NAME, photoName);
            values.put(Student.COLUMN_NAME_NAME, name);
            values.put(Student.COLUMN_NAME_PHONE, phone);
            db.insert(TABLE_NAME, null, values);
        }

        public static final void updateStudentFTS3(SQLiteDatabase db, String id, String photoName, String name, String phone, String roomID) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_PARSE_NAME, parseString(name));
            values.put(COLUMN_NAME_PARSE_ID, parseNum(id));
            values.put(COLUMN_NAME_PARSE_PHONE, parseNum(phone));
            values.put(COLUMN_NAME_PARSE_ROOM, parseString(roomID));
            values.put(Student._ID, id);
            values.put(Student.COLUMN_NAME_PHOTO_NAME, photoName);
            values.put(Student.COLUMN_NAME_NAME, name);
            values.put(Student.COLUMN_NAME_PHONE, phone);

            String selection = Student._ID + " = ?";
            String[] selectionArgs = {id};
            db.update(TABLE_NAME, values, selection, selectionArgs);
        }

        public static final Cursor findStudentFTS3(SQLiteDatabase db, String str) {
            String sql = "SELECT "
                    + Student.COLUMN_NAME_PHOTO_NAME + ", "
                    + Student.COLUMN_NAME_NAME + ", "
                    + Student._ID + ", "
                    + Student.COLUMN_NAME_PHONE
                    + " FROM " + TABLE_NAME
                    + " WHERE " + TABLE_NAME
                    + " MATCH ?;";

            String args;
            if ("".equals(str)) {
                args = "";
            } else {
                args = str + "* OR \"" + appendStarSpace(str) + "\"";
            }
            String[] selectionArgs = {
                    args
            };
            return db.rawQuery(sql, selectionArgs);
        }
    }

    public static class Course implements BaseColumns {
        public static final String TABLE_NAME = "course";
        public static final String COLUMN_NAME_PHOTO_NAME = "photo_name";
        public static final String COLUMN_NAME_COURSE_NAME = "course_name";
        public static final String COLUMN_NAME_TEACHER_NAME = "teacher_name";
        public static final String COLUMN_NAME_PHONE = "phone";

        public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " TEXT PRIMARY KEY,"
                + COLUMN_NAME_PHOTO_NAME + " TEXT,"
                + COLUMN_NAME_COURSE_NAME + " TEXT,"
                + COLUMN_NAME_TEACHER_NAME + " TEXT,"
                + COLUMN_NAME_PHONE + " TEXT"
                + ");";

        public static final void insertCourse(SQLiteDatabase db, String id, String photoName, String courseName,
                                              String teacherName, String phone) {
            ContentValues values = new ContentValues();

            values.put(_ID, id);
            values.put(COLUMN_NAME_PHOTO_NAME, photoName);
            values.put(COLUMN_NAME_COURSE_NAME, courseName);
            values.put(COLUMN_NAME_TEACHER_NAME, teacherName);
            values.put(COLUMN_NAME_PHONE, phone);

            db.insert(TABLE_NAME, null, values);
        }

        public static final void updateCourse(SQLiteDatabase db, String id, String photoName, String courseName,
                                              String teacherName, String phone) {
            ContentValues values = new ContentValues();

            values.put(_ID, id);
            values.put(COLUMN_NAME_PHOTO_NAME, photoName);
            values.put(COLUMN_NAME_COURSE_NAME, courseName);
            values.put(COLUMN_NAME_TEACHER_NAME, teacherName);
            values.put(COLUMN_NAME_PHONE, phone);

            String selection = _ID + " = ?";
            String[] selectionArgs = {id};
            db.update(TABLE_NAME, values, selection, selectionArgs);
        }

        public static final Cursor findCourse(SQLiteDatabase db, String courseID) {
            String selection = Course._ID + " = ?";
            String[] selectionArgs = {courseID};
            Cursor cursor = db.query(
                    TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            return cursor;
        }

        public static final void deleteCourse(SQLiteDatabase db, String courseID) {
            String[] selectionArgs = {courseID};

            String selection = State._ID + " IN ("
                    + " SELECT " + Record._ID
                    + " FROM " + Record.TABLE_NAME
                    + " WHERE " + Record.COLUMN_NAME_COURSE_ID
                    + " = ?)";
            db.delete(State.TABLE_NAME, selection, selectionArgs);

            selection = Record.COLUMN_NAME_COURSE_ID + " = ?";
            db.delete(Record.TABLE_NAME, selection, selectionArgs);

            selection = Optional._ID + " = ?";
            db.delete(Optional.TABLE_NAME, selection, selectionArgs);

            selection = Course._ID + " = ?";
            db.delete(TABLE_NAME, selection, selectionArgs);
        }
    }

    public static class Optional implements BaseColumns {
        public static final String TABLE_NAME = "optional";
        public static final String COLUMN_NAME_STUDENT_ID = "student_id";

        public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " TEXT"
                + " REFERENCES " + Course.TABLE_NAME + "(" + Course._ID + ")"
                + " ON DELETE RESTRICT"
                + " DEFERRABLE INITIALLY DEFERRED,"
                + COLUMN_NAME_STUDENT_ID + " TEXT"
                + " REFERENCES " + Student.TABLE_NAME + "(" + Student._ID + ")"
                + " ON DELETE RESTRICT"
                + " DEFERRABLE INITIALLY DEFERRED,"
                + " PRIMARY KEY( " + _ID + ", " + COLUMN_NAME_STUDENT_ID + ")"
                + ");";

        public static final void insertOptional(SQLiteDatabase db, String courseID, String studentID) {
            ContentValues values = new ContentValues();

            values.put(_ID, courseID);
            values.put(COLUMN_NAME_STUDENT_ID, studentID);
            db.insert(TABLE_NAME, null, values);
        }

        public static final Cursor findOptional(SQLiteDatabase db, String courseID, String studentID) {
            String selection = _ID + " = ? AND " + COLUMN_NAME_STUDENT_ID + " = ?";
            String[] selectionArgs = {courseID, studentID};
            Cursor cursor = db.query(
                    TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            return cursor;
        }

        public static final void deleteOptional(SQLiteDatabase db, String courseID, String studentID) {
            String[] selectionArgs = {studentID, courseID};

            String selection = State.COLUMN_NAME_STUDENT_ID + " = ? AND "
                    + State._ID + " IN ("
                    + " SELECT " + Record.COLUMN_NAME_COURSE_ID
                    + " FROM " + Record.TABLE_NAME
                    + " WHERE " + Record.COLUMN_NAME_COURSE_ID
                    + " = ?)";
            db.delete(State.TABLE_NAME, selection, selectionArgs);

            selection = COLUMN_NAME_STUDENT_ID + " = ?";
            String[] selectionArgs2 = {studentID};
            db.delete(TABLE_NAME, selection, selectionArgs2);
        }
    }

    public static class Record implements BaseColumns {
        public static final String TABLE_NAME = "record";
        public static final String COLUMN_NAME_COURSE_ID = "course_id";
        public static final String COLUMN_NAME_TIME = "time";

        public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME_COURSE_ID + " TEXT"
                + " REFERENCES " + Course.TABLE_NAME + "(" + Course._ID + ")"
                + " ON DELETE RESTRICT"
                + " DEFERRABLE INITIALLY DEFERRED,"
                + COLUMN_NAME_TIME + " timestamp"
                + " DEFAULT (DATETIME('NOW', 'LOCALTIME'))"
                + ");";

        public static final Cursor findRecords(SQLiteDatabase db, String courseID) {
            String selection = COLUMN_NAME_COURSE_ID + " = ?";
            String[] selectionArgs = {courseID};
            Cursor cursor = db.query(
                    TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            return cursor;
        }

        public static final void deleteRecord(SQLiteDatabase db, int recordID) {
            String selection = State._ID + " = " + recordID;
            db.delete(State.TABLE_NAME, selection, null);

            selection = _ID + " = " + recordID;
            db.delete(TABLE_NAME, selection, null);
        }
    }

    public static class State implements BaseColumns {
        public static final String VALUE_STATE_NORMAL = "Normal";
        public static final String VALUE_STATE_LEAVE = "Leave";
        public static final String VALUE_STATE_ABSENT = "Absent";

        public static final String TABLE_NAME = "state";
        public static final String COLUMN_NAME_STUDENT_ID = "student_id";
        public static final String COLUMN_NAME_STATE = "state";

        public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER,"
                + COLUMN_NAME_STUDENT_ID + " TEXT"
                + " REFERENCES " + Student.TABLE_NAME + "(" + Student._ID + ")"
                + " ON DELETE RESTRICT"
                + " DEFERRABLE INITIALLY DEFERRED,"
                + COLUMN_NAME_STATE + " TEXT NOT NULL"
                + " CHECK (" + COLUMN_NAME_STATE
                + " IN ('" + VALUE_STATE_NORMAL + "', '" + VALUE_STATE_LEAVE + "', '" + VALUE_STATE_ABSENT + "')),"
                + " PRIMARY KEY( " + _ID + ", " + COLUMN_NAME_STUDENT_ID + ")"
                + ");";

        public static final void insertState(SQLiteDatabase db, int recordID, String studentID, String state) {
            ContentValues values = new ContentValues();
            values.put(_ID, recordID);
            values.put(COLUMN_NAME_STUDENT_ID, studentID);
            values.put(COLUMN_NAME_STATE, state);
            db.insert(TABLE_NAME, null, values);
        }

        public static final void deleteRecordState(SQLiteDatabase db, int recordID) {
            String selection = _ID + " = " + recordID;
            db.delete(TABLE_NAME, selection, null);
        }

        public static final void deleteStudentState(SQLiteDatabase db, String studentID) {
            String selection = COLUMN_NAME_STUDENT_ID + " = ?";
            String[] selectionArgs = {studentID};
            db.delete(TABLE_NAME, selection, selectionArgs);
        }
    }
}