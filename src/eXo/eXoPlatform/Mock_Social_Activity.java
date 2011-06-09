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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.R.bool;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 9, 2011  
 */
public class Mock_Social_Activity {


//======================== INNER CLASS =========================
//==============================================================
  public class Mock_Activity {
    
    String userID;
    String title;
    String body;
    Date lastUpdateDate;
    long postedTime;
    int nbLikes;
    int nbComments;
    
    public Mock_Activity(String _userID, String _title, String _body, long _postedTime, int _numberOfLikes, int _numberOfComments)
    {
      this.userID = _userID;
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

}

//======================== INNER CLASS =========================
//==============================================================
  public class Mock_Activity_Comment {
    
    String userID;
    String statusID;
    String title;
    long postedTime;
    
    public Mock_Activity_Comment(String _userID, String _statusID, String _title, long _postedTime)
    {
      this.userID = _userID;
      this.statusID = _statusID;
      this.title = _title;
      this.postedTime = _postedTime;
    }

    public String datePrepared() // Method to calcul the date information (ie : 2minutes ago, 2 days ago...)
    {
      return "datePrepared not Implemented";
    }

}

  
  List<Mock_Activity> arrayOfActivities;
  List<Mock_Activity_Comment> arrayOfActivityComments;
  
  public Mock_Social_Activity(Boolean isComment)
  {
    if(isComment)
    {
      Mock_Activity_Comment act_cm_01 =  new Mock_Activity_Comment("32D52", "A1ED2", "This is a short message", 3600);
      Mock_Activity_Comment act_cm_02 =  new Mock_Activity_Comment("32D52", "A1ED2", "This is a normal message, with some content. And a second sentence.", 360);
      Mock_Activity_Comment act_cm_03 =  new Mock_Activity_Comment("32D52", "A1ED2", "This is a short message", 3600);
      Mock_Activity_Comment act_cm_04 =  new Mock_Activity_Comment("32D52", "A1ED2", "This is a short message", 3600);
      Mock_Activity_Comment act_cm_05 =  new Mock_Activity_Comment("32D52", "A1ED2", "This is a short message", 3600);
      Mock_Activity_Comment act_cm_06 =  new Mock_Activity_Comment("32D52", "A1ED2", "This is a short message", 3600);
      Mock_Activity_Comment act_cm_07 =  new Mock_Activity_Comment("32D52", "A1ED2", "This is a short message", 3600);
      Mock_Activity_Comment act_cm_08 =  new Mock_Activity_Comment("32D52", "A1ED2", "This is a short message", 3600);
      
      arrayOfActivityComments = Arrays.asList(new Mock_Activity_Comment[] {act_cm_01, act_cm_02, act_cm_03, act_cm_04, act_cm_05, act_cm_06, act_cm_07, act_cm_08});
    }
    else
    {
      Mock_Activity act_01 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 1, 1);
      Mock_Activity act_02 =  new Mock_Activity("32D52", "This is a normal message, with some content. And a second sentence.", "", 3600, 1, 1);
      Mock_Activity act_03 =  new Mock_Activity("32D52", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", "", 3600, 1, 1);
      Mock_Activity act_04 =  new Mock_Activity("32D52", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", "", 3600, 1, 1);
      Mock_Activity act_05 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 1, 1);
      Mock_Activity act_06 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 1, 1);
      Mock_Activity act_07 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 0, 0);
      Mock_Activity act_08 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 2, 0);
      Mock_Activity act_09 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 20, 0);
      Mock_Activity act_010 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 200, 0);
      Mock_Activity act_011 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 2000, 0);
      Mock_Activity act_012 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 0, 0);
      Mock_Activity act_013 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 0, 2);
      Mock_Activity act_014 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 0, 20);
      Mock_Activity act_015 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 0, 200);
      Mock_Activity act_016 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 0, 2000);
      Mock_Activity act_017 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 1, 1);
      Mock_Activity act_018 =  new Mock_Activity("32D52", "This is a short message", "", 30, 1, 1);
      Mock_Activity act_019 =  new Mock_Activity("32D52", "This is a short message", "", 60, 1, 1);
      Mock_Activity act_020 =  new Mock_Activity("32D52", "This is a short message", "", 600, 1, 1);
      Mock_Activity act_021 =  new Mock_Activity("32D52", "This is a short message", "", 3600, 1, 1);
      Mock_Activity act_022 =  new Mock_Activity("32D52", "This is a short message", "", 7200, 1, 1);
      Mock_Activity act_023 =  new Mock_Activity("32D52", "This is a short message", "", 86400, 1, 1);
      Mock_Activity act_024 =  new Mock_Activity("32D52", "This is a short message", "", 172800, 1, 1);
      Mock_Activity act_025 =  new Mock_Activity("32D52", "This is a short message", "", 864000, 1, 1);
      Mock_Activity act_026 =  new Mock_Activity("32D52", "This is a short message", "", 2592000, 1, 1);
      Mock_Activity act_027 =  new Mock_Activity("32D52", "This is a short message", "", 5184000, 1, 1);
      
      arrayOfActivities = Arrays.asList(new Mock_Activity[] {act_01, act_02, act_03, act_04, act_05, act_06, act_07, act_08, 
          act_09, act_010, act_011, act_012, act_013, act_014, act_015, act_016, act_017, act_018, act_019,
          act_020, act_021, act_022, act_023, act_024, act_025, act_026, act_027});
    }
    
    
  }


}
