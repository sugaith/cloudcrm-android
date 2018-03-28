package app.cloudcrm.tech.cloudcrm.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

/**
 * Created by Alberto on 10/5/2016.
 */
public class Settings {

    public String userToken;

    public String companyName;

    public int companyColor;

    public SharedPreferences preferences;

    public SharedPreferences.Editor editor;

    public Settings(Context context){

        preferences = context.getSharedPreferences("cloudcrm.tech", 0);

        editor = preferences.edit();

        userToken = preferences.getString("userToken", "none");

        companyName = preferences.getString("companyName", "CloudCRM");

        String col = preferences.getString("companyColor", "#009966");

        try {

            companyColor = Color.parseColor(col);

        }catch (Exception e){

            //Log.d("CCRM.ERROR", "Invalid color "+col+".."+e.getMessage());

            companyColor = Color.RED;

        }

    }

    public void setUserToken(String userToken){

        editor.putString("userToken", userToken);

        this.userToken = userToken;

        editor.apply();

    }

    public String getUserToken(){

        return preferences.getString("userToken", "none");

    }

    public void setCompanyName(String companyName){

        editor.putString("companyName", companyName);

        this.companyName = companyName;

        editor.apply();

    }

    public String getCompanyName(){

        return preferences.getString("companyName", "CloudCRM");

    }

    public void setCompanyColor(String color){

        editor.putString("companyColor", color);

        editor.apply();

        try {

            this.companyColor = Color.parseColor(color);

        }catch (Exception e){

            this.companyColor = Color.RED;

            //Log.d("CCRM.ERRRO", "Invalid color:"+color+", "+e.getMessage());

        }

    }

    public int getCompanyColor(){

        try {

            return Color.parseColor(preferences.getString("companyColor", "#009966"));

        }catch (Exception e){

            return Color.RED;

        }

    }


}
