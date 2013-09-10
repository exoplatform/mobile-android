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

  private SignUpWarningDialog mWarningDialog;

  /* Sign Up messages */
  private String signUpMess;

  private String warningTitle;

  private String invalidMess;

  private String wrongEmailDomainMess;

  private String accountExistsMess;

  private String maxUsersMess;

  private String OkMess;

  private String serverNotAvailableMess;

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
      new SignUpWarningDialog(mContext, warningTitle, serverNotAvailableMess, OkMess).show();
    }
  }

  private void getSignUpMessages() {
    signUpMess   = mResource.getString(R.string.SigningUp);
    warningTitle = mResource.getString(R.string.Warning);
    invalidMess  = mResource.getString(R.string.InvalidSignUp);
    OkMess       = mResource.getString(R.string.OK);

    wrongEmailDomainMess   = mResource.getString(R.string.WrongEmailDomain);
    accountExistsMess      = mResource.getString(R.string.AccountExists);
    maxUsersMess           = mResource.getString(R.string.MaxUsers);
    serverNotAvailableMess = mResource.getString(R.string.ServerNotAvailable);
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
        return ExoConnectionUtils.checkSignUpResponse(mResponse, mEmail);
      } catch (UnsupportedEncodingException e) {
        Log.i(TAG, "UnsupportedEncodingException");
        return ExoConnectionUtils.SIGNUP_SERVER_NAV;
      } catch (ClientProtocolException e) {
        Log.i(TAG, "ClientProtocolException");
        return ExoConnectionUtils.SIGNUP_SERVER_NAV;
      } catch (IOException e) {
        Log.i(TAG, "IOException");
        /** can not contact server, probably down or wrong address */
        return ExoConnectionUtils.SIGNUP_SERVER_NAV;
      }
    }

    @Override
    public void onPostExecute(Integer result) {

      View.OnClickListener closeDialog = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mWarningDialog.dismiss();
        }
      };

      switch (result) {
        case ExoConnectionUtils.SIGNUP_INVALID:
          mWarningDialog = new SignUpWarningDialog(mContext, warningTitle, invalidMess, OkMess);
          mWarningDialog.getOkButton().setOnClickListener(closeDialog);
          mWarningDialog.show();
          break;
        case ExoConnectionUtils.SIGNUP_WRONG_DOMAIN:
          mWarningDialog = new SignUpWarningDialog(mContext, warningTitle, wrongEmailDomainMess, OkMess);
          mWarningDialog.getOkButton().setOnClickListener(closeDialog);
          mWarningDialog.show();
          break;
        case ExoConnectionUtils.SIGNUP_ACCOUNT_EXISTS:
          mWarningDialog = new SignUpWarningDialog(mContext, warningTitle, accountExistsMess, OkMess);
          mWarningDialog.show();
          mWarningDialog.getOkButton().setOnClickListener(new Button.OnClickListener() {

            public void onClick(View view) {
              mWarningDialog.dismiss();
              // fires up activity for log in screen
              Intent next = new Intent(mContext, SignInActivity.class);
              next.putExtra(ExoConstants.EXO_EMAIL, mEmail);
              mContext.startActivity(next);
            }
          });
          break;
        case ExoConnectionUtils.SIGNUP_SERVER_NAV:
          mWarningDialog = new SignUpWarningDialog(mContext, warningTitle, serverNotAvailableMess, OkMess);
          mWarningDialog.getOkButton().setOnClickListener(closeDialog);
          mWarningDialog.show();
          break;
        case ExoConnectionUtils.SIGNUP_MAX_USERS:
          mWarningDialog = new SignUpWarningDialog(mContext, warningTitle, maxUsersMess, OkMess);
          mWarningDialog.getOkButton().setOnClickListener(closeDialog);
          mWarningDialog.show();
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
