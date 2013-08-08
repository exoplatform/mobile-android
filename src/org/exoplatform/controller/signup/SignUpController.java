package org.exoplatform.controller.signup;


// this sign up controller will handle the making request and response to
// the cloud workspace

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.exoplatform.R;
import org.exoplatform.ui.SignInActivity;
import org.exoplatform.ui.SignUpActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class SignUpController {

  private Context    mContext;

  private SignUpActivity mSignUpActivity;

  private String     mEmail;

  private SignUpTask mSignUpTask;

  private SignUpWaitingDialog mProgressDialog;

  private Resources mResource;

  private HttpResponse mResponse;

  private SignUpWarningDialog dialog;

  /* Sign Up messages */
  private String signUpMess;

  private String warningTitle;

  private String invalidMess;

  private String wrongEmailDomainMess;

  private String accountExistsMess;

  private String maxUsersMess;

  private String OkMess;

  private static final String TAG = "eXoSignUpController";


  public SignUpController(SignUpActivity context, String email) {
    mContext = context;
    mSignUpActivity = context;
    SettingUtils.setDefaultLanguage(mContext);
    mResource = mContext.getResources();
    mEmail = email;

    getSignUpMessages();

    onLoad();
  }

  private void onLoad() {
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mSignUpTask == null || mSignUpTask.getStatus() == SignUpTask.Status.FINISHED) {
        mSignUpTask = (SignUpTask) new SignUpTask().execute();
      }

    } else {
      new ConnectionErrorDialog(mContext).show();
    }
  }

  private void getSignUpMessages() {
    signUpMess   = mResource.getString(R.string.SigningUp);
    warningTitle = mResource.getString(R.string.Warning);
    invalidMess  = mResource.getString(R.string.InvalidSignUp);
    OkMess       = mResource.getString(R.string.OK);

    wrongEmailDomainMess = mResource.getString(R.string.WrongEmailDomain);
    accountExistsMess    = mResource.getString(R.string.AccountExists);
    maxUsersMess         = mResource.getString(R.string.MaxUsers);
  }

  public class SignUpTask extends AsyncTask<Void, Void, Integer> {

    @Override
    public void onPreExecute() {
      mProgressDialog = new SignUpWaitingDialog(mContext, null, signUpMess);
      mProgressDialog.show();
    }

    @Override
    public Integer doInBackground(Void... params) {

      try {
        mResponse = ExoConnectionUtils.makeCloudSignUpRequest(mEmail);
        // response 400 - invalid format for email address - do not handle
        return ExoConnectionUtils.checkSignUpResponse(mResponse);
      } catch (UnsupportedEncodingException e) {
        Log.i(TAG, "UnsupportedEncodingException");
        return ExoConnectionUtils.SIGNUP_INVALID;
      } catch (ClientProtocolException e) {
        Log.i(TAG, "ClientProtocolException");
        return ExoConnectionUtils.SIGNUP_INVALID;
      } catch (IOException e) {
        Log.i(TAG, "IOException");
        return ExoConnectionUtils.SIGNUP_INVALID;
      }
    }

    @Override
    public void onPostExecute(Integer result) {

      View.OnClickListener closeDialog = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          dialog.dismiss();
        }
      };

      switch (result) {
        case ExoConnectionUtils.SIGNUP_INVALID:
          dialog = new SignUpWarningDialog(mContext, warningTitle, invalidMess, OkMess);
          dialog.getOkButton().setOnClickListener(closeDialog);
          dialog.show();
          break;
        case ExoConnectionUtils.SIGNUP_WRONG_DOMAIN:
          dialog = new SignUpWarningDialog(mContext, warningTitle, wrongEmailDomainMess, OkMess);
          dialog.getOkButton().setOnClickListener(closeDialog);
          dialog.show();
          break;
        case ExoConnectionUtils.SIGNUP_ACCOUNT_EXISTS:
          dialog = new SignUpWarningDialog(mContext, warningTitle, accountExistsMess, OkMess);
          dialog.show();
          dialog.getOkButton().setOnClickListener(new Button.OnClickListener() {

            public void onClick(View view) {
              dialog.dismiss();
              // fires up activity for log in screen
              Intent next = new Intent(mContext, SignInActivity.class);
              next.putExtra(ExoConstants.EXO_EMAIL, mEmail);
              mContext.startActivity(next);
            }
          });
          break;
        case ExoConnectionUtils.SIGNUP_MAX_USERS:
          dialog = new SignUpWarningDialog(mContext, warningTitle, maxUsersMess, OkMess);
          dialog.getOkButton().setOnClickListener(closeDialog);
          dialog.show();
          new CreatingMarketoTask().execute();
          break;
        case ExoConnectionUtils.SIGNUP_OK:
          // swipe view to account creation in progress
          mSignUpActivity.flipToGreetingsPanel();
          new CreatingMarketoTask().execute();
          break;
      }

      mProgressDialog.dismiss();
    }
  }


  public class CreatingMarketoTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
      HttpResponse response;
      try {
        response = ExoConnectionUtils.requestCreatingMarketo(mEmail);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY || statusCode == HttpStatus.SC_OK)
          Log.d(TAG, "creating marketo for " + mEmail + " ok");
        else Log.d(TAG, "creating marketo fails");
      } catch (UnsupportedEncodingException e) {
        Log.d(TAG, "UnsupportedEncodingException: " + e.getLocalizedMessage());
      } catch (ClientProtocolException e) {
        Log.d(TAG, "ClientProtocolException: " + e.getLocalizedMessage());
      } catch (IOException e) {
        Log.d(TAG, "IOException: " + e.getLocalizedMessage());
      }

      return null;
    }
  }

  private class SignUpWaitingDialog extends WaitingDialog {

    public SignUpWaitingDialog(Context context, String titleString, String contentString) {
      super(context, titleString, contentString);
    }

    @Override
    public void onBackPressed() {
      super.onBackPressed();
      //onCancelLoad();
    }

  }

  private class SignUpWarningDialog extends WarningDialog {

    public SignUpWarningDialog(Context context, String titleString, String contentString, String okString) {
      super(context, titleString, contentString, okString);
    }

    public Button getOkButton() {
      return (Button) findViewById(R.id.warning_ok_button);
    }
  }
}
