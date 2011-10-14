package org.exoplatform.controller.home;

import java.util.ArrayList;

import org.exoplatform.model.HomeItem;
import org.exoplatform.singleton.HomeHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class HomeAdapter extends BaseAdapter {
  private Context             mContext;

  private ArrayList<HomeItem> homeItemList;

  public HomeAdapter(Context context) {
    mContext = context;
    homeItemList = HomeHelper.getInstance().getHomeItemList();
  }

//  @Override
  public int getCount() {
    return homeItemList.size();
  }

//  @Override
  public Object getItem(int pos) {
    return pos;
  }

//  @Override
  public long getItemId(int pos) {
    return pos;
  }

//  @Override
  public View getView(int position, View convertView, ViewGroup viewGroup) {
    HomeItem item = homeItemList.get(position);
    LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = li.inflate(R.layout.appitem, null);
    view.setPadding(8, 8, 8, 8);
//    view.setLayoutParams(new GridView.LayoutParams(90, 90));

    ImageView iv = (ImageView) view.findViewById(R.id.icon_image);
    iv.setImageBitmap(item._icon);
    TextView tv = (TextView) view.findViewById(R.id.icon_text);
    tv.setText(item._name);
    return view;
  }

}
