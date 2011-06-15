/*
 * Copyright (C) 2010 Cyril Mottier (http://www.cyrilmottier.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eXo.eXoPlatform;

import greendroid.widget.item.SubtitleItem;
import greendroid.widget.itemview.ItemView;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cyrilmottier.android.greendroid.R;

/**
 * A ThumbnailItem item is a complex item that wraps a drawable and two strings
 * : a title and a subtitle. The representation of that item is quite common to
 * Android users: The drawable is on the left of the item view and on the right
 * the title and the subtitle are displayed like a {@link SubtitleItem}.
 * 
 * @author Cyril Mottier
 */
public class ActivityStreamBrowserItem extends greendroid.widget.item.Item {

    public Bitmap bmAvatar;
    public String strName;
    public String strMessage;
    public int nbComment;
    public int nbLike;
    public String strTime;
    public boolean isShowMore;
    /**
     * @hide
     */
    public ActivityStreamBrowserItem() {
    }

    public ActivityStreamBrowserItem(Bitmap _bmAvatar, String _strName, String _strMessage, int _nbComment, int _nbLike, String _strTime, boolean _isShowMore) {
      super();
      
      this.bmAvatar = _bmAvatar;
      this.strName = _strName;
      this.strMessage = _strMessage;
      this.nbComment = _nbComment;
      this.nbLike = _nbLike;
      this.strTime = _strTime;
      this.isShowMore = _isShowMore;
  }
    
    @Override
    public ItemView newView(Context context, ViewGroup parent) {
//        return createCellFromXml(context, R.layout.activitybrowserviewcell, parent);
      return (ItemView)LayoutInflater.from(context).inflate(R.layout.activitybrowserviewcell, parent, false);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException,
            IOException {
         super.inflate(r, parser, attrs);

//        TypedArray a = r.obtainAttributes(attrs, R.styleable.ThumbnailItem);
//        drawableId = a.getResourceId(R.styleable.ThumbnailItem_thumbnail, drawableId);
//        a.recycle();
    }

}
