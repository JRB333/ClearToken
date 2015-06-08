package com.clancy.cleartoken2;

/**
 * Created from Mark's code on 5/7/2015.
 */

import android.content.Context;
import android.content.SharedPreferences;

public class WorkingStorage {

    public static String GetCharVal(String VariableName, Context dan) {
        SharedPreferences AticketGetPrefs = dan.getSharedPreferences("dbHoldValues",0);
        return AticketGetPrefs.getString(VariableName, "");
    }

    public static void StoreCharVal(String VariableName, String WhatToWrite, Context dan) {
        SharedPreferences AticketPutPrefs = dan.getSharedPreferences("dbHoldValues", 0);
        SharedPreferences.Editor prefEditor = AticketPutPrefs.edit();
        prefEditor.putString(VariableName, WhatToWrite);
        prefEditor.commit();
    }

    public static void StoreLongVal(String VariableName, int WhatToWrite, Context dan) {
        SharedPreferences AticketPutPrefs = dan.getSharedPreferences("dbLongValues", 0);
        SharedPreferences.Editor prefEditor = AticketPutPrefs.edit();
        prefEditor.putInt(VariableName, WhatToWrite);
        prefEditor.commit();
    }

    public static int GetLongVal(String VariableName, Context dan) {
        SharedPreferences AticketGetPrefs = dan.getSharedPreferences("dbLongValues",0);
        return AticketGetPrefs.getInt(VariableName, 0);
    }
}