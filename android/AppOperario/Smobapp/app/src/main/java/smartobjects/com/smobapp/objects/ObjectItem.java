package smartobjects.com.smobapp.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import smartobjects.com.smobapp.bluetoothTsl.ReadWriteModel;

/**
 * Created by Andres Rubiano on 16/02/2016.
 */
public class ObjectItem extends RealmObject implements Parcelable {

    int    id;
    String SKU;
    String ubicacion;
    @PrimaryKey String EPC;
    String estado;
    String nombre;
    String talla;
    int    itemEsperados;
    int    itemEncontrados;
    String rutaImagen;
    int estatus = ObjectItem.ESTADO_ESPERANDO;
    boolean skuCompleto = false;

    public boolean isSkuCompleto() {
        return skuCompleto;
    }

    public static synchronized void setSkuCompleto(ObjectItem item) {
        for (ObjectItem items : mListaItem){
            if(items.getSKU().equals(item.getSKU())){
                items.setSkuCompleto(true);
            }
        }
    }

    private void setSkuCompleto(boolean status) {
        this.skuCompleto = status;
    }

    public static ArrayList<ObjectItem> getChildren() {
        return children;
    }

    public static void setChildren(ArrayList<ObjectItem> children) {
        ObjectItem.children = children;
    }

    public static ArrayList<ObjectItem> children;

    public static final int ESTADO_ESPERANDO  = 1,
                               ESTADO_ENCONTRADO = 2,
                               ESTADO_NO_ESTA    = 3,
                               ESTADO_TAG_DAÑADO = 4;

    static ArrayList<ObjectItem> mListaItem = new ArrayList<>();

    public ObjectItem() {
    }

    public ObjectItem(String EPC) {
        this.EPC = EPC;
    }

    public ObjectItem(String SKU, String nombre, String talla, int itemEsperados, int itemEncontrados, String rutaImagen, int estatus, String epc) {
        this.SKU            = SKU;
        this.nombre         = nombre;
        this.talla          = talla;
        this.itemEsperados  = itemEsperados;
        this.itemEncontrados = itemEncontrados;
        this.rutaImagen     = rutaImagen;
        this.estatus        = estatus;
        this.EPC            = epc;
    }

    public ObjectItem(int id, String SKU, String ubicacion, String epc, String estado) {
        this.id = id;
        this.SKU = SKU;
        this.ubicacion = ubicacion;
        this.EPC = epc;
        this.estado = estado;
        this.itemEsperados  = 0;
        this.itemEncontrados = 0;
        this.rutaImagen     = "";
        this.estatus        = ObjectItem.ESTADO_ESPERANDO;
        this.nombre         = "";
        this.talla          = "";
    }

    public ObjectItem(String SKU, String ubicacion, String EPC, String estado, String nombre, String talla, int itemEsperados, int itemEncontrados, String rutaImagen, int estatus) {
        this.SKU = SKU;
        this.ubicacion = ubicacion;
        this.EPC = EPC;
        this.estado = estado;
        this.nombre = nombre;
        this.talla = talla;
        this.itemEsperados = itemEsperados;
        this.itemEncontrados = itemEncontrados;
        this.rutaImagen = rutaImagen;
        this.estatus = estatus;
    }

    public void setItemEncontrados(int itemEncontrados) {
        this.itemEncontrados = itemEncontrados;
    }

    public ObjectItem(String SKU, String nombre, String talla, int itemEsperados, int itemEncontrados, String rutaImagen, int estatus) {
        this.SKU = SKU;
        this.nombre = nombre;
        this.talla = talla;
        this.itemEsperados = itemEsperados;
        this.itemEncontrados = itemEncontrados;
        this.rutaImagen = rutaImagen;
        this.estatus = estatus;
    }

    public int getId() {
        return id;
    }

    public String getSKU() {
        return SKU;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public String getEpc() {
        return EPC;
    }

    public String getEstado() {
        return estado;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTalla() {
        return talla;
    }

    public int getItemEsperados() {
        return itemEsperados;
    }

    public int getItemEncontrados() {
        return itemEncontrados;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public int getEstatus() {
        return estatus;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public static void setItems(ArrayList<ObjectItem> items){
        mListaItem.clear();
        mListaItem.addAll(items);
    }

    public static void setItem(ObjectItem item){
        mListaItem.clear();
        mListaItem.add(item);
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public static ObjectItem getItem(int id) throws Exception {
        return mListaItem.get(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.SKU);
        dest.writeString(this.ubicacion);
        dest.writeString(this.EPC);
        dest.writeString(this.estado);
        dest.writeString(this.nombre);
        dest.writeString(this.talla);
        dest.writeInt(this.itemEsperados);
        dest.writeInt(this.itemEncontrados);
        dest.writeString(this.rutaImagen);
        dest.writeInt(this.estatus);
    }

    protected ObjectItem(Parcel in) {
        this.id = in.readInt();
        this.SKU = in.readString();
        this.ubicacion = in.readString();
        this.EPC = in.readString();
        this.estado = in.readString();
        this.nombre = in.readString();
        this.talla = in.readString();
        this.itemEsperados = in.readInt();
        this.itemEncontrados = in.readInt();
        this.rutaImagen = in.readString();
        this.estatus = in.readInt();
    }

    public static final Creator<ObjectItem> CREATOR = new Creator<ObjectItem>() {
        public ObjectItem createFromParcel(Parcel source) {
            return new ObjectItem(source);
        }

        public ObjectItem[] newArray(int size) {
            return new ObjectItem[size];
        }
    };

    public void setEPC(String EPC) {
        this.EPC = EPC;
    }

    @Override
    public String toString() {
        return "ObjectItem{" +
                "id=" + id +
                ", SKU='" + SKU + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", EPC='" + EPC + '\'' +
                ", estado='" + estado + '\'' +
                ", nombre='" + nombre + '\'' +
                ", talla='" + talla + '\'' +
                ", itemEsperados=" + itemEsperados +
                ", itemEncontrados=" + itemEncontrados +
                ", rutaImagen='" + rutaImagen + '\'' +
                ", estatus=" + estatus +
                '}';
    }

    public void setRutaImagen(String rutaImagen) {

        this.rutaImagen = rutaImagen;
    }

    public static String getEstatus(int estatus) {
        String estado = "";
        switch (estatus) {
            case 1 :
                estado = "Esperado";
                break;

            case 2 :
                estado = "Encontrado";
                break;

            case 3 :
                estado = "No está";
                break;

            case 4 :
                estado = "Tag dañado";
                break;
        }
        return estado;
    }

    public synchronized static ArrayList<ObjectItem> getmListaItem() {
        if (mListaItem.size() == 0) {
                fillListaItem(null);
        }
        return mListaItem;
    }

    public synchronized  static ArrayList<ObjectItem> getmListaItemEsperado() {
//        if (mListaItem.size() == 0) {
//            fillListaItem();
//        }
        ArrayList<ObjectItem> itemEncontrados = new ArrayList<>();

        for (ObjectItem item : mListaItem){
            if (item.getEstatus() != ESTADO_ENCONTRADO){
                itemEncontrados.add(item);
            }
        }
        return itemEncontrados;
    }

    public synchronized static ArrayList<ObjectItem> getmListaItemEncontrados() {
//        if (mListaItem.size() == 0) {
//            fillListaItem();
//        }
        ArrayList<ObjectItem> itemEncontrados = new ArrayList<>();

        for (ObjectItem item : getmListaItem()){
            if (item.getEstatus() == ESTADO_ENCONTRADO){
                itemEncontrados.add(item);
            }
        }
        return itemEncontrados;
    }

    public synchronized static ArrayList<ObjectItem> getmListaItemPerdidos() {
//        if (mListaItem.size() == 0) {
//            fillListaItem();
//        }
        ArrayList<ObjectItem> itemEncontrados = new ArrayList<>();

        for (ObjectItem item : getmListaItem()){
            if (item.getEstatus() == ESTADO_NO_ESTA || item.getEstatus() == ESTADO_ESPERANDO){
                itemEncontrados.add(item);
            }
        }
        return itemEncontrados;
    }

    public synchronized static ArrayList<ObjectItem> getmListaItemDaniados() {
//        if (mListaItem.size() == 0) {
//            fillListaItem();
//        }
        ArrayList<ObjectItem> itemEncontrados = new ArrayList<>();

        for (ObjectItem item : getmListaItem()){
            if (item.getEstatus() == ESTADO_TAG_DAÑADO){
                itemEncontrados.add(item);
            }
        }
        return itemEncontrados;
    }

    public static void fillListaItem(List<ObjectItem> items) {
        if (null != mListaItem){
//            mListaItem.addAll(items);
            //SKU, nombre, talla, itemEsperados, itemEncontrados, rutaImagen, Estatus, EPC
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E280116060000205079084F8"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790B858"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790B839"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E28011606000020507906AE8"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E280116060000205079048C8"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790B8F9"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790DA38"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790DA49"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E280116060000205079084E9"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E28011606000020507906A68"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E28011606000020507906AA8"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790B8B8"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790B809"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790B8B9"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790DA19"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790B818"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790DA39"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E28011606000020507908469"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790B819"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E280116060000205079048D8"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E28011606000020507908408"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E280116060000205079048F8"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790B848"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790DA08"));
            mListaItem.add(new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E2801160600002050790DA69"));



            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507906A48 "));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E280116060000205079084A8"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507908439"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B829"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507906AD8"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B8E8"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B8E9"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790DA79"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B889"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507908478"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B8A9"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507908449"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B879"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B8F8"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790DA58"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E280116060000205079084D9"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507906A78"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B868"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B838"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507906AF9"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "1111222233334444DDDDCCCC"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507908498"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E280116060000205079084C9"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B869"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B828"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507906AF8"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507906A98"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507908428"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507908409"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507908479"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790DA78"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790DA28"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B878"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E280116060000205079048E8"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507908418"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B8A8"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790DA59"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E280116060000205079084A9"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507908429"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B8D9"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507908448"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E280116060000205079084B8"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790DA68"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790DA29"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B898"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507906A08"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E2801160600002050790B899"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507908459"));
            mListaItem.add(new ObjectItem("1527410740", "Camisa",   "M",  49, 0, "http://www.camisetas-barcelona.com/img/Camisetas.png",                                                 1, "E28011606000020507908438"));


            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E2801160600002050790B8C8"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E2801160600002050790DA48"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "BEEF11606000020507906A28"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E28011606000020507906A18"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E28011606000020507908499"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E28011606000020507908468"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E28011606000020507906AB8"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E28011606000020507906A38"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E2801160600002050790DA18"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E280116060000205079084E8"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E2801160600002050790B808"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E280116060000205079084D8"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E28011606000020507908458"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E2801160600002050790B8C9"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E28011606000020507908419"));
            mListaItem.add(new ObjectItem("1945446608", "Medias",   "32", 16, 0, "http://www.futboladicto.com/wp-content/uploads/2010/05/medias-barcelona-home-2010-2011.jpg",           1, "E2801160600002050790DA09"));



            mListaItem.add(new ObjectItem("2097935529", "Jean",     "30",  9, 0,  "https://s3-eu-west-1.amazonaws.com/iouproyect/photos/items/a1I20000000cYb9EAEphoto.jpg",              1, "E28011606000020507906A58"));
            mListaItem.add(new ObjectItem("2097935529", "Jean",     "30",  9, 0,  "https://s3-eu-west-1.amazonaws.com/iouproyect/photos/items/a1I20000000cYb9EAEphoto.jpg",              1, "E280116060000205079084B9"));
            mListaItem.add(new ObjectItem("2097935529", "Jean",     "30",  9, 0,  "https://s3-eu-west-1.amazonaws.com/iouproyect/photos/items/a1I20000000cYb9EAEphoto.jpg",              1, "E28011606000020507906AC8"));
            mListaItem.add(new ObjectItem("2097935529", "Jean",     "30",  9, 0,  "https://s3-eu-west-1.amazonaws.com/iouproyect/photos/items/a1I20000000cYb9EAEphoto.jpg",              1, "E280116060000205079084C8"));
            mListaItem.add(new ObjectItem("2097935529", "Jean",     "30",  9, 0,  "https://s3-eu-west-1.amazonaws.com/iouproyect/photos/items/a1I20000000cYb9EAEphoto.jpg",              1, "E280116060000205079048B8"));
            mListaItem.add(new ObjectItem("2097935529", "Jean",     "30",  9, 0,  "https://s3-eu-west-1.amazonaws.com/iouproyect/photos/items/a1I20000000cYb9EAEphoto.jpg",              1, "E2801160600002050790B8D8"));
            mListaItem.add(new ObjectItem("2097935529", "Jean",     "30",  9, 0,  "https://s3-eu-west-1.amazonaws.com/iouproyect/photos/items/a1I20000000cYb9EAEphoto.jpg",              1, "E2801160600002050790B849"));
            mListaItem.add(new ObjectItem("2097935529", "Jean",     "30",  9, 0,  "https://s3-eu-west-1.amazonaws.com/iouproyect/photos/items/a1I20000000cYb9EAEphoto.jpg",              1, "E28011606000020507908489"));
            mListaItem.add(new ObjectItem("2097935529", "Jean",     "30",  9, 0,  "https://s3-eu-west-1.amazonaws.com/iouproyect/photos/items/a1I20000000cYb9EAEphoto.jpg",              1, "E2801160600002050790B859"));
        }
    }

    public void setEstatus(int estatus) {
        this.estatus = estatus;
    }

    public static synchronized int getTamanioItemsEncontrados(){
        return getmListaItemEncontrados().size();
    }

    public static synchronized int getTamanioItemsPerdidos(){

        return getmListaItemPerdidos().size();
    }

    public static synchronized int getTamanioItemsDaniados(){
        return getmListaItemDaniados().size();
    }

    public static void changeEstatusItem(ObjectItem item, int pos, int tipo){
        try{
            for(ObjectItem items : getmListaItem()){
                if (items.getEpc().equals(item.getEpc())){
                    items.setEstatus(tipo);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void changeEstatusSkuCompleto(ObjectItem item){
        try{
            for(ObjectItem items : getmListaItem()){
                if (items.getSKU().equals(item.getSKU())){
                    items.setSkuCompleto(true);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
