package smartobjects.com.smobapp.objects;

/**
 * Created by Andres Rubiano on 16/02/2016.
 */
public class ObjectMenu {

    private int mId;
    private String mNombre;

    public ObjectMenu() {
    }

    public ObjectMenu(int mId, String mNombre) {
        this.mId = mId;
        this.mNombre = mNombre;
    }

    public String getmNombre() {
        return mNombre;
    }

    public int getmId() {
        return mId;
    }

    public static ObjectMenu[] ITEMS = {
            new ObjectMenu(1, "Inventario"),
            new ObjectMenu(2, "Auditoría"),
            new ObjectMenu(3, "Antena"),
    };

    /**
     * Obtiene item basado en su identificador
     *
     * @param id identificador
     * @return Coche
     */
    public static ObjectMenu getItem(int id) {
        for (ObjectMenu item : ITEMS) {
            if (item.getmId() == id) {
                return item;
            }
        }
        return null;
    }
}
