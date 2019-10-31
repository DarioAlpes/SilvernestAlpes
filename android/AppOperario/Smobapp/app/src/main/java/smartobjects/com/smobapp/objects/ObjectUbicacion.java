package smartobjects.com.smobapp.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Andres Rubiano on 03/03/2016.
 */
public class ObjectUbicacion
//        extends RealmObject
        implements Parcelable
{
    private int id;
//    @Required
    private String nombre;
    private String llave;
    private String tipo;
    private String padre;
    @SerializedName("EPC")
    private String epc;

    public ObjectUbicacion() {
    }

    public ObjectUbicacion(int id, String nombre, String llave, String tipo, String epc) {
        this.id = id;
        this.nombre = nombre;
        this.llave = llave;
        this.tipo = tipo;
        this.epc = epc;
    }

    public ObjectUbicacion(int id, String nombre, String llave, String tipo, String padre, String epc) {
        this.id = id;
        this.nombre = nombre;
        this.llave = llave;
        this.tipo = tipo;
        this.padre = padre;
        this.epc = epc;
    }

    public String getPadre() {
        return padre;
    }

    public void setPadre(String padre) {
        this.padre = padre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLlave() {
        return llave;
    }

    public void setLlave(String llave) {
        this.llave = llave;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nombre);
        dest.writeString(llave);
        dest.writeString(tipo);
        dest.writeString(padre);
        dest.writeString(epc);
    }

    protected ObjectUbicacion(Parcel in) {
        id = in.readInt();
        nombre = in.readString();
        llave = in.readString();
        tipo = in.readString();
        padre = in.readString();
        epc = in.readString();
    }

    public static final Creator<ObjectUbicacion> CREATOR = new Creator<ObjectUbicacion>() {
        @Override
        public ObjectUbicacion createFromParcel(Parcel in) {
            return new ObjectUbicacion(in);
        }

        @Override
        public ObjectUbicacion[] newArray(int size) {
            return new ObjectUbicacion[size];
        }
    };

    @Override
    public String toString() {
        return "ObjectUbicacion{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", llave='" + llave + '\'' +
                ", tipo='" + tipo + '\'' +
                ", padre='" + padre + '\'' +
                ", epc='" + epc + '\'' +
                '}';
    }
}
