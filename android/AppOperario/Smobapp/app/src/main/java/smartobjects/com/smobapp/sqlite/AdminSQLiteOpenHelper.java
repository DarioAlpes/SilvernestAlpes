package smartobjects.com.smobapp.sqlite;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;

import java.util.ArrayList;

/**
 * Created by Andres Rubiano on 30/10/2015.
 */
public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CARTONES";
    private static final int DATABASE_VERSION = 10;
    ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();
    private long fileSize = 0;

    Context mContext;

    public AdminSQLiteOpenHelper(Context context){
        super(context,
                DATABASE_NAME,//String name
                null,//factory
                DATABASE_VERSION//int version
        );
    }

    public AdminSQLiteOpenHelper(Context context, String nombre, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, nombre, factory, version);
        mContext = context;
        //show();
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnte, int versionNue) {
        deleteTable(db);
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) throws NullPointerException {
        if (db != null) {
//            String query = CREATE_TABLE +
//                    SPACE +
//                    TABLE_CARTON +
//                    "(LETRA,VALOR)";
//
//            db.execSQL(query);
        }
    }

    //Elimina la base de datos creada
    private void deleteTable(SQLiteDatabase db) throws NullPointerException {
        if (db != null) {
//            db.execSQL(DROP_TABLE +
//                    SPACE +
//                    TABLE_CARTON);
        }
    }

    public boolean insertCarton (String valor)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("valor", valor);
        db.insert("CARTON", null, contentValues);
        return true;
    }

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, "");
        return numRows;
    }

    public boolean updateContact (Integer id, String name, String phone, String email, String street,String place)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("street", street);
        contentValues.put("place", place);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteCartones ()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("CARTON",
                null,
                null);
    }

    public ArrayList<String> getAllCartones()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select VALOR from CARTON", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            String dato = "B,I,N,G,O," + res.getString(res.getColumnIndex("VALOR"));
            array_list.add(dato);
            res.moveToNext();
        }
        return array_list;
    }
}
