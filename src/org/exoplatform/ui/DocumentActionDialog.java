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
package org.exoplatform.ui;

import org.exoplatform.R;
import org.exoplatform.controller.document.DocumentActionAdapter;
import org.exoplatform.model.ExoFile;

import android.app.Dialog;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

//File action list
public class DocumentActionDialog extends Dialog {

  private ListView             _listViewFileAction;   // List of
                                                       // action

  private TextView             _txtvFileName;         // File's
                                                       // name

  public ExoFile               myFile;                // Current
                                                       // file

  public DocumentActionAdapter _documentActionAdapter;
  
  // Constructor
  public DocumentActionDialog(DocumentActivity context, ExoFile file, boolean isActBar) {

    super(context);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.exofileaction);

    setCanceledOnTouchOutside(true);

    myFile = file;

    _documentActionAdapter = new DocumentActionAdapter(context, this, myFile, isActBar);

    init();

    setTileForDialog(myFile.name);

  }

  public void setTileForDialog(String title) {
    _txtvFileName.setText(title);
  }

  private void init() {

    _listViewFileAction = (ListView) findViewById(R.id.ListView0_FileAction);

    _txtvFileName = (TextView) findViewById(R.id.TextView_Title);

    setDocumentActionAdapter();

  }

  public void setDocumentActionAdapter() {
    _listViewFileAction.setAdapter(_documentActionAdapter);
  }

}
