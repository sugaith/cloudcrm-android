package app.cloudcrm.tech.cloudcrm.forms;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by Alberto on 17/6/2016.
 */
public class CustomTextWatcher implements TextWatcher {

    public EditText editText;

    CustomTextWatcher(EditText editText){

        this.editText = editText;

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public EditText getEditText() {
        return editText;
    }

    public void setEditText(EditText editText) {
        this.editText = editText;
    }
}
