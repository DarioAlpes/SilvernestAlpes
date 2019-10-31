package smartobjects.com.smobapp.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Andres Rubiano on 06/04/2016.
 */
public class Test extends RealmObject {

    @PrimaryKey
    private int age;
    private String estado;

    public Test() {
    }

    public Test(int age, String estado) {
        this.age = age;
        this.estado = estado;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Test{" +
                "age=" + age +
                ", estado='" + estado + '\'' +
                '}';
    }
}
