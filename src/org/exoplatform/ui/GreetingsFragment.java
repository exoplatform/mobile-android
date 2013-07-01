package org.exoplatform.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import org.exoplatform.R;

public class GreetingsFragment extends Fragment {

  private Button mCheckMailBtn;

  private static final String TAG = "eXoGreetingsFragment";

  public GreetingsFragment() {}

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {


    View layout = inflater.inflate(R.layout.greetings_panel, container, false);
    mCheckMailBtn = (Button) layout.findViewById(R.id.signup_check_mail_btn);
    mCheckMailBtn.setOnClickListener(onClickCheckMail());

    return layout;
  }


  private View.OnClickListener onClickCheckMail() {
    return new View.OnClickListener() {

      @Override
      public void onClick(View view) {
        Log.i(TAG, "onClickCheckMail");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("mailto:?subject=&body=");
        intent.setData(data);
        startActivity(intent);
      }
    };
  }
}
