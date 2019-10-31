package smartobjects.com.smobapp.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andres Rubiano on 12/02/2016.
 */
public class ObjectPais implements Parcelable{

    int mId;
    String mNombre;

    public ObjectPais() {
    }

    public ObjectPais(int mId, String mNombre) {
        this.mId = mId;
        this.mNombre = mNombre;
    }

    public int getmId() {
        return mId;
    }

    public String getmNombre() {
        return mNombre;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mNombre);
    }

    protected ObjectPais(Parcel in) {
        this.mId = in.readInt();
        this.mNombre = in.readString();
    }

    public static final Creator<ObjectPais> CREATOR = new Creator<ObjectPais>() {
        public ObjectPais createFromParcel(Parcel source) {
            return new ObjectPais(source);
        }

        public ObjectPais[] newArray(int size) {
            return new ObjectPais[size];
        }
    };
}
