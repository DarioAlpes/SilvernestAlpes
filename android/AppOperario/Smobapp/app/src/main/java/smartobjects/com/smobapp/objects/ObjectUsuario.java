package smartobjects.com.smobapp.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andres Rubiano on 15/02/2016.
 */
public class ObjectUsuario implements Parcelable {

    public String username, password, token;

    public ObjectUsuario() {
    }

    public ObjectUsuario(String token) {
        this.token = token;
    }

    public ObjectUsuario(String username, String password, String token) {
        this.username = username;
        this.password = password;
        this.token = token;
    }

    public ObjectUsuario(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.username);
        dest.writeString(this.password);
        dest.writeString(this.token);
    }

    protected ObjectUsuario(Parcel in) {
        this.username = in.readString();
        this.password = in.readString();
        this.token = in.readString();
    }

    public static final Parcelable.Creator<ObjectUsuario> CREATOR = new Parcelable.Creator<ObjectUsuario>() {
        public ObjectUsuario createFromParcel(Parcel source) {
            return new ObjectUsuario(source);
        }

        public ObjectUsuario[] newArray(int size) {
            return new ObjectUsuario[size];
        }
    };

    @Override
    public String toString() {
        return "ObjectUsuario{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
