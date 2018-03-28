package app.cloudcrm.tech.cloudcrm.classes;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.text.DecimalFormat;

import app.cloudcrm.tech.cloudcrm.R;

/**
 * Created by Alberto on 3/8/2016.
 */
public class FloatEditText extends EditText {

    static String TAG = "FLOAT_EDIT_TEXT";

    double floatValue = 0.0f;

    double maxVal = -1;

    double minVal = -1;

    String stringValue = "";

    boolean currencyFormat = true;

    OnChangeListener onChangeListener;

    Formater formater;

    int defaultColor = Color.WHITE;

    String decimalFormatString = "#,###,###,##0.00";

    int decimalPlaces = 4;

    int multiplier = 100;

    static Formater defaultFormater = new Formater() {
        @Override
        public String format(double value) {

            DecimalFormat df2 = new DecimalFormat( "#,###,###,##0.00" );

            return df2.format(value);
        }
    };

    public static Formater phoneFormater = new Formater() {
        @Override
        public String format(double value) {

            String numberStr = String.valueOf(value);
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            try {
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(numberStr, "BR");
                //Since you know the country you can format it as follows:
                return phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            } catch (NumberParseException e) {
                System.err.println("NumberParseException was thrown: " + e.toString());
            }

            return null;
        }
    };

    private boolean isCurrencyFormat;

    public FloatEditText(Context context) {
        super(context);
    }

    public FloatEditText(Context context, AttributeSet attributeSet){
        super(context, attributeSet);

    }

    public FloatEditText(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
    }

    public void setText(String text){

        super.setText(text);

    }

    public String getDecimalFormatString() {
        return decimalFormatString;
    }

    public void setDecimalFormatString(String decimalFormatString) {
        this.decimalFormatString = decimalFormatString;
    }

    public String formatOutput(){

        DecimalFormat decimalFormat = new DecimalFormat(decimalFormatString);

        //decimalFormat.setMultiplier(multiplier);

        decimalFormat.setMaximumFractionDigits(decimalPlaces);

        decimalFormat.setMinimumFractionDigits(decimalPlaces);

        return decimalFormat.format(floatValue);

    }

    public void appendText(String appendString){

        //Log.d(TAG, "appendText("+appendString+")");

        DecimalFormat decimalFormat = new DecimalFormat("#");

        decimalFormat.setMinimumFractionDigits(0);

        decimalFormat.setMaximumFractionDigits(0);

        Double temp = Double.parseDouble(stringValue+appendString) / (double) multiplier;

        if(((temp <= maxVal) || (maxVal==-1))) {

            stringValue += appendString;

        }else if(temp > maxVal){

            //stringValue = decimalFormat.format(*(double)multiplier);

            ////Log.d(TAG, "Setting maxVal: "+stringValue);

        }else if(temp < minVal){

            //stringValue = String.valueOf(Math.round.xml(minVal*100));

        }

        updateView();

    }

    public void setFloatValue(double value){
        setFloatValue(value, true);
    }

    public void setFloatValue(double value, boolean triggerOnChangeListener){

        this.floatValue = value;

        DecimalFormat decimalFormat = new DecimalFormat("#");

        decimalFormat.setMultiplier(multiplier);

        decimalFormat.setMinimumFractionDigits(0);

        decimalFormat.setMaximumFractionDigits(0);

        this.stringValue = decimalFormat.format(floatValue);

        //Log.d(TAG, "StringValue: "+stringValue);

        updateView(triggerOnChangeListener);

    }

    public void backspace(){



        try {
            if (stringValue.length() > 0) {

                //Log.d(TAG, "before stringValue is: "+stringValue);

                stringValue = stringValue.substring(0, stringValue.length() - 1);

                //Log.d(TAG, "after stringValue is: "+stringValue);

            }
        }catch (Exception e){

            stringValue = "";

        }
        updateView();
    }

    public void updateView(){
        updateView(true);
    }

    public void updateView(boolean triggerOnChangeListener){

        //Log.d(TAG, "UpdateView!");

        double oldVal = floatValue;

        DecimalFormat decimalFormat = new DecimalFormat("#");

        decimalFormat.setMultiplier(multiplier);

        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setMaximumFractionDigits(0);

        try {
            this.floatValue = Double.parseDouble(stringValue) / (double) multiplier;

            //Log.d(TAG, "UpdatedView:"+decimalFormat.format(floatValue));

        }catch (Exception e){
            floatValue = 0.0f;
            e.printStackTrace();
            //Log.d(TAG, e.getMessage());

        }

        setError(null);

        if((floatValue > maxVal)&&(maxVal!=-1)){
            floatValue = oldVal;
            stringValue = decimalFormat.format(Math.abs(oldVal));
            //Log.d(TAG, "MaxVal:"+decimalFormat.format(maxVal));
            setError(getContext().getString(R.string.err_max_val)+formater.format(maxVal));
        }

        /*if((floatValue < minVal)&&(minVal!=-1)){
            floatValue = minVal;
            stringValue = String.valueOf(Math.abs(floatValue)*100.0f);
            //Log.d(TAG, "MinVal:"+minVal);
        }*/

        if(formater == null) {

            setText(formatOutput());

        }else{

            //Log.d(TAG, "not currency format");

            setText(formater.format(floatValue));

        }

        //Log.d(TAG, "stringValue = "+stringValue);

        if((onChangeListener != null) && (triggerOnChangeListener))

            onChangeListener.onChange(this, floatValue, oldVal);

    }

    private void init(){

        this.setFocusable(true);

        this.multiplier = (int) Math.pow(10, decimalPlaces);

    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
        this.multiplier = (int) Math.pow(10, decimalPlaces);
    }

    public boolean isCurrencyFormat() {
        return currencyFormat;
    }

    public void setCurrencyFormat(boolean currencyFormat) {

        //Log.d(TAG, "Currency: "+String.valueOf(currencyFormat));

        this.currencyFormat = currencyFormat;
    }

    public OnChangeListener getOnChangeListener() {
        return onChangeListener;
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public double getFloatValue() {
        return floatValue;
    }

    public void setIsCurrencyFormat(boolean isCurrencyFormat) {
        this.isCurrencyFormat = isCurrencyFormat;
    }

    public interface OnChangeListener{
        void onChange(FloatEditText floatEditText, double newVal, double oldVal);
    }

    public double getMinVal() {
        return minVal;
    }

    public void setMinVal(float minVal) {

        this.minVal = minVal;

        if(floatValue < minVal){
            setFloatValue(minVal);
        }
    }

    public double getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(double maxVal) {
        this.maxVal = maxVal;


        if(floatValue > maxVal){

            setFloatValue(maxVal);

        }
    }

    public interface Formater{
        String format(double value);
    }

    public Formater getFormater() {
        return formater;
    }

    public void setFormater(Formater formater) {
        this.formater = formater;
    }

    public void setFocus(){
        this.setBackgroundColor(Color.argb(50, 0, 0, 0));
        this.setCursorVisible(true);
    }
    public void removeFocus(){
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setCursorVisible(false);
    }

}
