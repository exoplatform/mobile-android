package org.exoplatform.ui.login;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;
import org.exoplatform.R;
import org.exoplatform.controller.login.ServerAdapter;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;

import java.util.ArrayList;

/**
 * Represents the server panel in login screen
 * contains a list of server
 *
 */
public class ServerPanel extends LinearLayout implements AdapterView.OnItemClickListener {

  private Context        mContext;

  private AccountSetting mSetting;

  /** list view that contains list of servers */
  private ListView       mServerListView;

  public ServerPanel(Context context) {
    super(context);
    mContext = context;
  }

  public ServerPanel(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    mSetting = AccountSetting.getInstance();

    initSubViews();
  }

  private void initSubViews() {

    mServerListView = (ListView) findViewById(R.id.ListView_Servers);
    mServerListView.setCacheColorHint(Color.TRANSPARENT);
    mServerListView.setFadingEdgeLength(0);
    mServerListView.setDivider(null);
    mServerListView.setDividerHeight(1);

    mServerListView.setOnItemClickListener(this);
    mServerListView.setAdapter(new ServerAdapter(mContext));
  }

  public void turnOn() {
    setVisibility(View.VISIBLE);
    mServerListView.setVisibility(View.VISIBLE);
  }

  public void repopulateServerList() {
    mServerListView.setAdapter(new ServerAdapter(mContext));
  }

  public void turnOff() {
    setVisibility(View.INVISIBLE);
    mServerListView.setVisibility(View.INVISIBLE);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View rowView, int position, long id) {

    int selectedIdx = Integer.valueOf(mSetting.getDomainIndex());

    if (selectedIdx == position) {

      rowView.findViewById(R.id.ImageView_Checked)
             .setBackgroundResource(R.drawable.authenticate_checkmark_off);
      mSetting.setDomainIndex(String.valueOf(-1));
      mSetting.setCurrentServer(null);
      return ;
    }

    int firstVisiblePosition = parent.getFirstVisiblePosition();
    if ((firstVisiblePosition <= selectedIdx) && (selectedIdx <= parent.getLastVisiblePosition()))
      parent.getChildAt(selectedIdx - firstVisiblePosition)
          .findViewById(R.id.ImageView_Checked)
          .setBackgroundResource(R.drawable.authenticate_checkmark_off);

    rowView.findViewById(R.id.ImageView_Checked)
           .setBackgroundResource(R.drawable.authenticate_checkmark_on);

    ArrayList<ServerObjInfo> serverList = ServerSettingHelper.getInstance().getServerInfoList();
    mSetting.setDomainIndex(String.valueOf(position));
    mSetting.setCurrentServer(serverList.get(position));
  }

}
