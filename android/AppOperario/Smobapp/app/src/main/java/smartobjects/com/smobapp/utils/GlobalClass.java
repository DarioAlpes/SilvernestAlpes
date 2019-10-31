package smartobjects.com.smobapp.utils;

import android.app.Application;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;


/**
 * Created by Andres Rubiano on 08/10/2015.
 */
public class GlobalClass extends Application {

    public String usuario = "";

    public String token = "";

    public String getUsuario() {
        return usuario;
    }

    public String getToken() {
        return token;
//        return "";
    }

    private static AsciiCommander commander = null;

    /// Returns the current AsciiCommander
    public AsciiCommander getCommander() { return commander; }

    /// Sets the current AsciiCommander
    public void setCommander(AsciiCommander _commander ) { commander = _commander; }

    @Override
    public void onCreate() {
        super.onCreate();
        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

                // DynamicRealm exposes an editable schema
                RealmSchema schema = realm.getSchema();

                if (oldVersion == 0) {
//                    schema.create("Test")
//                            .addField("age", int.class);
                    schema.create("ObjectItem")
                            .addField("epc", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("sku", String.class)
                            .addField("ubicacion", String.class)
                            .addField("estado", String.class)
                            .addField("nombre", String.class)
                            .addField("talla", String.class)
                            .addField("ubicacion", String.class)
                            .addField("itemEsperados", int.class)
                            .addField("itemEncontrados", int.class)
                            .addField("estatus", int.class)
                            .addField("rutaImagen", String.class)
                            .addField("skuCompleto", boolean.class)
                            ;
                    oldVersion++;
                }

                // Migrate to version 2: Add a primary key + object references
                // Example:
                // public Person extends RealmObject {
                //     private String name;
                //     @PrimaryKey
                //     private int age;
                //     private Dog favoriteDog;
                //     private RealmList<Dog> dogs;
                //     // getters and setters left out for brevity
                // }
                if (oldVersion == 1) {
                    schema.get("Test")
                            .addField("id", long.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("favoriteDog", schema.get("Dog"))
                            .addRealmListField("dogs", schema.get("Dog"));
                    oldVersion++;
                }
            }
        };

        // The realm file will be located in Context.getFilesDir() with name "default.realm"
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                                    .name("myrealm.realm")
                                    .schemaVersion(2)
                                    .migration(migration)
                                    .build();
        Realm.setDefaultConfiguration(config);
    }
}
