package smartobjects.com.smobapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import smartobjects.com.smobapp.connectivity.MainThread;
import smartobjects.com.smobapp.fragments.FragmentListProducto;
import smartobjects.com.smobapp.fragments.FragmentProgressBar;
import smartobjects.com.smobapp.objects.ObjectProducto;
import smartobjects.com.smobapp.utils.BaseActivity;
import smartobjects.com.smobapp.utils.UtilsConstants;

public class ActivityConsultarProducto extends BaseActivity
        implements FragmentListProducto.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_producto);

        FragmentProgressBar progressBar = FragmentProgressBar.newInstance(null);
        ActivityConsultarProducto.this.getFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_activity_consultar_producto, progressBar).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if (null != ActivityConsultarProducto.this) {
                    ActivityConsultarProducto.this.finish();
                }
            }
        };

        showAlertToCartones("Producto",
                "Por favor, ingrese el SKU:", listener);
    }

    protected void showAlertToCartones(final CharSequence title, final CharSequence mensaje,
                                       final DialogInterface.OnClickListener viewActionNegative) {
        //Obtenemos el alertDialog
        final AlertDialog.Builder alertDialog = getAlertDialog(title, mensaje);
        //Obtenemos el EditText
        final EditText mEditText = getEditText();
        if (null != alertDialog) {
            if (null != mEditText) {
                alertDialog.setView(mEditText);

                alertDialog.setPositiveButton(R.string.st_general_aceptar, new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (mEditText != null) {
                                String sku = mEditText.getText().toString();
                                if (sku.length() > 0) {
                                    MainThread mMainThread = new MainThread(ActivityConsultarProducto.this,
                                            UtilsConstants.PROGRESS_DIALOG.DOWNLOAD_PRODUCTS_DETAIL,
                                            R.id.frame_layout_activity_consultar_producto);
                                    mMainThread.execute(UtilsConstants.PRODUCTO.JSON_PRODUCT_BY_SKU,
                                            sku);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                alertDialog.setNegativeButton(R.string.st_general_cancelar, viewActionNegative);

                AlertDialog mAlert = alertDialog.create();
                mAlert.show();
            }
        }
    }

    public EditText getEditText() {
        final EditText mEditText = new EditText(ActivityConsultarProducto.this);
//        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
//                | InputType.TYPE_NUMBER_FLAG_SIGNED);
//        mEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED
//                | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        return mEditText;
    }

    @Override
    public void onFragmentInteraction(int id) {
        try {
            ObjectProducto producto = ObjectProducto.getProducto(id);
            Bundle bundle = new Bundle();
            Intent intent = new Intent(ActivityConsultarProducto.this, ActivityProductDetail.class);
            intent.putExtra(UtilsConstants.PRODUCTO.BUNDLE_ID, producto.getId());
            intent.putExtra(UtilsConstants.PRODUCTO.BUNDLE_SKU, producto.getSKU());
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
