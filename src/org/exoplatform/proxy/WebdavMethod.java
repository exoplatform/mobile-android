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


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 17, 2011  
 */
public class WebdavMethod extends HttpRequestBase {

  String sourceUriStr, destinationUriStr;
  
  //Constructor for delete, upload
  public WebdavMethod(String url)
  {
    this.sourceUriStr = url;
  }

  //Constructor for copy, move
  public WebdavMethod(String source, String destination)
  {
    this.sourceUriStr = source;
    this.destinationUriStr = destination;
  }
  
  public boolean MoveMethod()
  {
    return false;
  }
  
  public boolean UploadMethod()
  {
    return false;
  }
  
  public void setMethod(String method)
  {
    
    this.setMethod(method);
    
    this.setURI(URI.create(sourceUriStr));
    
    if(method.equalsIgnoreCase("DELETE"))
    {
      
    }
    else if(method.equalsIgnoreCase("UPLOAD"))
    {
      
    }
    else if(method.equalsIgnoreCase("COPY") || method.equalsIgnoreCase("MOVE"))
    {
      this.setHeader("Overwrite", "T");
      this.setHeader("Destination", destinationUriStr);
    }
    else
    {
      
    }
    
  }
  
  @Override
  public String getMethod() {
    // TODO Auto-generated method stub
    return null;
  }

}
