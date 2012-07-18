package org.exoplatform.widget;

import org.exoplatform.R;
import org.exoplatform.utils.CompatibleFileOpen;
import org.exoplatform.utils.ExoDocumentUtils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CompatibleFileOpenDialog extends Dialog implements android.view.View.OnClickListener {

  private Button  okButton;

  private Button  cancelButton;

  private Context mContext;

  private String  fileType;

  private String  filePath;

  private String  fileName;

  public CompatibleFileOpenDialog(Context context, String fType, String fPath, String fName) {
    super(context);
    mContext = context;
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.compatible_open_file_dialog_layout);
    TextView titleView = (TextView) findViewById(R.id.com_dialog_title_view);
    titleView.setText(fName);
    TextView contentView = (TextView) findViewById(R.id.com_warning_content);
    contentView.setText(context.getResources().getString(R.string.CompatibleFileSuggest));
    okButton = (Button) findViewById(R.id.com_ok_button);
    okButton.setOnClickListener(this);
    cancelButton = (Button) findViewById(R.id.com_cancel_button);
    cancelButton.setOnClickListener(this);
    ImageView iconView = (ImageView) findViewById(R.id.com_warning_image);
    iconView.setBackgroundResource(ExoDocumentUtils.getIconFromType(fType));
    fileType = fType;
    filePath = fPath;
    fileName = fName;
  }

  public void onClick(View view) {
    if (view.equals(cancelButton)) {
      dismiss();
    }
    if (view.equals(okButton)) {
      new CompatibleFileOpen(mContext, fileType, filePath, fileName);
      dismiss();
    }
  }

}
