package smartobjects.com.smobapp.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andres Rubiano on 12/02/2016.
 */
public class ObjectTienda implements Parcelable {

    int mId;
    String mNombre;
    ObjectPais mPais;
    ObjectCiudad mCiudad;
    ObjectMunicipio mMunicipio;
    ObjectZona mZona;

    public ObjectTienda() {
    }

    public ObjectTienda(int mId, String mNombre, ObjectPais mPais, ObjectCiudad mCiudad, ObjectMunicipio mMunicipio, ObjectZona mZona) {
        this.mId = mId;
        this.mNombre = mNombre;
        this.mPais = mPais;
        this.mCiudad = mCiudad;
        this.mMunicipio = mMunicipio;
        this.mZona = mZona;
    }

    protected ObjectTienda(Parcel in) {
        mId = in.readInt();
        mNombre = in.readString();
        mPais = in.readParcelable(ObjectPais.class.getClassLoader());
        mMunicipio = in.readParcelable(ObjectMunicipio.class.getClassLoader());
        mZona = in.readParcelable(ObjectZona.class.getClassLoader());
    }

    public static final Creator<ObjectTienda> CREATOR = new Creator<ObjectTienda>() {
        @Override
        public ObjectTienda createFromParcel(Parcel in) {
            return new ObjectTienda(in);
        }

        @Override
        public ObjectTienda[] newArray(int size) {
            return new ObjectTienda[size];
        }
    };

    public int getmId() {
        return mId;
    }

    public String getmNombre() {
        return mNombre;
    }

    public ObjectPais getmPais() {
        return mPais;
    }

    public ObjectCiudad getmCiudad() {
        return mCiudad;
    }

    public ObjectMunicipio getmMunicipio() {
        return mMunicipio;
    }

    public ObjectZona getmZona() {
        return mZona;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(mId);
        dest.writeString(mNombre);
        dest.writeParcelable(mPais, flags);
        dest.writeParcelable(mMunicipio, flags);
        dest.writeParcelable(mZona, flags);
    }

}
