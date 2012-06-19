package org.exoplatform.utils;

import greendroid.util.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.exoplatform.R;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.ui.WebViewActivity;
import org.exoplatform.widget.UnreadableFileDialog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ExoDocumentUtils {

  public static final String ALL_VIDEO_TYPE       = "video/*";

  public static final String ALL_AUDIO_TYPE       = "audio/*";

  public static final String IMAGE_TYPE           = "image";

  public static final String TEXT_TYPE            = "text";

  public static final String VIDEO_TYPE           = "video";

  public static final String AUDIO_TYPE           = "audio";

  public static final String MSWORD_TYPE          = "application/msword";

  public static final String OPEN_MSWORD_TYPE     = "application/vnd.oasis.opendocument.text";

  public static final String PDF_TYPE             = "application/pdf";

  public static final String XLS_TYPE             = "application/xls";

  public static final String OPEN_XLS_TYPE        = "application/vnd.oasis.opendocument.spreadsheet";

  public static final String POWERPOINT_TYPE      = "application/vnd.ms-powerpoint";

  public static final String OPEN_POWERPOINT_TYPE = "application/vnd.oasis.opendocument.presentation";

  /*
   * If content type is image or text, open it in WebView else open in other
   * installed application
   */

  public static void fileOpen(Context context, String fileType, String filePath, String fileName) {
    if (fileType != null && (fileType.startsWith(IMAGE_TYPE) || fileType.startsWith(TEXT_TYPE))) {
      Intent intent = new Intent(context, WebViewActivity.class);
      intent.putExtra(ExoConstants.WEB_VIEW_URL, filePath);
      intent.putExtra(ExoConstants.WEB_VIEW_TITLE, fileName);
      context.startActivity(intent);
    } else {
      new UnreadableFileDialog(context, fileType, filePath, fileName).show();
    }

  }

  /*
   * Check if file can readable or not
   */
  public static boolean isFileReadable(String type) {
    if (type.equals(PDF_TYPE) || type.equals(MSWORD_TYPE) || type.equals(XLS_TYPE)
        || type.equals(POWERPOINT_TYPE) || type.equals(OPEN_MSWORD_TYPE)
        || type.equals(OPEN_XLS_TYPE) || type.equals(OPEN_POWERPOINT_TYPE)
        || type.startsWith(AUDIO_TYPE) || type.startsWith(VIDEO_TYPE)) {
      return true;
    }

    return false;
  }

  public static boolean putFileToServerFromLocal(String url, File fileManager, String fileType) {
    try {
      url = url.replaceAll(" ", "%20");
      HttpPut put = new HttpPut(url);
      FileEntity fileEntity = new FileEntity(fileManager, fileType);
      put.setEntity(fileEntity);
      fileEntity.setContentType(fileType);
      HttpResponse response = ExoConnectionUtils.httpClient.execute(put);
      int status = response.getStatusLine().getStatusCode();
      if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
        return true;
      } else {
        return false;
      }
    } catch (IOException e) {
      return false;
    } finally {
      fileManager.delete();
    }

  }

  public static void setRepositoryHomeUrl(String userName, String domain) {

    StringBuffer buffer = new StringBuffer();
    buffer.append(domain);
    buffer.append(ExoConstants.DOCUMENT_PATH);

    int length = userName.length();
    if (length < 4) {
      for (int i = 1; i < length; i++) {
        String userNameLevel = userName.substring(0, i);
        buffer.append("/");
        buffer.append(userNameLevel);
        buffer.append("___");
      }
    } else {
      for (int i = 1; i < 4; i++) {
        String userNameLevel = userName.substring(0, i);
        buffer.append("/");
        buffer.append(userNameLevel);
        buffer.append("___");
      }
    }

    buffer.append("/");
    buffer.append(userName);

    try {
      WebdavMethod copy = new WebdavMethod("HEAD", buffer.toString());
      int status = ExoConnectionUtils.httpClient.execute(copy).getStatusLine().getStatusCode();

      if (status >= 200 && status < 300) {
        DocumentHelper.getInstance().setRepositoryHomeUrl(buffer.toString());
      } else {
        buffer = new StringBuffer(domain);
        buffer.append(ExoConstants.DOCUMENT_PATH);
        buffer.append("/");
        buffer.append(userName);
        DocumentHelper.getInstance().setRepositoryHomeUrl(buffer.toString());
      }

    } catch (IOException e) {
      DocumentHelper.getInstance().setRepositoryHomeUrl(null);
    }

  }

  // Get file array from URL
  public static ArrayList<ExoFile> getPersonalDriveContent(ExoFile file) throws IOException {
    ArrayList<ExoFile> arrFilesTmp = new ArrayList<ExoFile>();
    String domain = AccountSetting.getInstance().getDomainName();
    HttpResponse response = null;
    String urlStr = null;
    /*
     * Put the current folder and its child list to mapping dictionary
     */
    if (DocumentHelper.getInstance().childFilesMap == null) {
      DocumentHelper.getInstance().childFilesMap = new Bundle();
    }

    if (file.name == "") {
      // personal
      StringBuffer buffer = new StringBuffer();
      arrFilesTmp.add(new ExoFile());
      buffer.append(domain);
      buffer.append(ExoConstants.DOCUMENT_DRIVE_PATH_REST);
      buffer.append("personal");
      urlStr = buffer.toString();
      response = ExoConnectionUtils.getRequestResponse(urlStr);
      arrFilesTmp.addAll(getDrives(response));
      // group
      arrFilesTmp.add(new ExoFile());
      buffer = new StringBuffer();
      buffer.append(domain);
      buffer.append(ExoConstants.DOCUMENT_DRIVE_PATH_REST);
      buffer.append("group");
      urlStr = buffer.toString();
      response = ExoConnectionUtils.getRequestResponse(urlStr);
      arrFilesTmp.addAll(getDrives(response));
      if (DocumentHelper.getInstance().childFilesMap.containsKey(ExoConstants.DOCUMENT_PATH)) {
        DocumentHelper.getInstance().childFilesMap.remove(ExoConstants.DOCUMENT_PATH);
        DocumentHelper.getInstance().childFilesMap.putParcelableArrayList(ExoConstants.DOCUMENT_PATH,
                                                                          arrFilesTmp);
      } else {
        DocumentHelper.getInstance().childFilesMap.putParcelableArrayList(ExoConstants.DOCUMENT_PATH,
                                                                          arrFilesTmp);
      }

    } else {
      urlStr = getDriverUrl(file);
      urlStr = URLAnalyzer.encodeUrl(urlStr);
      response = ExoConnectionUtils.getRequestResponse(urlStr);
      arrFilesTmp.addAll(getContentOfFolder(response, file));
      if (file.path == "") {
        DocumentHelper.getInstance().childFilesMap.putParcelableArrayList(file.path, arrFilesTmp);
      } else if (DocumentHelper.getInstance().childFilesMap.containsKey(file.path)) {
        DocumentHelper.getInstance().childFilesMap.remove(file.path);
        DocumentHelper.getInstance().childFilesMap.putParcelableArrayList(file.path, arrFilesTmp);
      } else {
        DocumentHelper.getInstance().childFilesMap.putParcelableArrayList(file.path, arrFilesTmp);
      }
    }

    return arrFilesTmp;

  }

  public static String fullURLofFile(String url) {
    String domain = AccountSetting.getInstance().getDomainName();
    StringBuffer buffer = new StringBuffer(domain);
    buffer.append(ExoConstants.DOCUMENT_JCR_PATH_REST);
    buffer.append(url);
    return buffer.toString();

  }

  public static ArrayList<ExoFile> getDrives(HttpResponse response) {
    // Initialize the blogEntries MutableArray that we declared in the header
    ArrayList<ExoFile> folderArray = new ArrayList<ExoFile>();

    try {
      Document obj_doc = null;
      DocumentBuilderFactory doc_build_fact = null;
      DocumentBuilder doc_builder = null;

      doc_build_fact = DocumentBuilderFactory.newInstance();
      doc_builder = doc_build_fact.newDocumentBuilder();
      InputStream is = ExoConnectionUtils.sendRequest(response);
      if (is != null) {
        obj_doc = doc_builder.parse(is);

        NodeList obj_nod_list = null;
        if (null != obj_doc) {
          obj_nod_list = obj_doc.getElementsByTagName("Folder");

          for (int i = 0; i < obj_nod_list.getLength(); i++) {
            Node itemNode = obj_nod_list.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
              Element itemElement = (Element) itemNode;
              ExoFile file = new ExoFile();
              file.name = itemElement.getAttribute("name");
              if (Config.GD_INFO_LOGS_ENABLED)
                Log.i(" Public file name", file.name);
              file.workspaceName = itemElement.getAttribute("workspaceName");
              file.driveName = file.name;
              file.currentFolder = itemElement.getAttribute("currentFolder");
              if (file.currentFolder == null)
                file.currentFolder = "";
              file.isFolder = true;
              /*
               * If file name is "Public", get path for it.
               */
              if (file.name.equals("Public")) {
                file.path = getRootDriverPath(file);
              }
              folderArray.add(file);
            }
          }
        }
      }
    } catch (ParserConfigurationException e) {
      folderArray = null;
    } catch (SAXException e) {
      folderArray = null;
    } catch (IOException e) {
      folderArray = null;
    }

    return folderArray;
  }

  // return the driver url
  private static String getDriverUrl(ExoFile file) {
    String domain = AccountSetting.getInstance().getDomainName();
    StringBuffer buffer = new StringBuffer(domain);
    buffer.append(ExoConstants.DOCUMENT_FILE_PATH_REST);
    buffer.append(file.driveName);
    buffer.append(ExoConstants.DOCUMENT_WORKSPACE_NAME);
    buffer.append(file.workspaceName);
    buffer.append(ExoConstants.DOCUMENT_CURRENT_FOLDER);
    buffer.append(file.currentFolder);

    return buffer.toString();
  }

  // get path for driver folder (ex. Public/Private)
  private static String getRootDriverPath(ExoFile file) {
    String path = null;
    String urlStr = getDriverUrl(file);
    urlStr = URLAnalyzer.encodeUrl(urlStr);
    Document obj_doc = null;
    DocumentBuilderFactory doc_build_fact = null;
    DocumentBuilder doc_builder = null;
    try {
      HttpResponse response = ExoConnectionUtils.getRequestResponse(urlStr);
      doc_build_fact = DocumentBuilderFactory.newInstance();
      doc_builder = doc_build_fact.newDocumentBuilder();
      InputStream is = ExoConnectionUtils.sendRequest(response);
      if (is != null) {
        obj_doc = doc_builder.parse(is);

        if (null != obj_doc) {

          // Get folders
          NodeList obj_nod_list = obj_doc.getElementsByTagName("Folder");
          Node rootNode = obj_nod_list.item(0);
          if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
            Element itemElement = (Element) rootNode;
            path = fullURLofFile(itemElement.getAttribute("path"));
          }
        }
      }
      return path;
    } catch (ParserConfigurationException e) {
      return null;
    } catch (SAXException e) {
      return null;
    } catch (IOException e) {
      return null;
    }
  }

  public static ArrayList<ExoFile> getContentOfFolder(HttpResponse response, ExoFile file) {

    // Initialize the blogEntries MutableArray that we declared in the header
    ArrayList<ExoFile> folderArray = new ArrayList<ExoFile>();

    Document obj_doc = null;
    DocumentBuilderFactory doc_build_fact = null;
    DocumentBuilder doc_builder = null;
    try {
      doc_build_fact = DocumentBuilderFactory.newInstance();
      doc_builder = doc_build_fact.newDocumentBuilder();
      InputStream is = ExoConnectionUtils.sendRequest(response);
      if (is != null) {
        obj_doc = doc_builder.parse(is);

        NodeList obj_nod_list = null;
        if (null != obj_doc) {

          // Get folders
          obj_nod_list = obj_doc.getElementsByTagName("Folder");

          for (int i = 0; i < obj_nod_list.getLength(); i++) {
            Node itemNode = obj_nod_list.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
              Element itemElement = (Element) itemNode;
              if (i > 0) {
                ExoFile newFile = new ExoFile();
                newFile.name = itemElement.getAttribute("name");
                newFile.path = fullURLofFile(itemElement.getAttribute("path"));
                newFile.workspaceName = itemElement.getAttribute("workspaceName");
                newFile.driveName = itemElement.getAttribute("driveName");
                newFile.currentFolder = itemElement.getAttribute("currentFolder");
                if (newFile.currentFolder == null)
                  newFile.currentFolder = "";
                newFile.isFolder = true;

                folderArray.add(newFile);
              }

            }
          }

          // Get files
          obj_nod_list = obj_doc.getElementsByTagName("File");

          for (int i = 0; i < obj_nod_list.getLength(); i++) {
            Node itemNode = obj_nod_list.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
              Element itemElement = (Element) itemNode;

              ExoFile newFile = new ExoFile();
              newFile.path = fullURLofFile(itemElement.getAttribute("path"));
              newFile.name = itemElement.getAttribute("name");
              newFile.workspaceName = itemElement.getAttribute("workspaceName");
              newFile.driveName = file.name;
              newFile.currentFolder = itemElement.getAttribute("currentFolder");
              newFile.nodeType = itemElement.getAttribute("nodeType");
              newFile.isFolder = false;

              folderArray.add(newFile);
            }
          }

        }
      }
      return folderArray;
    } catch (ParserConfigurationException e) {
      return null;
    } catch (SAXException e) {
      return null;
    } catch (IOException e) {
      return null;
    }

  }

  /*
   * Get document icon from content type
   */

  public static int getIconFromType(String contentType) {
    int id = R.drawable.documenticonforunknown;
    if (contentType != null) {
      if (contentType.indexOf(IMAGE_TYPE) >= 0)
        id = R.drawable.documenticonforimage;
      else if (contentType.indexOf(VIDEO_TYPE) >= 0)
        id = R.drawable.documenticonforvideo;
      else if (contentType.indexOf(AUDIO_TYPE) >= 0)
        id = R.drawable.documenticonformusic;
      else if (contentType.indexOf(MSWORD_TYPE) >= 0 || contentType.indexOf(OPEN_MSWORD_TYPE) >= 0)
        id = R.drawable.documenticonforword;
      else if (contentType.indexOf(PDF_TYPE) >= 0)
        id = R.drawable.documenticonforpdf;
      else if (contentType.indexOf(XLS_TYPE) >= 0 || contentType.indexOf(OPEN_XLS_TYPE) >= 0)
        id = R.drawable.documenticonforxls;
      else if (contentType.indexOf(POWERPOINT_TYPE) >= 0
          || contentType.indexOf(OPEN_POWERPOINT_TYPE) >= 0)
        id = R.drawable.documenticonforppt;
      else if (contentType.indexOf(TEXT_TYPE) >= 0)
        id = R.drawable.documenticonfortxt;
    }

    return id;
  }

  public static String getParentUrl(String url) {

    int index = url.lastIndexOf("/");
    if (index > 0)
      return url.substring(0, index);

    return "";
  }

  public static String getLastPathComponent(String url) {

    int index = url.lastIndexOf("/");
    if (index > 0)
      return url.substring(url.lastIndexOf("/") + 1, url.length());

    return url;

  }

  public static boolean isContainSpecialChar(String str, String charSet) {

    Pattern patt = Pattern.compile(charSet);
    Matcher matcher = patt.matcher(str);
    return matcher.find();
  }

  // Delete file/folder method
  public static boolean deleteFile(String url) {
    HttpResponse response;
    try {
      url = URLAnalyzer.encodeUrl(url);
      WebdavMethod delete = new WebdavMethod("DELETE", url);
      response = ExoConnectionUtils.httpClient.execute(delete);
      int status = response.getStatusLine().getStatusCode();
      if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
        return true;
      } else
        return false;

    } catch (IOException e) {
      return false;
    }

  }

  // Copy file/folder method
  public static boolean copyFile(String source, String destination) {

    HttpResponse response;
    try {
      if (source.equals(destination)) {
        return false;
      }
      source = URLAnalyzer.encodeUrl(source);
      destination = URLAnalyzer.encodeUrl(destination);
      WebdavMethod copy = new WebdavMethod("COPY", source, destination);
      response = ExoConnectionUtils.httpClient.execute(copy);
      int status = response.getStatusLine().getStatusCode();
      if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
        return true;
      } else
        return false;

    } catch (IOException e) {
      return false;
    }
  }

  // Move file/folder method
  public static boolean moveFile(String source, String destination) {
    HttpResponse response;
    try {
      if (source.equals(destination)) {
        return false;
      }
      source = URLAnalyzer.encodeUrl(source);
      destination = URLAnalyzer.encodeUrl(destination);
      WebdavMethod move = new WebdavMethod("MOVE", source, destination);
      response = ExoConnectionUtils.httpClient.execute(move);
      int status = response.getStatusLine().getStatusCode();
      if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
        return true;
      } else
        return false;

    } catch (IOException e) {
      return false;
    }

  }

  public static boolean renameFolder(String source, String destination) {
    HttpResponse response;
    try {
      source = URLAnalyzer.encodeUrl(source);
      destination = URLAnalyzer.encodeUrl(destination);
      WebdavMethod create = new WebdavMethod("HEAD", destination);
      response = ExoConnectionUtils.httpClient.execute(create);
      int status = response.getStatusLine().getStatusCode();
      if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
        return true;
      } else {
        WebdavMethod move = new WebdavMethod("MOVE", source, destination);
        response = ExoConnectionUtils.httpClient.execute(move);
        status = response.getStatusLine().getStatusCode();
        if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
          return true;
        } else {
          return false;
        }

      }
    } catch (IOException e) {
      return false;
    }

  }

  public static boolean createFolder(String destination) {
    HttpResponse response;
    try {

      destination = URLAnalyzer.encodeUrl(destination);
      WebdavMethod create = new WebdavMethod("HEAD", destination);
      response = ExoConnectionUtils.httpClient.execute(create);
      int status = response.getStatusLine().getStatusCode();
      if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
        return true;
      } else {
        create = new WebdavMethod("MKCOL", destination);
        response = ExoConnectionUtils.httpClient.execute(create);
        status = response.getStatusLine().getStatusCode();

        if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
          return true;
        } else
          return false;
      }

    } catch (IOException e) {
      return false;
    }
  }
}
