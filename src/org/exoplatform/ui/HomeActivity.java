package org.exoplatform.ui;

import org.exoplatform.controller.home.HomeActionListenner;
import org.exoplatform.controller.home.HomeAdapter;
import org.exoplatform.controller.home.HomeController;
import org.exoplatform.widget.MyActionBar;

import android.os.Bundle;
import android.view.Window;
import android.widget.GridView;

import com.cyrilmottier.android.greendroid.R;

public class HomeActivity extends MyActionBar {
  private GridView gridView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.appsview2);
    super.getGDActionBar().setType(greendroid.widget.GDActionBar.Type.Dashboard);
    addActionBarItem(R.drawable.signout);
    super.setTitle("eXo");
    init();
  }

  @Override
  protected void onResume() {
    super.onResume();
    init();
  }

  private void init() {
    new HomeController(this);
    createAdapter();
  }

  private void createAdapter() {
    gridView = (GridView) findViewById(R.id.gridView1);
    gridView.setAdapter(new HomeAdapter(this));
    gridView.setOnItemClickListener(new HomeActionListenner(this));
  }

}
