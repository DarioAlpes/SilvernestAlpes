package smartobjects.com.smobapp.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Andres Rubiano on 15/02/2016.
 */

public class ObjectProducto implements Parcelable {

    int id;
    String SKU;
    static ArrayList<ObjectProducto> mListaProductos = new ArrayList<>();

    public ObjectProducto() {
    }

    public ObjectProducto(String SKU) {
        this.SKU = SKU;
    }

    public ObjectProducto(int id, String SKU) {
        this.id = id;
        this.SKU = SKU;
    }

    public int getId() {
        return id;
    }

    public String getSKU() {
        return SKU;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.SKU);
    }

    protected ObjectProducto(Parcel in) {
        this.id = in.readInt();
        this.SKU = in.readString();
    }

    public static final Parcelable.Creator<ObjectProducto> CREATOR = new Parcelable.Creator<ObjectProducto>() {
        public ObjectProducto createFromParcel(Parcel source) {
            return new ObjectProducto(source);
        }

        public ObjectProducto[] newArray(int size) {
            return new ObjectProducto[size];
        }
    };

    public static ArrayList<ObjectProducto> getProductos(){
        return mListaProductos;
    }

    public static void setProductos(ArrayList<ObjectProducto> productos){
        mListaProductos.clear();
        mListaProductos.addAll(productos);
    }

    public static void setProducto(ObjectProducto productos){
        mListaProductos.clear();
        mListaProductos.add(productos);
    }

    public static ObjectProducto getProducto(int id) throws Exception {
        return mListaProductos.get(id);
    }

}