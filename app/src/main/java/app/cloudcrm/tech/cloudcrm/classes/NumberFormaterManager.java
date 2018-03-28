package app.cloudcrm.tech.cloudcrm.classes;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Alberto on 9/10/2016.
 */
public class NumberFormaterManager {

    ArrayList<NumberFormater> numberFormaters;

    int minLength;

    boolean leftToRight = false;

    public NumberFormaterManager(){

        numberFormaters = new ArrayList<NumberFormater>();

    }

    public void addFormat(String pattern){

        numberFormaters.add(new NumberFormater(pattern));

    }

    public NumberFormater selectNumberFormater(String val){

        for(NumberFormater nf:numberFormaters){

            if(val.length() == nf.getDigits()){

                return nf;

            }

        }

        return null;

    }

    public String format(String val) {

        if (val.length() < getMinLength()){

            //Log.d(NumberFormater.TAG, val);

            //Log.d(NumberFormater.TAG, String.valueOf(minLength));

            val = (val + "000000000000000000").substring(0, minLength);

        }

        NumberFormater nf = selectNumberFormater(val);

        if(nf!=null){

            return nf.format(val);

        }

        return val;
    }

    public int getMinLength() {

        int retVal = numberFormaters.get(0).getDigits();

        for(NumberFormater nf:numberFormaters){

            //Log.d(NumberFormater.TAG, String.valueOf(retVal)+">"+String.valueOf(nf.getDigits()));

            if(nf.getDigits()<retVal){

                //Log.d(NumberFormater.TAG, "true");

                retVal = nf.getDigits();
            }

        }

        minLength = retVal;

        //Log.d(NumberFormater.TAG, "Min: "+String.valueOf(minLength));

        return retVal;
    }
}
