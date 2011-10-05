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
import java.net.URISyntaxException;

import android.util.Log;
import android.webkit.URLUtil;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 25, 2011  
 */
public class URLAnalyzer {

  public URLAnalyzer()
  {
    
  }
  
  public boolean isValidUrl(String urlStr)
  {
    return URLUtil.isValidUrl(urlStr);
  }
  
  public String parserURL(String urlStr)
  {
    
//    Log.e("Valid:", "" + isValidUrl(urlStr));
    
    String url = urlStr;
    
    boolean isHTTPSUrl = false;


    try {
      
      int indexOfProtocol;
      
      
      indexOfProtocol = urlStr.indexOf(ExoConstants.HTTP_PROTOCOL);
      if(indexOfProtocol == 0)
        url = urlStr.substring(ExoConstants.HTTP_PROTOCOL.length() + 3);
      
      indexOfProtocol = urlStr.indexOf(ExoConstants.HTTPS_PROTOCOL);
      if(indexOfProtocol == 0) {
        url = urlStr.substring(ExoConstants.HTTPS_PROTOCOL.length() + 3);
        isHTTPSUrl = true;
      }
      
      while(url.charAt(0) == '/')
      {
        if(url.length() > 1)
          url = url.substring(1);
        else
          url = null;
      }
      
      
    } catch (Exception e) {
      url = null;
    }
    
    
    try {
      
      URI uri;	
    	
      if (!(isHTTPSUrl)) {
    	  url = ExoConstants.HTTP_PROTOCOL + "://" + url;
    	  uri = new URI(ExoConstants.HTTP_PROTOCOL + url);
    	  url = ExoConstants.HTTP_PROTOCOL + "://" + uri.getHost();
      } else {
    	  url = ExoConstants.HTTPS_PROTOCOL + "://" + url;
    	  uri = new URI(ExoConstants.HTTPS_PROTOCOL + url);
    	  url = ExoConstants.HTTPS_PROTOCOL + "://" + uri.getHost();
      }
      
      int port = uri.getPort(); 
      if(port > 0)
        url += ":" + port; 
      
    } catch (URISyntaxException e) {
      String msg = e.getMessage();
      String reason = e.getReason();
      Log.e("URISyntaxException",msg+"  "+reason);
      
      url = null;
    }
    
    return url;
  }
}
