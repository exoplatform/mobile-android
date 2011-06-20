/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package eXo.eXoPlatform;

import java.util.Date;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 16, 2011  
 */
public class Mock_Activity {
  
  String userID;
  String imageUrl;
  String title;
  String body;
  Date lastUpdateDate;
  long postedTime;
  int nbLikes;
  int nbComments;
  String postedTimeInWords;
  boolean isShowMore;
  boolean isHeader;
  
  public Mock_Activity()
  {
    
  }
  
  public Mock_Activity(String _userID, String _imageUrl, String _title, String _body, long _postedTime, int _numberOfLikes, int _numberOfComments)
  {
    this.userID = _userID;
    this.imageUrl = _imageUrl;
    this.title = _title;
    this.postedTime = _postedTime;
    this.body = _body;
    this.nbLikes = _numberOfLikes;
    this.nbComments = _numberOfComments;
  }

  public String datePrepared() // Method to calcul the date information (ie : 2minutes ago, 2 days ago...)
  {
    return "datePrepared not Implemented";
  }
  
  public void setShowMore(boolean showMore)
  {
    this.isShowMore = showMore;
  }
  
  public void setHeader(boolean header)
  {
    this.isHeader = header;
  }

}
