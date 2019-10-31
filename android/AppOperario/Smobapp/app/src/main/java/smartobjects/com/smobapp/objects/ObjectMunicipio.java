package smartobjects.com.smobapp.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andres Rubiano on 12/02/2016.
 */
public class ObjectMunicipio implements Parcelable {

    int mId;
    String mNombre;
    ObjectPais mPais;
    ObjectCiudad mCiudad;

    public ObjectMunicipio() {
    }

    public ObjectMunicipio(int mId, String mNombre, ObjectPais mPais, ObjectCiudad mCiudad) {
        this.mId = mId;
        this.mNombre = mNombre;
        this.mPais = mPais;
        this.mCiudad = mCiudad;
    }

    protected ObjectMunicipio(Parcel in) {
        mId = in.readInt();
        mNombre = in.readString();
        mPais = in.readParcelable(ObjectPais.class.getClassLoader());
    }

    public static final Creator<ObjectMunicipio> CREATOR = new Creator<ObjectMunicipio>() {
        @Override
        public ObjectMunicipio createFromParcel(Parcel in) {
            return new ObjectMunicipio(in);
        }

        @Override
        public ObjectMunicipio[] newArray(int size) {
            return new ObjectMunicipio[size];
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(mId);
        dest.writeString(mNombre);
        dest.writeParcelable(mPais, flags);
    }

}
