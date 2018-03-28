package app.cloudcrm.tech.cloudcrm.classes;

import android.util.Log;

import java.text.DecimalFormat;

/**
 * Created by Alberto on 9/10/2016.
 */
public class NumberFormater {

    final static String TAG = "NumberFormaterTag";

    String resultString;

    String pattern;

    int digits;

    public NumberFormater(String pattern){

        this.pattern = pattern;

        digits = count(pattern);

        //Log.d(TAG, "New Formater ("+pattern+")");

    }

    int count(String pattern)
    {
        if(pattern.length() == 0) return 0;

        int numbers = 0;

        for(int i = 0; i < pattern.length(); i++){

            if(pattern.charAt(i) == '#') {
                numbers++;
            }

        }

        //Log.d(TAG, "count("+pattern+") = "+String.valueOf(numbers));

        return numbers;

    }

    String format(String value){

        //Log.d(TAG, "Pattern: "+pattern);

        //Log.d(TAG, "Value: "+value);

        StringBuilder result = new StringBuilder();

        int k = 0;

        for(int i = 0; i < pattern.length(); i++){

            if(pattern.charAt(i) == '#'){
                result.append(value.charAt(k));
                k++;
            }else {
                result.append(pattern.charAt(i));
            }
        }

        //Log.d(TAG, "Result: "+result.toString());

        return result.toString();

    }

    public int getDigits() {
        return digits;
    }
}
