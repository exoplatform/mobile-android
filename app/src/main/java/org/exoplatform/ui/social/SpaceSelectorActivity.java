/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ui.social;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.R;
import org.exoplatform.model.SocialSpaceInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.model.RestSpace;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Philippe Aristote paristote@exoplatform.com
 * @since Apr 21, 2015
 */
public class SpaceSelectorActivity extends FragmentActivity implements LoaderCallbacks<List<SocialSpaceInfo>>,
    OnItemClickListener {

  private static final String   LOG_TAG        = SpaceSelectorActivity.class.getName();

  public static final String    SELECTED_SPACE = "SelectedSpace";

  private SpaceListAdapter      listAdapterSpaces;

  private List<SocialSpaceInfo> listInfoSpaces;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.compose_message_space_selector_activity);
    setTitle(R.string.ShareWithWhom);

    ListView listViewSpaces = (ListView) findViewById(R.id.list_spaces);
    listViewSpaces.setOnItemClickListener(this);
    listAdapterSpaces = new SpaceListAdapter(this);
    listViewSpaces.setAdapter(listAdapterSpaces);
    listViewSpaces.setEmptyView(findViewById(R.id.list_spaces_empty_view));
    getLoaderManager().initLoader(0, null, this).forceLoad();

  }

  /*
   * Tap listeners
   */

  /**
   * Called when the "Public" item is tapped.<br/>
   * Results in a call to
   * 
   * <pre>
   * sendResultToComposer(-1);
   * </pre>
   * 
   * @param view
   */
  public void selectPublicDestination(View view) {
    sendResultToComposer(-1);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case android.R.id.home:
      finish();
      break;
    default:
      break;
    }

    return true;
  }

  /**
   * Called when an item of the spaces list is tapped.<br/>
   * Results in a call to
   * 
   * <pre>
   * sendResultToComposer(...);
   * </pre>
   * 
   * with the position of the selected space in the list.
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    SocialSpaceInfo selectedSpace = listInfoSpaces.get(position);
    if (selectedSpace != null) {
      sendResultToComposer(position);
    } else {
      // TODO handle failure to get space
    }
  }

  /**
   * Return the result to the calling activity (the message composer).
   * 
   * @param result <br/>
   *          if a space was selected, the intent will contain the extras:<br/>
   *          - SELECTED_DESTINATION: the space technical name<br/>
   *          - SELECTED_SPACE_DISPLAY_NAME: the space display name<br/>
   *          - SELECTED_SPACE_IMAGE: the avatar url of the space<br/>
   *          if "Public" was selected, the intent contains no extra
   */
  private void sendResultToComposer(int result) {
    Intent data = new Intent();
    if (result >= 0 && result < listInfoSpaces.size()) {
      SocialSpaceInfo space = listInfoSpaces.get(result);
      data.putExtra(SELECTED_SPACE, space);
    }
    setResult(RESULT_OK, data);
    finish();
  }

  /*
   * Loader manager
   */

  @Override
  public Loader<List<SocialSpaceInfo>> onCreateLoader(int id, Bundle args) {
    return new AsyncTaskLoader<List<SocialSpaceInfo>>(this) {
      @Override
      public List<SocialSpaceInfo> loadInBackground() {
        List<SocialSpaceInfo> spacesNames = new ArrayList<SocialSpaceInfo>();
        if (SocialServiceHelper.getInstance().spaceService == null) {
          Log.e(LOG_TAG, "Cannot retrieve spaces. Social Space service is null.");
          return null;
        }
        List<RestSpace> spaces = SocialServiceHelper.getInstance().spaceService.getMySocialSpaces();
        String currentServer = AccountSetting.getInstance().getDomainName();
        for (RestSpace space : spaces) {
          SocialSpaceInfo sp = new SocialSpaceInfo();
          sp.displayName = space.getDisplayName();
          sp.name = space.getName();
          sp.avatarUrl = currentServer + space.getAvatarUrl();
          sp.groupId = space.getGroupId();
          spacesNames.add(sp);
        }
        return spacesNames;
      }
    };
  }

  @Override
  public void onLoadFinished(Loader<List<SocialSpaceInfo>> loader, List<SocialSpaceInfo> data) {
    if (data != null) {
      listInfoSpaces = data;
      listAdapterSpaces.setSpaceList(data);
      listAdapterSpaces.notifyDataSetChanged();
    }
  }

  @Override
  public void onLoaderReset(Loader<List<SocialSpaceInfo>> loader) {
  }
}
