package com.jhonsarq.nimblesurvey.utilities

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.google.gson.Gson
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Utils {
    fun hideKeyboard(activity: Activity, view: View) {
        val inputMethodManager: InputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun jsonToMap(json: String): Map<String, Any>? {
        val gson = Gson()
        val type: Type = com.google.gson.reflect.TypeToken.getParameterized(
            Map::class.java, String::class.java, Any::class.java
        ).type
        return gson.fromJson(json, type)
    }

    fun currentDateToSeconds(): Long {
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time

        return currentDate.time / 1000
    }

    fun getCurrentDateFormatted(): String {
        val date = Date()
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH)

        return dateFormat.format(date)
    }

    fun findElementByTag(container: ViewGroup, tag: String): LinearLayout? {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)

            if (child is LinearLayout && child.tag == tag) {
                return child
            } else if (child is ViewGroup) {
                val result = findElementByTag(child, tag)

                if (result != null) {
                    return result
                }
            }
        }

        return null
    }
}