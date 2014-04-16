/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.mobile.tests;

import org.exoplatform.model.ServerObjInfo;
import org.junit.After;
import org.junit.Before;
import org.robolectric.util.ActivityController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Apr 15, 2014  
 */
public abstract class ExoActivityTestUtils<A extends Activity> {
  
  final String TEST_SERVER_NAME = "testserver";
  final String TEST_SERVER_URL = "http://www.test.com";
  final String TEST_USER_NAME = "testuser";
  final String TEST_USER_PWD = "testpwd";
  final ServerObjInfo TEST_SERVER = defaultServer();
  
  private ServerObjInfo defaultServer() {
    ServerObjInfo srv = new ServerObjInfo();
    srv.serverName = TEST_SERVER_NAME;
    srv.serverUrl = TEST_SERVER_URL;
    srv.username = TEST_USER_NAME;
    srv.password = TEST_USER_PWD;
    srv.isAutoLoginEnabled = false;
    srv.isRememberEnabled = false;
    return srv;
  }
  
  ActivityController<A> controller;
  A activity;
  
  @Before
  public abstract void setup();
  
  @After
  public void teardown() {
    controller.destroy();
  }
  
  public void create() {
    activity = controller
        .create()
        .start()
        .resume()
        .visible()
        .get();
  }
  
  public void createWithBundle(Bundle b) {
    activity = controller
        .create(b) // passing the bundle here, no need to call controller.restoreInstanceState(b)
        .start()
        .resume()
        .visible()
        .get();
  }
  
  public void createWithIntent(Intent i) {
     activity = controller
       .withIntent(i) // passing the intent here
       .create()
       .start()
       .resume()
       .visible()
       .get();
  }
  
}
