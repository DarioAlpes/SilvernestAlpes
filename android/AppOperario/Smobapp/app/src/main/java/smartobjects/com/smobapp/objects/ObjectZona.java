package smartobjects.com.smobapp.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andres Rubiano on 12/02/2016.
 */
public class ObjectZona implements Parcelable {

    int mId;
    String mNombre;
    ObjectPais mPais;
    ObjectCiudad mCiudad;
    ObjectMunicipio mMunicipio;

    public ObjectZona() {
    }

    public ObjectZona(int mId, String mNombre, ObjectPais mPais, ObjectCiudad mCiudad, ObjectMunicipio mMunicipio) {
        this.mId = mId;
        this.mNombre = mNombre;
        this.mPais = mPais;
        this.mCiudad = mCiudad;
        this.mMunicipio = mMunicipio;
    }

    protected ObjectZona(Parcel in) {
        mId = in.readInt();
        mNombre = in.readString();
        mPais = in.readParcelable(ObjectPais.class.getClassLoader());
        mMunicipio = in.readParcelable(ObjectMunicipio.class.getClassLoader());
    }

    public static final Creator<ObjectZona> CREATOR = new Creator<ObjectZona>() {
        @Override
        public ObjectZona createFromParcel(Parcel in) {
            return new ObjectZona(in);
        }

        @Override
        public ObjectZona[] newArray(int size) {
            return new ObjectZona[size];
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
    }

}
