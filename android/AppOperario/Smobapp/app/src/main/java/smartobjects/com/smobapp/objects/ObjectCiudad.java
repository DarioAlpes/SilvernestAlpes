package smartobjects.com.smobapp.objects;

import org.parceler.Parcel;

/**
 * Created by Andres Rubiano on 12/02/2016.
 */

@Parcel
public class ObjectCiudad {
    int id;
    String nombre;
    ObjectPais pais;

    public ObjectCiudad() {
    }

    public ObjectCiudad(int id, String nombre, ObjectPais pais) {
        this.id = id;
        this.nombre = nombre;
        this.pais = pais;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public ObjectPais getPais() {
        return pais;
    }


//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeInt(this.id);
//        dest.writeString(this.nombre);
//        dest.writeParcelable(this.pais, 0);
//    }
//
//    protected ObjectCiudad(Parcel in) {
//        this.id = in.readInt();
//        this.nombre = in.readString();
//        this.pais = in.readParcelable(ObjectPais.class.getClassLoader());
//    }
//
//    public static final Creator<ObjectCiudad> CREATOR = new Creator<ObjectCiudad>() {
//        public ObjectCiudad createFromParcel(Parcel source) {
//            return new ObjectCiudad(source);
//        }
//
//        public ObjectCiudad[] newArray(int size) {
//            return new ObjectCiudad[size];
//        }
//    };
}
