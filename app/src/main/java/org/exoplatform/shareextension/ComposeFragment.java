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
package org.exoplatform.shareextension;

import org.exoplatform.R;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.Log;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Philippe Aristote paristote@exoplatform.com
 * @since Jun 3, 2015
 */
public class ComposeFragment extends Fragment {

  private static ComposeFragment instance;

  public static final String     COMPOSE_FRAGMENT = "compose_fragment";

  private EditText               etPostMessage;

  private TextView               tvAccount, tvSpace, tvMoreAttachments;

  private ImageView              imgThumb;

  private Bitmap                 bmThumb;

  private int                    nbAttachments;

  private ScrollView             scroller;

  private TextWatcher            postValidator;

  public ComposeFragment() {
    postValidator = new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        // Enables the post button if the composer text field contains a message
        enableDisableMainButton();
      }

      @Override
      public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
      }

      @Override
      public void afterTextChanged(Editable e) {
        // Update the value of post message after edit
        getShareActivity().setPostMessage(e.toString());
      }
    };
  }

  public static ComposeFragment getFragment() {
    if (instance == null) {
      instance = new ComposeFragment();
    }
    return instance;
  }

  private void init() {
    etPostMessage.setText(getShareActivity().getPostInfo().postMessage);
    // Show a > icon on the account selector if 2 or more accounts exist
    boolean manyAccounts = ServerSettingHelper.getInstance().twoOrMoreAccountsExist(getActivity());
    if (manyAccounts) {
      tvAccount.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_chevron_right_grey, 0);
    } else {
      tvAccount.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }
  }

  public void setTouchListener() {
    // Open the soft keyboard and give focus to the edit text field when the
    // scroll view is tapped
    scroller.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          etPostMessage.requestFocus();
          InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
          mgr.showSoftInput(etPostMessage, InputMethodManager.SHOW_IMPLICIT);
        }
        return v.performClick();
      }
    });
  }

  public void setThumbnailImage(Bitmap bm) {
    bmThumb = bm;
    if (bm != null) {
      imgThumb.setImageBitmap(bm);
    }
  }

  public void setNumberOfAttachments(int nb) {
    nbAttachments = nb;
    if (nb > 1) {
      tvMoreAttachments.setText(String.format("+ %d",(nb - 1)));
      tvMoreAttachments.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.share_extension_compose_fragment, container, false);
    etPostMessage = (EditText) layout.findViewById(R.id.share_post_message);
    etPostMessage.addTextChangedListener(postValidator);
    tvAccount = (TextView) layout.findViewById(R.id.share_account);
    tvAccount.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getShareActivity().onSelectAccount();
      }
    });
    tvSpace = (TextView) layout.findViewById(R.id.share_space);
    tvSpace.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getShareActivity().onSelectSpace();
      }
    });
    tvMoreAttachments = (TextView) layout.findViewById(R.id.share_attachment_more);
    imgThumb = (ImageView) layout.findViewById(R.id.share_attachment_thumbnail);
    scroller = (ScrollView) layout.findViewById(R.id.share_scroll_wrapper);
    init();
    return layout;
  }

  @Override
  public void onResume() {
    setTouchListener();
    getShareActivity().toggleMainButtonType(ShareActivity.BUTTON_TYPE_SHARE);
    if (!getShareActivity().isProgressVisible())
      getShareActivity().toggleProgressVisible(false);
    ExoAccount selectedAccount = getShareActivity().getPostInfo().ownerAccount;
    if (selectedAccount != null)
      tvAccount.setText(String.format("%s (%s)", selectedAccount.accountName, selectedAccount.username));
    setThumbnailImage(bmThumb);
    setNumberOfAttachments(nbAttachments);
    super.onResume();
  }

  @Override
  public void onDetach() {
    instance = null;
    super.onDetach();
  }

    /*
   * GETTERS & SETTERS
   */

  public EditText getEditText() {
    return etPostMessage;
  }

  public ShareActivity getShareActivity() {
    if (getActivity() instanceof ShareActivity) {
      return (ShareActivity) getActivity();
    } else {
      throw new UnsupportedOperationException("This fragment is only valid in the activity org.exoplatform.shareextension.ShareActivity");
    }
  }

  public String getPostMessage() {
    return etPostMessage != null ? etPostMessage.getText().toString() : "";
  }

  public void setSpaceSelectorLabel(String label) {
    tvSpace.setText(label);
  }

  private void enableDisableMainButton() {
    if (isAdded()) {
      boolean postEmpty = "".equals(etPostMessage.getText().toString());
      getShareActivity().enableDisableMainButton(!postEmpty);
    }
  }

}
