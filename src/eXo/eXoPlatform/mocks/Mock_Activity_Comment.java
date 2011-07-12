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
package eXo.eXoPlatform.mocks;

import java.util.ArrayList;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jun
 * 21, 2011
 */

public class Mock_Activity_Comment {

  String                   activityID;

  String                   userID;

  ArrayList<Mock_Activity> arrTxtComments;

  public Mock_Activity_Comment() {

  }

  public Mock_Activity_Comment(String _activityID,
                               String _userID,
                               ArrayList<Mock_Activity> _arrTxtComments) {

    this.activityID = _activityID;
    this.userID = _userID;
    this.arrTxtComments = _arrTxtComments;
  }

}
