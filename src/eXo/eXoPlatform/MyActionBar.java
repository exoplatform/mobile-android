package eXo.eXoPlatform;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

public class MyActionBar extends RelativeLayout {

  private ImageButton btnHomeBack;
    private ImageButton btn1;
    private ImageButton btn0;
    private TextView tvTitle;
    private String strTitle;

    public MyActionBar(Context context, AttributeSet attr) {
        super(context, attr);
        initializeLayoutBasics(context);
        retrieveLabelString(context, attr);
    }

    private void initializeLayoutBasics(Context context) {
//        setOrientation(HORIZONTAL);
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.myactionbar, this);
    }

    private void retrieveLabelString(Context context, AttributeSet attr) {
      
        this.btnHomeBack = (ImageButton) findViewById(R.id.Button_Home_Back);
        this.btn1 = (ImageButton) findViewById(R.id.Button_1);
        this.btn0 = (ImageButton) findViewById(R.id.Button_0);
        
        this.tvTitle = (TextView) findViewById(R.id.TextView_Title);
        
//        final TypedArray a = context.obtainStyledAttributes(attr, R.styleable.LabeledNumberInput);
//        this.labelStr = a.getString(R.styleable.LabeledNumberInput_label);
//        this.labelText.setText(this.labelStr);
    }

    

}
