package smartobjects.com.smobapp.sharedPreferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import smartobjects.com.smobapp.utils.UtilsConstants;


/**
 * Created by Andres Rubiano on 26/10/2015.
 * La clase permite el almacenamiento y obtención de la información que se ha de guardar en
 * las preferencias del dispositivo. Se crea la clase a través del Patrón Singleton.
 */
public class UtilsSharedPreferences {

    public static UtilsSharedPreferences mPreferencias;
    private CharSequence preferencesName;
    private Activity mContext;

    public UtilsSharedPreferences() {}

    public static UtilsSharedPreferences newInstance(Activity activity){
        if (mPreferencias == null) {
            mPreferencias = new UtilsSharedPreferences(UtilsConstants.GENERAL.PREFERENCES,
                                                        activity);
        }
        return mPreferencias;
    }

    public UtilsSharedPreferences(CharSequence preferencesName, Activity mContext) {
        this.preferencesName = preferencesName;
        this.mContext = mContext;
    }

    public int getPreference(CharSequence key) throws Exception {
        int value = 0;
        if (mContext != null){
            SharedPreferences preferences = getPreferences();

            if (preferences != null) {
                value = preferences.getInt(String.valueOf(key), -1);
            }
        }
        return value;
    }

    public String getPreference(CharSequence key, String x) throws Exception {
        String value = "";
        if (mContext != null){
            SharedPreferences preferences = getPreferences();

            if (preferences != null) {
                value = preferences.getString(String.valueOf(key), "");
            }
        }
        return value;
    }

    public boolean getPreference(CharSequence key, boolean type) throws Exception {
        boolean value = false;
        if (mContext != null){
            SharedPreferences preferences = getPreferences();

            if (preferences != null) {
                value = preferences.getBoolean(String.valueOf(key), false);
            }
        }
        return value;
    }

    public Set<String> getPreference(CharSequence key, HashSet type) throws Exception {
        Set<String> value = new HashSet();
        if (mContext != null){
            SharedPreferences preferences = getPreferences();

            if (preferences != null) {
                value = preferences.getStringSet(String.valueOf(key), null);
            }
        }
        return value;
    }

    public boolean deletePreference(CharSequence key) throws Exception {
        if (mContext != null){
            SharedPreferences preferences = getPreferences();
            if (preferences != null) {
                preferences.edit().remove(String.valueOf(key)).commit();
            }
        }
        return false;
    }

    private SharedPreferences getPreferences() throws NullPointerException{
        return mContext.getSharedPreferences(
                String.valueOf(mPreferencias.preferencesName),
                Context.MODE_PRIVATE);
    }

    public void savePreference(CharSequence key, int value) throws Exception {
        if (mContext != null) {
            SharedPreferences preferencias= getPreferences();
            SharedPreferences.Editor editor=preferencias.edit();
            editor.putInt(String.valueOf(key), value);
            editor.commit();
        }
    }

    public void savePreference(CharSequence key, String value) throws Exception {
        if (mContext != null) {
            SharedPreferences preferencias= getPreferences();
            SharedPreferences.Editor editor=preferencias.edit();
            editor.putString(String.valueOf(key), value);
            editor.commit();
        }
    }

    public void savePreference(CharSequence key, boolean value) throws Exception {
        if (mContext != null) {
            SharedPreferences preferencias= getPreferences();
            SharedPreferences.Editor editor=preferencias.edit();
            editor.putBoolean(String.valueOf(key), value);
            editor.commit();
        }
    }

    public void savePreference(CharSequence key, Set<String> value) throws Exception {
        if (mContext != null) {
            SharedPreferences preferencias= getPreferences();
            SharedPreferences.Editor editor=preferencias.edit();
            editor.putStringSet(String.valueOf(key), value);
            editor.commit();
        }
    }

}
