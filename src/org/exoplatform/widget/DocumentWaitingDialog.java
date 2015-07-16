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
package org.exoplatform.widget;

import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.Utils;

import android.content.Context;

public class DocumentWaitingDialog extends WaitingDialog {

  public DocumentWaitingDialog(Context context, String titleString, String contentString) {
    super(context, titleString, contentString);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    DocumentActivity docAct = Utils.getVal(DocumentActivity._documentActivityInstance);
    if (docAct != null)
      docAct.onCancelLoad();
  }

}
