/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 12, 2011  
 */

package org.exoplatform.utils;

import greendroid.util.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

public class ServerConfigurationUtils {

  public static String version;

  public ServerConfigurationUtils(Context context) {

  }

  // Constructor

  private static boolean createLocalFileDirectory(String path, boolean isFolder) {
    boolean returnValue = false;

    File f = new File(path);
    try {
      if (!f.exists()) {
        if (isFolder)
          returnValue = f.mkdir();
        else
          returnValue = f.createNewFile();
      }
    } catch (IOException e) {
      return false;
    }

    return returnValue;
  }

  // Get app's current version, create DefaultServerList.xml if needed
  public static String getAppVersion(Context _context) {

      boolean mExternalStorageAvailable = false;
      boolean mExternalStorageWriteable = false;
      String state = Environment.getExternalStorageState();

      if (Environment.MEDIA_MOUNTED.equals(state)) {
          // We can read and write the media
          mExternalStorageAvailable = mExternalStorageWriteable = true;
      } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
          // We can only read the media
          mExternalStorageAvailable = true;
          mExternalStorageWriteable = false;
      } else {
          // Something else is wrong. It may be one of many other states, but all we need
          //  to know is we can neither read nor write
          mExternalStorageAvailable = mExternalStorageWriteable = false;
      }
    String filePath ="";
    String rootPath ="";
    if (!(mExternalStorageAvailable && mExternalStorageWriteable)) {
       File folder = _context.getDir("eXo", Context.MODE_WORLD_WRITEABLE);
        rootPath = folder.getPath();
        filePath = rootPath + "/DefaultServerList.xml";
    } else {
       rootPath = Environment.getExternalStorageDirectory().getPath();
       filePath =  rootPath + "/eXo/DefaultServerList.xml";
    }
      createLocalFileDirectory(rootPath + "/eXo", true);
       File file = new File(filePath);
      if (!file.exists()) {
        createXmlDataWithServerList(getDefaultServerList(_context), "DefaultServerList.xml", "0");
        return "0";
      }

      InputStream obj_is = null;
      Document obj_doc = null;
      DocumentBuilderFactory doc_build_fact = null;
      DocumentBuilder doc_builder = null;
      try {
        obj_is = new FileInputStream(filePath);
        doc_build_fact = DocumentBuilderFactory.newInstance();
        doc_builder = doc_build_fact.newDocumentBuilder();

        obj_doc = doc_builder.parse(obj_is);

        NodeList obj_nod_list = null;
        if (null != obj_doc) {
          org.w3c.dom.Element feed = obj_doc.getDocumentElement();
          obj_nod_list = feed.getElementsByTagName("version");

          for (int i = 0; i < obj_nod_list.getLength(); i++) {
            Node itemNode = obj_nod_list.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
              Element itemElement = (Element) itemNode;
              return itemElement.getAttribute("number");
            }
          }
        }

      } catch (ParserConfigurationException e) {
        return "0";
      } catch (FileNotFoundException e) {
        return "0";
      } catch (SAXException e) {
        return "0";
      } catch (IOException e) {
        return "0";
      }


    return "0";
  }

  // Get default server list
  public static ArrayList<ServerObjInfo> getDefaultServerList(Context _context) {

    ArrayList<ServerObjInfo> arrServerList = getServerListWithFileName("");
    XmlResourceParser parser = _context.getResources().getXml(R.xml.defaultconfiguaration);

    try {

      int eventType = parser.getEventType();

      while (eventType != XmlPullParser.END_DOCUMENT) {
        String name = null;

        switch (eventType) {
        case XmlPullParser.START_TAG:
          name = parser.getName().toLowerCase();

          if (name.equalsIgnoreCase("server")) {
            ServerObjInfo serverObj = new ServerObjInfo();
            for (int i = 0; i < parser.getAttributeCount(); i++) {
              String attribute = parser.getAttributeName(i).toLowerCase();
              if (attribute.equalsIgnoreCase("name")) {
                serverObj._strServerName = parser.getAttributeValue(i);
              } else if (attribute.equalsIgnoreCase("serverURL")) {
                serverObj._strServerUrl = parser.getAttributeValue(i);
              }
            }
            serverObj._bSystemServer = true;
            arrServerList.add(serverObj);
          }

          break;
        case XmlPullParser.END_TAG:
          name = parser.getName();
          break;
        }

        try {
          eventType = parser.next();
        } catch (Exception e) {
          eventType = 0;
        }

      }
    } catch (XmlPullParserException e) {
      if (Config.GD_ERROR_LOGS_ENABLED)
        Log.e("XmlPullParserException", "Cannot parse XML");
    }

      boolean mExternalStorageAvailable = false;
      boolean mExternalStorageWriteable = false;
      String state = Environment.getExternalStorageState();

      if (Environment.MEDIA_MOUNTED.equals(state)) {
          // We can read and write the media
          mExternalStorageAvailable = mExternalStorageWriteable = true;
      } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
          // We can only read the media
          mExternalStorageAvailable = true;
          mExternalStorageWriteable = false;
      } else {
          // Something else is wrong. It may be one of many other states, but all we need
          //  to know is we can neither read nor write
          mExternalStorageAvailable = mExternalStorageWriteable = false;
      }
      String filePath ="";
      String rootPath ="";
      if (!(mExternalStorageAvailable && mExternalStorageWriteable)) {
          File folder = _context.getDir("eXo", Context.MODE_WORLD_WRITEABLE);
          rootPath = folder.getPath();
          filePath = rootPath + "/DefaultServerList.xml";
      } else {
          rootPath = Environment.getExternalStorageDirectory().getPath();
          filePath =  rootPath + "/eXo/DefaultServerList.xml";
      }

    //String filePath = Environment.getExternalStorageDirectory() + "/eXo/DefaultServerList.xml";
    File file = new File(filePath);
    if (!file.exists()) {
      createXmlDataWithServerList(arrServerList, "DefaultServerList.xml", "0");
    }

    return arrServerList;

  }

  // Get added/deleted servers
  public static ArrayList<ServerObjInfo> getServerListWithFileName(String name) {

    ArrayList<ServerObjInfo> arrServerList = new ArrayList<ServerObjInfo>();
    StringBuffer pathBuffer = new StringBuffer();
    pathBuffer.append(Environment.getExternalStorageDirectory());
    pathBuffer.append("/eXo/");
    pathBuffer.append(name);
    String filePath = pathBuffer.toString();
    File file = new File(filePath);
    if (!file.exists()) {
      return arrServerList;
    }

    InputStream obj_is = null;
    Document obj_doc = null;
    DocumentBuilderFactory doc_build_fact = null;
    DocumentBuilder doc_builder = null;
    try {
      obj_is = new FileInputStream(filePath);

      doc_build_fact = DocumentBuilderFactory.newInstance();
      doc_builder = doc_build_fact.newDocumentBuilder();

      obj_doc = doc_builder.parse(obj_is);

      NodeList obj_nod_list = null;
      if (null != obj_doc) {
        org.w3c.dom.Element feed = obj_doc.getDocumentElement();
        obj_nod_list = feed.getElementsByTagName("server");

        for (int i = 0; i < obj_nod_list.getLength(); i++) {
          Node itemNode = obj_nod_list.item(i);
          if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
            Element itemElement = (Element) itemNode;

            ServerObjInfo serverObj = new ServerObjInfo();
            serverObj._strServerName = itemElement.getAttribute("name");
            serverObj._strServerUrl = itemElement.getAttribute("serverURL");
            serverObj._bSystemServer = false;
            if (name.equalsIgnoreCase("DefaultServerList.xml"))
              serverObj._bSystemServer = true;

            arrServerList.add(serverObj);
          }
        }
      }

    } catch (IOException e) {
      if (Config.GD_ERROR_LOGS_ENABLED)
        Log.e("IOException", "getServerListWithFileName");
    } catch (ParserConfigurationException e) {
      if (Config.GD_ERROR_LOGS_ENABLED)
        Log.e("ParserConfigurationException", "getServerListWithFileName");
    } catch (SAXException e) {
      if (Config.GD_ERROR_LOGS_ENABLED)
        Log.e("SAXException", "getServerListWithFileName");
    }

    return arrServerList;
  }

  // Create user configuration file: deleted & added servers
  public static boolean createXmlDataWithServerList(ArrayList<ServerObjInfo> objList,
                                                    String fileName,
                                                    String appVersion) {
    StringBuffer pathBuffer = new StringBuffer();
    pathBuffer.append(Environment.getExternalStorageDirectory());
    pathBuffer.append("/eXo/");
    pathBuffer.append(fileName);
    File newxmlfile = new File(pathBuffer.toString());
    try {
      newxmlfile.createNewFile();

      // we have to bind the new file with a FileOutputStream
      FileOutputStream fileos = null;

      fileos = new FileOutputStream(newxmlfile);

      // we create a XmlSerializer in order to write xml data
      XmlSerializer serializer = Xml.newSerializer();
      // we set the FileOutputStream as output for the serializer, using UTF-8
      // encoding
      serializer.setOutput(fileos, "UTF-8");

      // Write <?xml declaration with encoding (if encoding not null) and
      // standalone flag (if standalone not null)
      serializer.startDocument(null, Boolean.valueOf(true));

      // set indentation option
      // serializer.setFeature(name,
      // state)("http://xmlpull.org/v1/doc/features.htmlindent-output", true);

      // start a tag called "root"
      serializer.startTag(null, "xml");
      if (fileName.equalsIgnoreCase("DefaultServerList.xml")) {
        serializer.startTag(null, "version");
        serializer.attribute(null, "number", appVersion);
        serializer.endTag(null, "version");
      }

      serializer.startTag(null, "Servers");

      // i indent code just to have a view similar to xml-tree
      for (int i = 0; i < objList.size(); i++) {
        ServerObjInfo serverObj = objList.get(i);
        serializer.startTag(null, "server");
        serializer.attribute(null, "name", serverObj._strServerName);
        serializer.attribute(null, "serverURL", serverObj._strServerUrl);
        serializer.endTag(null, "server");
      }

      serializer.endTag(null, "Servers");
      serializer.endTag(null, "xml");

      serializer.endDocument();

      // write xml data into the FileOutputStream
      serializer.flush();
      // finally we close the file stream

      fileos.close();
      return true;

    } catch (IOException e) {
      return false;
    }

  }

}
