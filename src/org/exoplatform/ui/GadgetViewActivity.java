package org.exoplatform.ui;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.exoplatform.R;
import org.exoplatform.controller.ExoApplicationsController;
import org.exoplatform.model.GadgetInfo;
import org.exoplatform.model.GateInDbItem;
import org.exoplatform.singleton.LocalizationHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//Gadget view controller
public class GadgetViewActivity extends Activity {
  /** Called when the activity is first created. */

  private Button                          _btnClose;                      // Close

  // button

  private Button                          _btnLanguageHelp;               // Setting

  // button

  private TextView                        _txtvTitleBar;                  // Gadget

  // title

  private ListView                        _lstvGadgets;                   // Gadget

  // list view

  // Localization strings

  private String                          strConnectionTimedOut;

  public static GadgetViewActivity      eXoGadgetViewControllerInstance; // Instance

  public static ExoApplicationsController _delegate;                      // Main
                                                                           // app

  // view
  // controller

  public static GadgetInfo                currentGadget;                  // Current

  // gadget

  public static Cookie                    cookie = null;                  // Cookie

  // Constructor
  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.exogadgetview);

    eXoGadgetViewControllerInstance = this;

    _btnClose = (Button) findViewById(R.id.Button_Close);
    _btnClose.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        GadgetViewActivity.this.finish();
      }
    });

    _btnLanguageHelp = (Button) findViewById(R.id.Button_Language_Help);
    _btnLanguageHelp.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        // eXoLanguageSettingDialog customizeDialog = new
        // eXoLanguageSettingDialog(eXoGadgetViewController.this, 6,
        // eXoGadgetViewControllerInstance);
        // customizeDialog.show();

      }
    });

    _txtvTitleBar = (TextView) findViewById(R.id.TextView_TitleBar);

    _lstvGadgets = (ListView) findViewById(R.id.ListView_Gadgets);

    BaseAdapter adapter = new BaseAdapter() {

      public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = eXoGadgetViewControllerInstance.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.rowinlistview, parent, false);

        GateInDbItem gadgetTab = ExoApplicationsController.gadgetTab;
        if (gadgetTab._arrGadgetsInItem == null)
          return rowView;

        _txtvTitleBar.setText(gadgetTab._strDbItemName);
        final GadgetInfo gadget = gadgetTab._arrGadgetsInItem.get(position);

        rowView.setOnClickListener(new View.OnClickListener() {

          public void onClick(View v) {

            ExoApplicationsController.webViewMode = 0;
            currentGadget = gadget;
            DefaultHttpClient client = new DefaultHttpClient();

            HttpGet get = new HttpGet(currentGadget._strGadgetUrl);
            try {
              HttpResponse response = client.execute(get);
              int status = response.getStatusLine().getStatusCode();
              if (status < 200 || status >= 300) {
                Toast.makeText(GadgetViewActivity.this, strConnectionTimedOut, Toast.LENGTH_LONG)
                     .show();
                return;
              }
            } catch (Exception e) {

              return;
            }

            Intent next = new Intent(GadgetViewActivity.this, WebViewActivity.class);
            GadgetViewActivity.this.startActivity(next);
            finish();
          }
        });

        TextView label = (TextView) rowView.findViewById(R.id.label);
        label.setText(gadget._strGadgetName);
        TextView description = (TextView) rowView.findViewById(R.id.description);
        description.setText(gadget._strGadgetDescription);
        ImageView icon = (ImageView) rowView.findViewById(R.id.icon);
        icon.setImageBitmap(gadget._btmGadgetIcon);

        return (rowView);
      }

      public long getItemId(int arg0) {

        return arg0;
      }

      public Object getItem(int arg0) {

        return arg0;
      }

      public int getCount() {

        return ExoApplicationsController.gadgetTab._arrGadgetsInItem.size();
      }
    };
    if (ExoApplicationsController.gadgetTab._arrGadgetsInItem != null)
      _lstvGadgets.setAdapter(adapter);

    changeLanguage();

  }

  @Override
  public void onBackPressed() {
    finish();
  }

  // Set language
  public void changeLanguage() {
    LocalizationHelper local = LocalizationHelper.getInstance();

    String strClose = "";

    strClose = local.getString("CloseButton");

    strConnectionTimedOut = local.getString("ConnectionTimedOut");

    _btnClose.setText(strClose);
  }
}
