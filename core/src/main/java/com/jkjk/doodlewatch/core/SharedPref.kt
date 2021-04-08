package com.jkjk.doodlewatch.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.HashMap


class SharedPref private constructor(appContext: Context) {

    private val gson = Gson()
    private val preferences: SharedPreferences = appContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

    private val editor: Editor by lazy { Editor() }

    fun getValue(key: String, defaultValue: String): String? {
        return preferences.getString(key, defaultValue)
    }

    fun getValue(key: String, defaultValue: Int): Int {
        return preferences.getInt(key, defaultValue)
    }

    fun getValue(key: String, defaultValue: Long): Long {
        return preferences.getLong(key, defaultValue)
    }

    fun getValue(key: String, defaultValue: Float): Float {
        return preferences.getFloat(key, defaultValue)
    }

    fun getValue(key: String, defaultValue: Boolean?): Boolean {
        return preferences.getBoolean(key, defaultValue!!)
    }

    fun <T> getValue(key: String, clazz: Class<T>): T? {
        val jsonString = preferences.getString(key, null)
        if (jsonString != null) {
            try {
                val `object` = gson.fromJson(jsonString, clazz)
                return clazz.cast(`object`)
            } catch (e: Exception) {
            }
        }
        return null
    }


    /**
     * A bug in android 7.0, when using HashMap<T, U> as method return type
     * TypeToken<HashMap<String, String>>() {}.type
     * cannot get T, U as String, String, to prevent this, don't use dynamic type for TypeToken
     */
    fun getStringMapValue(key: String): HashMap<String, String>? {

        val jsonString = preferences.getString(key, null)
        var `object`: HashMap<String, String>? = null
        try {
            val type = object : TypeToken<HashMap<String, String>>() {}.type
            `object` = gson.fromJson(jsonString, type)!!
        } catch (e: Exception) {
        }

        return `object`
    }



    /**
     * Manual call is not necessary
     */
    @SuppressLint("ApplySharedPref")
    private fun commit() {
        preferences.edit().also { preferenceEditor ->
            for (entry in this.editor.editPref) {
                when (entry.value) {
                    null -> {
                        preferenceEditor.putString(entry.key, null)
                    }
                    is String -> {
                        preferenceEditor.putString(entry.key, entry.value as String)
                    }
                    is Int -> {
                        preferenceEditor.putInt(entry.key, entry.value as Int)
                    }
                    is Long -> {
                        preferenceEditor.putLong(entry.key, entry.value as Long)
                    }
                    is Float -> {
                        preferenceEditor.putFloat(entry.key, entry.value as Float)
                    }
                    is Boolean -> {
                        preferenceEditor.putBoolean(entry.key, entry.value as Boolean)
                    }
                }
            }
        }.commit()
    }

    inner class Editor {
        val editPref = hashMapOf<String, Any?>()

        fun setValue(key: String, `object`: Any?) {
            when (`object`) {
                null -> {
                    editPref[key] = null
                }
                is String, is Int, is Long, is Float, is Boolean, is Double -> {
                    editPref[key] = `object`
                }
                else -> {
                    val jsonString: String = gson.toJson(`object`)
                    editPref[key] = jsonString
                }
            }
        }

//        fun <T> setList(key: String, value: List<T>) {
//            editPref[key] = JSONArray(value).toString()
//        }
    }

    companion object {
        private const val SHARED_PREF_NAME = "doodlePref"

        fun read(context: Context): SharedPref {
            return SharedPref(context.applicationContext)
        }

        /**
         * Manual commit is not necessary
         */
        fun write(context: Context, action: (Editor) -> Unit) {
            val pref =
                SharedPref(context.applicationContext)
            action(pref.editor)
            pref.commit()
        }
    }
}