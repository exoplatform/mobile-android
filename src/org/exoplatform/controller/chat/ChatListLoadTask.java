package org.exoplatform.controller.chat;

import greendroid.util.Config;

import java.net.URI;

import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ChatServiceHelper;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.content.Context;
import android.util.Log;

public class ChatListLoadTask extends UserTask<Void, Void, Boolean> {
  private String                loadingData;

  private String                okString;

  private String                titleString;

  private String                contentString;

  private Context               mContext;

  private ChatListController    chatListController;

  private ChatListWaitingDialog _progressDialog;

  public ChatListLoadTask(Context context, ChatListController controller) {
    mContext = context;
    chatListController = controller;
    changeLanguage();
  }

  @Override
  public void onPreExecute() {
    _progressDialog = new ChatListWaitingDialog(mContext, null, loadingData);
    _progressDialog.show();
  }

  @Override
  public Boolean doInBackground(Void... params) {
    XMPPConnection connection = ChatServiceHelper.getInstance().getXMPPConnection();
    if (connection != null && connection.isConnected()) {
      return true;
    }
    try {
      AccountSetting acc = AccountSetting.getInstance();
      String urlStr = acc.getDomainName();

      URI url = null;

      try {
        url = new URI(urlStr);
      } catch (Exception e) {
        return false;
      }

      String userName = acc.getUsername();
      String password = acc.getPassword();

      ConnectionConfiguration config = new ConnectionConfiguration(url.getHost(), 5222, "Work");
      connection = new XMPPConnection(config);
      connection.connect();
      connection.login(userName, password);
      ChatServiceHelper.getInstance().setXMPPConnection(connection);
      return true;
    } catch (XMPPException e) {
    	if (Config.GD_INFO_LOGS_ENABLED)
    		Log.i("XMPPException", e.getMessage());
    	
      connection.disconnect();
      connection = null;
      return false;
    }

  }

  @Override
  public void onPostExecute(Boolean result) {
    _progressDialog.dismiss();
    if (result == true) {
      chatListController.init();
    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }

  }

  private void changeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    loadingData = location.getString("LoadingData");
    okString = location.getString("OK");
    titleString = location.getString("Warning");
    contentString = location.getString("ConnectionError");
  }

  private class ChatListWaitingDialog extends WaitingDialog {

    public ChatListWaitingDialog(Context context, String titleString, String contentString) {
      super(context, titleString, contentString);
    }

    @Override
    public void onBackPressed() {
      super.onBackPressed();
      chatListController.onCancelLoad();
    }
  }
}
