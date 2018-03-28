package app.cloudcrm.tech.cloudcrm.forms;

import java.util.ArrayList;

import app.cloudcrm.tech.cloudcrm.R;

/**
 * Created by Alberto on 8/9/2016.
 */
public class FormularioAdapterConstants {

    public static final String
            TYPE_TEXT =         "text"      ,
            TYPE_INT =          "int"       ,
            TYPE_DATE =         "date"      ,
            TYPE_HEADING =      "heading"   ,
            TYPE_PICTURE =      "picture"   ,
            TYPE_SELECT =       "select"    ,
            TYPE_BOOLEAN =      "boolean"   ,
            TYPE_SIGNPAD =      "signpad"   ,
            TYPE_GPS =          "gps"       ,
            TYPE_SAVE_N_SEND =  "save_and_send",
            TYPE_CIDADE =       "cidade"    ,
            TYPE_CPF =          "cpf"       ,
            TYPE_EMAIL =        "email"     ,
            TYPE_FLOAT =        "float"     ,
            TYPE_FOREING =      "foreing"   ,
            TYPE_BIT =          "bit"       ,
            TYPE_POLYGON =      "polygon"   ,
            TYPE_CUSTOM =       "custom"    ,
            TYPE_PAYMENT =      "payment"   ,
            TYPE_PHONE =        "phone";


    public static ArrayList<Tipo> tipos = new ArrayList<Tipo>(){
        {
            add(new Tipo(R.layout.formularios_text_item, 0, "null"));
            add(new Tipo(R.layout.formularios_int_item, 1, TYPE_INT));
            add(new Tipo(R.layout.formularios_text_item, 2, TYPE_TEXT));
            add(new Tipo(R.layout.formularios_date_item, 3, TYPE_DATE));
            add(new Tipo(R.layout.formularios_heading_item, 4, TYPE_HEADING));
            add(new Tipo(R.layout.formularios_picture_item, 5, TYPE_PICTURE));
            add(new Tipo(R.layout.formularios_select_item, 6, TYPE_SELECT));
            add(new Tipo(R.layout.formularios_checkbox_item, 7, TYPE_BOOLEAN));
            add(new Tipo(R.layout.formularios_signpad_item, 8, TYPE_SIGNPAD));
            add(new Tipo(R.layout.formularios_gps_item, 9, TYPE_GPS));
            add(new Tipo(R.layout.formularios_save_send_item, 10, TYPE_SAVE_N_SEND));
            add(new Tipo(R.layout.formularios_text_cidade_item, 11, TYPE_CIDADE));
            add(new Tipo(R.layout.formularios_text_item, 12, TYPE_CPF));
            add(new Tipo(R.layout.formularios_text_item, 13, TYPE_EMAIL));
            add(new Tipo(R.layout.formularios_float_item, 14, TYPE_FLOAT));
            add(new Tipo(R.layout.formularios_checkbox_item, 15, TYPE_BIT));
            add(new Tipo(R.layout.formularios_select_item, 16, TYPE_FOREING));
            add(new Tipo(R.layout.formularios_float_item, 17, TYPE_POLYGON));
            add(new Tipo(R.layout.formularios_payment_item, 18, TYPE_PAYMENT));
            add(new Tipo(R.layout.formularios_custom_item,19, TYPE_CUSTOM));
            add(new Tipo(R.layout.formularios_text_phone_item,20, TYPE_PHONE));
        }

    };
}
