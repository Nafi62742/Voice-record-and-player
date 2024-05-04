package com.ataraxia.pawahara.utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ataraxia.pawahara.model.Schedule_Pojos;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PreferenceUtils {
    private static final String PREF_NAME = "MyAppPreferences";
    private static final String PREF_DURATION = "MyAppDuration";
    private static final String PREF_BOTTOM_BAR = "BottombarPreferences";

    // Keys for saving and retrieving state
    private static final String STATE_KEY = "app_state";
    private static final String DURATION_KEY = "record_duration";
    int savedIndex = 1;
    private Map<String, String> translations;
    private static final String CURRENT_PRICE_KEY = "current_price";
    private static final String PRICE_MAP_KEY = "price_map_key";

    public static void saveState(Context context, int state) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STATE_KEY, state);
        editor.apply();
    }

    public static int getState(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(STATE_KEY, -1); // Default value is 0
    }

    public static void savePrice(Context context, String currentPrice) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CURRENT_PRICE_KEY, currentPrice);
        editor.apply();
    }

    public static String getCurrentPrice(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(CURRENT_PRICE_KEY, ""); // Default value is 0
    }

    public static void savePriceMap(Context context, HashMap<Integer, String> indexPriceMap) {

        Gson gson = new Gson();
        String json = gson.toJson(indexPriceMap);
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PRICE_MAP_KEY, json);
        editor.apply();
    }

//    public static HashMap<Integer, String> loadPriceMap(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        String json = sharedPreferences.getString(PRICE_MAP_KEY, null);
//
//        Gson gson = new Gson();
//        HashMap<Integer, String> indexPriceMap = gson.fromJson(json, HashMap.class);
//
//        if (indexPriceMap == null) {
//            indexPriceMap = new HashMap<>();
//        }
//
//        return indexPriceMap;
//    }

    public static HashMap<Integer, String> loadPriceMap(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(PRICE_MAP_KEY, null);

        Log.d(TAG, "Loaded JSON from SharedPreferences: " + json);

        Gson gson = new Gson();
        HashMap<Integer, String> indexPriceMap = gson.fromJson(json, new TypeToken<HashMap<Integer, String>>() {}.getType());

        if (indexPriceMap == null) {
            indexPriceMap = new HashMap<>();
        }

        return indexPriceMap;
    }



    public static void saveDuration(Context context, String Duration) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_DURATION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DURATION_KEY, Duration);
        editor.apply();
    }

    public static String getDuration(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_DURATION, Context.MODE_PRIVATE);
        return preferences.getString(DURATION_KEY, "00:00:00"); // Default value is 0
    }

    public static void saveSelectedIndex(Context context,int index) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_BOTTOM_BAR,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("selected_index", index);
        editor.apply();
    }
// Use savedIndex as needed

    public static int getSavedIndex(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_BOTTOM_BAR,Context.MODE_PRIVATE);
        return preferences.getInt("selected_index", 0);
    }


    //schedule preference
    public void saveScheduleListToSharedPreferences(Context context, List<Schedule_Pojos> scheduleList) {
        SharedPreferences preferences = context.getSharedPreferences("YourSharedPreferencesName", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        JSONArray jsonArray = new JSONArray();

        // Convert scheduleList to a JSONArray
        for (Schedule_Pojos schedule : scheduleList) {
            JSONObject scheduleObject = new JSONObject();
            try {
                scheduleObject.put("schedule_switch", schedule.getSchedule_switch());
                scheduleObject.put("name", schedule.getSchedule_name());
                JSONArray scheduleDaysArray = new JSONArray(schedule.getSchedule_days());
                scheduleObject.put("schedule_days", scheduleDaysArray);
                scheduleObject.put("startTime", schedule.getStart_time());
                scheduleObject.put("endTime", schedule.getEnd_time());
                scheduleObject.put("schedule_push_switch", schedule.getSchedule_push_switch());
                jsonArray.put(scheduleObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Save the JSONArray as a string in SharedPreferences
        editor.putString("scheduleList", jsonArray.toString());
        editor.apply();
    }
    public List<Schedule_Pojos> getScheduleListFromSharedPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("YourSharedPreferencesName", Context.MODE_PRIVATE);
        String currentLanguage = Locale.getDefault().getLanguage();



        String jsonArrayString = preferences.getString("scheduleList", null);

        if (jsonArrayString != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonArrayString);
                List<Schedule_Pojos> scheduleList = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject scheduleObject = jsonArray.getJSONObject(i);
                    boolean scheduleSwitch = scheduleObject.getBoolean("schedule_switch");
                    String name = scheduleObject.getString("name");
                    List<Integer> checkedChips = new ArrayList<>();
                    JSONArray checkedChipsArray = scheduleObject.getJSONArray("schedule_days");
                    for (int j = 0; j < checkedChipsArray.length(); j++) {
                        checkedChips.add(checkedChipsArray.getInt(j));
                        Log.d(TAG, "getScheduleListFromSharedPreferences: test1"+checkedChipsArray.getInt(j));
                    }

//                    Popuputils.showCustomDialog(context,checkedChips.toString());
                    String startTime = scheduleObject.getString("startTime");
                    String endTime = scheduleObject.getString("endTime");
                    boolean schedule_push_switch = scheduleObject.getBoolean("schedule_push_switch");


                    Schedule_Pojos schedule = new Schedule_Pojos(scheduleSwitch, name,new ArrayList<>(checkedChips)
                            , startTime, endTime, schedule_push_switch);
                    scheduleList.add(schedule);
                }

                return scheduleList;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return new ArrayList<>(); // Return an empty list in case of an error or if data doesn't exist.
    }

    private String getTranslatedToJapanese(String dayNames) {
        String currentLanguage = Locale.getDefault().getLanguage();
        String translatedDayNames = "";
//        for (String day : dayNames) {
            switch (dayNames) {
                case "Su.":
                    translatedDayNames += currentLanguage.equals("ja") ? "日" : "Sun";
                    break;
                case "Mo.":
                    translatedDayNames += currentLanguage.equals("ja") ? "月" : "Mon";
                    break;
                case "Tu.":
                    translatedDayNames += currentLanguage.equals("ja") ? "火" : "Tue";
                    break;
                case "We.":
                    translatedDayNames += currentLanguage.equals("ja") ? "水" : "Wed";
                    break;
                case "Th.":
                    translatedDayNames += currentLanguage.equals("ja") ? "木" : "Thu";
                    break;
                case "Fr.":
                    translatedDayNames += currentLanguage.equals("ja") ? "金" : "Fri";
                    break;
                case "Sa.":
                    translatedDayNames += currentLanguage.equals("ja") ? "土" : "Sat";
                    break;
            }

//        }
        return translatedDayNames;
    }
    private static List<String> convertToTranslationFormat(List<String> dayNamesList) {
        Map<String, String> translations = new HashMap<>();
        translations.put("日", "Su.");
        translations.put("月", "Mo.");
        translations.put("火", "Tu.");
        translations.put("水", "We.");
        translations.put("木", "Th.");
        translations.put("金", "Fr.");
        translations.put("土", "Sa.");

        List<String> convertedDayNames = new ArrayList<>();

        for (String originalDayName : dayNamesList) {
            // Trim the originalDayName to remove any leading or trailing whitespace
            String trimmedDayName = originalDayName.trim();

            // Check if the trimmedDayName exists in the translations map
            if (translations.containsKey(trimmedDayName)) {
                // Add the translated value to the result list
                convertedDayNames.add(translations.get(trimmedDayName));
            } else {
                // If not found in translations, keep the original value
                convertedDayNames.add(trimmedDayName);
            }
        }

        return convertedDayNames;
    }
    private static final String STATE_KEY1 = "another_state";


    public static void boolSave(Context context, boolean state) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(STATE_KEY1, state);
        editor.apply();
    }

    public static boolean getBool(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(STATE_KEY1, false); // Default value is 0
    }
}
