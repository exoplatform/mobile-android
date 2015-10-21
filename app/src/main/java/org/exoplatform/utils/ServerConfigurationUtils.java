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
/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 12, 2011  
 */

package org.exoplatform.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import org.exoplatform.model.ExoAccount;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;

/**
 * Help to deal with writing and reading from xml config file
 */
public class ServerConfigurationUtils {

  public static String        version;

  private static final String TAG = "eXo____ServerConfigUtils___";

  public ServerConfigurationUtils(Context context) {
  }

  /**
   * Retrieve server list from XML config file
   * 
   * @param context
   * @param fileName
   * @return a list of servers, or an empty list, but never null
   */
  public static ArrayList<ExoAccount> getServerListFromFile(Context context, String fileName) {
    Log.i(TAG, "getServerListFromFile: " + fileName);

    ArrayList<ExoAccount> arrServerList = new ArrayList<ExoAccount>();
    FileInputStream fis = null;
    try {
      fis = context.openFileInput(fileName);
      DocumentBuilderFactory doc_build_fact = DocumentBuilderFactory.newInstance();
      DocumentBuilder doc_builder = doc_build_fact.newDocumentBuilder();
      Document obj_doc = doc_builder.parse(fis);

      if (null != obj_doc) {
        org.w3c.dom.Element feed = obj_doc.getDocumentElement();
        NodeList obj_nod_list = feed.getElementsByTagName("server");

        for (int i = 0; i < obj_nod_list.getLength(); i++) {
          Node itemNode = obj_nod_list.item(i);
          if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
            Element itemElement = (Element) itemNode;

            ExoAccount serverObj = new ExoAccount();
            serverObj.accountName = itemElement.getAttribute("name");
            serverObj.serverUrl = itemElement.getAttribute(ExoConstants.EXO_URL_SERVER);
            serverObj.username = itemElement.getAttribute(ExoConstants.EXO_URL_USERNAME);
            try {
              serverObj.password = SimpleCrypto.decrypt(ExoConstants.EXO_MASTER_PASSWORD, itemElement.getAttribute("password"));
            } catch (Exception ee) {
              // Catch runtime exception throw while decrypt password
              Log.e(TAG, "Could not decrypt password: " + ee.getLocalizedMessage());
              Log.w(TAG, "Leaving password attribute empty");
              serverObj.password = "";
            }
            serverObj.isRememberEnabled = Boolean.parseBoolean(itemElement.getAttribute(ExoConstants.EXO_REMEMBER_ME));
            serverObj.isAutoLoginEnabled = Boolean.parseBoolean(itemElement.getAttribute(ExoConstants.EXO_AUTOLOGIN));
            serverObj.userFullName = itemElement.getAttribute(ExoConstants.EXO_USER_FULLNAME);
            try {
              String logTime = itemElement.getAttribute(ExoConstants.EXO_LAST_LOGIN);
              if (logTime != null)
                serverObj.lastLoginDate = Long.parseLong(logTime);
              else {
                serverObj.lastLoginDate = -1;
                Log.i(TAG, "Last login date unknown");
              }
            } catch (NumberFormatException e) {
              serverObj.lastLoginDate = -1;
              Log.i(TAG, "Last login date unknown");
            }
            serverObj.avatarUrl = itemElement.getAttribute(ExoConstants.EXO_URL_AVATAR);
            arrServerList.add(serverObj);
          }
        }
      }

    } catch (FileNotFoundException e) {
      Log.i(TAG, "File not found");
      return arrServerList;
    } catch (IOException e) {
//       if (Config.GD_ERROR_LOGS_ENABLED)
      Log.e(TAG, "getServerListWithFileName - " + e.getLocalizedMessage());
      return arrServerList;
    } catch (ParserConfigurationException e) {
      // if (Config.GD_ERROR_LOGS_ENABLED)
      Log.e(TAG, "getServerListWithFileName - " + e.getLocalizedMessage());
      return arrServerList;
    } catch (SAXException e) {
      // if (Config.GD_ERROR_LOGS_ENABLED)
      Log.e(TAG, "getServerListWithFileName - " + e.getLocalizedMessage());
      return arrServerList;
    } catch (Exception e) {
      Log.e(TAG, "getServerListWithFileName - " + e.getLocalizedMessage());
      return arrServerList;
    }  finally {
      if (fis != null)
        try {
          fis.close();
        } catch (IOException e) {
          Log.d(TAG, Log.getStackTraceString(e));
        }
    }

    return arrServerList;
  }

  /**
   * Check whether new config file for app exists
   * 
   * @param context
   * @return
   */
  public static boolean newAppConfigExists(Context context) {
    return context.getFileStreamPath(ExoConstants.EXO_SERVER_SETTING_FILE).exists();
  }

  /**
   * Check if previous config file exists
   * 
   * @param context
   * @return
   */
  public static String checkPreviousAppConfig(Context context) {
    /* check external storage available */
    String state = Environment.getExternalStorageState();
    File oldConfig;
    if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) || Environment.MEDIA_MOUNTED.equals(state)) {
      oldConfig = new File(Environment.getExternalStorageDirectory().getPath() + "/eXo/"
          + ExoConstants.EXO_OLD_SERVER_SETTING_FILE);
    } else {
      oldConfig = new File(context.getDir("eXo", Context.MODE_WORLD_READABLE).getPath() + "/"
          + ExoConstants.EXO_OLD_SERVER_SETTING_FILE);
    }

    if (oldConfig.exists())
      return oldConfig.getAbsolutePath();
    return null;
  }

  /**
   * Retrieve server list from XML config file
   * 
   * @param fileName
   * @return
   */
  public static ArrayList<ExoAccount> getServerListFromOldConfigFile(String fileName) {
    Log.i(TAG, "getServerListFromOldConfigFile: " + fileName);

    ArrayList<ExoAccount> arrServerList = new ArrayList<ExoAccount>();
    File file = new File(fileName);
    try {
      FileInputStream fis = new FileInputStream(file);
      DocumentBuilderFactory doc_build_fact = DocumentBuilderFactory.newInstance();
      DocumentBuilder doc_builder = doc_build_fact.newDocumentBuilder();
      Document obj_doc = doc_builder.parse(fis);

      if (null != obj_doc) {
        org.w3c.dom.Element feed = obj_doc.getDocumentElement();
        NodeList obj_nod_list = feed.getElementsByTagName("server");

        for (int i = 0; i < obj_nod_list.getLength(); i++) {
          Node itemNode = obj_nod_list.item(i);
          if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
            Element itemElement = (Element) itemNode;

            ExoAccount serverObj = new ExoAccount();
            serverObj.accountName = itemElement.getAttribute("name");
            serverObj.serverUrl = itemElement.getAttribute("serverURL");
            Log.i(TAG, "server: " + serverObj.accountName + " - url: " + serverObj.serverUrl);
            if (serverObj.serverUrl != null)
              if (!serverObj.serverUrl.equals(""))
                arrServerList.add(serverObj);
          }
        }
      }
      fis.close();

      if (file.delete())
        Log.i(TAG, "delete old config file");
      else
        Log.e("Error", "Can not delete old config file: " + fileName);
    } catch (FileNotFoundException e) {
      Log.i(TAG, "File not found");
      return null;
    } catch (IOException e) {
      // if (Config.GD_ERROR_LOGS_ENABLED)
      Log.e("IOException", "getServerListFromOldConfigFile");
    } catch (ParserConfigurationException e) {
      // if (Config.GD_ERROR_LOGS_ENABLED)
      Log.e("ParserConfigurationException", "getServerListFromOldConfigFile");
    } catch (SAXException e) {
      // if (Config.GD_ERROR_LOGS_ENABLED)
      Log.e("SAXException", "getServerListFromOldConfigFile");
    } catch (Exception e) {
      Log.e("Exception", "getServerListFromOldConfigFile - decryption exception : " + e.getLocalizedMessage());
    }
    return arrServerList;
  }

  /**
   * Create XML config file from server list
   * 
   * @param context
   * @param objList
   * @param fileName
   * @param appVersion
   * @return
   */
  public static boolean generateXmlFileWithServerList(Context context,
                                                      ArrayList<ExoAccount> objList,
                                                      String fileName,
                                                      String appVersion) {
    Log.i(TAG, "generateXmlFileWithServerList: " + fileName);

    try {

      FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
      // we create a XmlSerializer in order to write xml data
      XmlSerializer serializer = Xml.newSerializer();
      // we set the FileOutputStream as output for the serializer, using
      // UTF-8 encoding
      serializer.setOutput(fos, "UTF-8");

      // Write <?xml declaration with encoding (if encoding not null) and
      // standalone flag (if standalone not null)
      serializer.startDocument(null, Boolean.TRUE);

      // set indentation option
      // serializer.setFeature(name,
      // state)("http://xmlpull.org/v1/doc/features.htmlindent-output",
      // true);

      // start a tag called "root"
      serializer.startTag(null, "xml").startTag(null, "version");
      serializer.attribute(null, "number", appVersion);
      serializer.endTag(null, "version");
      // }

      serializer.startTag(null, "Servers");

      // i indent code just to have a view similar to xml-tree
      for (int i = 0; i < objList.size(); i++) {
        ExoAccount serverObj = objList.get(i);
        if (Log.LOGD)
          Log.d(TAG, "Writing account " + serverObj);
        serializer.startTag(null, "server");
        serializer.attribute(null, "name", serverObj.accountName);
        serializer.attribute(null, ExoConstants.EXO_URL_SERVER, serverObj.serverUrl);
        serializer.attribute(null, ExoConstants.EXO_URL_USERNAME, serverObj.username);

        /* encrypt password */
        try {
          serializer.attribute(null, "password", SimpleCrypto.encrypt(ExoConstants.EXO_MASTER_PASSWORD, serverObj.password));
        } catch (Exception e) {
          Log.e(TAG, "Error while encrypting password: " + e.getLocalizedMessage());
          Log.w(TAG, "Writing password in clear");
          serializer.attribute(null, "password", serverObj.password);
        }
        serializer.attribute(null, ExoConstants.EXO_REMEMBER_ME, String.valueOf(serverObj.isRememberEnabled));
        serializer.attribute(null, ExoConstants.EXO_AUTOLOGIN, String.valueOf(serverObj.isAutoLoginEnabled));
        serializer.attribute(null, ExoConstants.EXO_USER_FULLNAME, serverObj.userFullName);
        serializer.attribute(null, ExoConstants.EXO_LAST_LOGIN, String.valueOf(serverObj.lastLoginDate));
        serializer.attribute(null, ExoConstants.EXO_URL_AVATAR, serverObj.avatarUrl);
        serializer.endTag(null, "server");
      }

      serializer.endTag(null, "Servers");
      serializer.endTag(null, "xml");
      serializer.endDocument();

      // write xml data into the FileOutputStream
      serializer.flush();
      // finally we close the file stream

      fos.close();

      return true;

    } catch (FileNotFoundException e) {
      return false;
    } catch (IOException e) {
      return false;
    }
  }
}
