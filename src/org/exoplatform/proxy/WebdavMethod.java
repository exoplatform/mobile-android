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
package org.exoplatform.proxy;

import java.net.URI;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.FileEntity;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 17, 2011  
 */
public class WebdavMethod extends HttpRequestBase{

  String method;
  String sourceUriStr, destinationUriStr;
  FileEntity entity;
  
  
  //Constructor for delete
  public WebdavMethod(String method, String url)
  {
    this.method = method;
    this.sourceUriStr = url;
    this.setURI(URI.create(sourceUriStr));
  }
  
//Constructor for upload
  public WebdavMethod(String method, String url, FileEntity fileEntity)
  {
    this.method = method;
    this.sourceUriStr = url;
    this.entity = fileEntity;
  }

  //Constructor for copy, move
  public WebdavMethod(String method, String source, String destination)
  {
    this.method = method;
    this.sourceUriStr = source;
    this.destinationUriStr = destination;
    this.setURI(URI.create(sourceUriStr));
    this.setHeader("Overwrite", "T");
    this.setHeader("Destination", destinationUriStr);
  }
   
  @Override
  public String getMethod() {
    // TODO Auto-generated method stub
    return method;
  }

}
