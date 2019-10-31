package smartobjects.com.smobapp.customViews;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import smartobjects.com.smobapp.R;

/**
 * Created by Andres Rubiano on 30/10/2015.
 */
public class CustomConfirmOkDialog extends Dialog implements View.OnClickListener
{
    private Button okButton = null;
    private TextView infoText=null,confirmBody=null;
    private int errorMessage=0;
    private Activity activity;

    public CustomConfirmOkDialog(Activity context)
    {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.confirm_ok);
        this.activity = context;
        initControls();
    }

    private void initControls()
    {
        okButton = (Button) findViewById(R.id.button);
        okButton.setOnClickListener(this);

        infoText = (TextView)findViewById(R.id.textView3);

    }
    public void onClick(View v)
    {
        dismiss();
    }
}