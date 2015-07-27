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
