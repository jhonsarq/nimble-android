package com.jhonsarq.nimblesurvey.utilities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "nimble.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS user (accessToken TEXT, expiresIn INTEGER, refreshToken TEXT, createdAt INTEGER, name TEXT, avatarUrl TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun addData(table: String, values: ContentValues) {
        val db = this.writableDatabase
        db.insert(table, null, values)
        db.close()
    }

    fun updateData(table: String, values: ContentValues, whereClause: String?, whereArgs: Array<String>?) {
        val db = this.writableDatabase
        db.update(table, values, whereClause, whereArgs)
        db.close()
    }

    fun deleteData(table: String, whereClause: String?, whereArgs: Array<String>?) {
        val db = this.writableDatabase
        db.delete(table, whereClause, whereArgs)
        db.close()
    }

    @SuppressLint("Recycle")
    fun getData(table:String, columns: Array<String>?, selection: String?, selectionArgs: Array<String>?, groupBy: String?, having: String?, orderBy: String?): MutableList<Map<String, Any?>> {
        val db = this.writableDatabase
        val cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
        val result = mutableListOf<Map<String, Any?>>()

        if(cursor != null) {
            while(cursor.moveToNext()) {
                val row = mutableMapOf<String, Any?>()

                for(columnName in cursor.columnNames) {
                    val index = cursor.getColumnIndex(columnName)

                    when(cursor.getType(index)) {
                        Cursor.FIELD_TYPE_BLOB -> row[columnName] = cursor.getBlob(index)
                        Cursor.FIELD_TYPE_FLOAT -> row[columnName] = cursor.getFloat(index)
                        Cursor.FIELD_TYPE_INTEGER -> row[columnName] = cursor.getLong(index)
                        Cursor.FIELD_TYPE_NULL -> row[columnName] = null
                        Cursor.FIELD_TYPE_STRING -> row[columnName] = cursor.getString(index)
                    }
                }

                result.add(row)
            }

            cursor.close()
        }

        db.close()

        return result
    }
}