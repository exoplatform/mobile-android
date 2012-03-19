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
package org.exoplatform.utils;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Aug
 * 17, 2011
 */
public class WebdavMethod extends HttpRequestBase {

  private String method;

  // Constructor for delete, create new folder, check reachability url
  public WebdavMethod(String method, String sourceUriStr) {
    this.method = method;
    this.setURI(URI.create(sourceUriStr));
  }

  // Constructor for copy, move
  public WebdavMethod(String method, String sourceUriStr, String destinationUriStr) {
    this.method = method;
    this.setURI(URI.create(sourceUriStr));
    this.setHeader("Overwrite", "T");
    this.setHeader("Destination", destinationUriStr);
  }

  @Override
  public String getMethod() {
    return method;
  }

}
