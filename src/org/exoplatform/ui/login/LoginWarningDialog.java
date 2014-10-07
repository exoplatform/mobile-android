/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.ui.login;

import org.exoplatform.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * A dialog that display a warning message with an animation <br/>
 */
public class LoginWarningDialog extends Dialog implements android.view.View.OnClickListener {

    private TextView mTitleTxt;

    private TextView mMessageTxt;

    protected Button mBtn;

    private int      mWindowsAnim = 0;

    public LoginWarningDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_warning_dialog_layout);

        initSubViews();
    }

    public LoginWarningDialog(Context context,
                              String titleString,
                              String contentString,
                              String okString) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_warning_dialog_layout);

        initSubViews();

        mTitleTxt.setText(titleString);
        mMessageTxt.setText(contentString);
        mBtn.setText(okString);
    }

    private void initSubViews() {
        mTitleTxt = (TextView) findViewById(R.id.warning_dialog_title_txt);
        mMessageTxt = (TextView) findViewById(R.id.warning_dialog_message_txt);
        mBtn = (Button) findViewById(R.id.warning_dialog_btn);
        mBtn.setOnClickListener(this);
    }

    public LoginWarningDialog setTitle(String title) {
        mTitleTxt.setText(title);
        return this;
    }

    public LoginWarningDialog setMessage(String message) {
        mMessageTxt.setText(message);
        return this;
    }

    public LoginWarningDialog setButtonText(String text) {
        mBtn.setText(text);
        return this;
    }

    public LoginWarningDialog setWindowsAnimation(int anim) {
        mWindowsAnim = anim;
        return this;
    }

    @Override
    public void show() {
        getWindow().getAttributes().windowAnimations = mWindowsAnim != 0 ? mWindowsAnim
                                                                        : R.style.Animations_Window;
        super.show();
    }

    public void onClick(View view) {
        if (view.equals(mBtn)) {
            dismiss();

            if (mViewListener != null)
                mViewListener.onClickOk(this);
        }
    }

    private ViewListener mViewListener;

    /* interface to listen to view event */
    public interface ViewListener {

        void onClickOk(LoginWarningDialog dialog);
    }

    public void setViewListener(ViewListener l) {
        mViewListener = l;
    }
}
