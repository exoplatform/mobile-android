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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.exoplatform.R;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.ui.WebViewActivity;
import org.exoplatform.utils.CompatibleFileOpen.FileOpenRequest;
import org.exoplatform.utils.CompatibleFileOpen.FileOpenRequestResult;
import org.exoplatform.widget.UnreadableFileDialog;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.webkit.MimeTypeMap;

public class ExoDocumentUtils {

  private static final String  LOG_TAG              = ExoDocumentUtils.class.getName();

  public static final String   ALL_VIDEO_TYPE       = "video/*";

  public static final String   ALL_AUDIO_TYPE       = "audio/*";

  public static final String   ALL_IMAGE_TYPE       = "image/*";

  public static final String   ALL_TEXT_TYPE        = "text/*";

  public static final String   IMAGE_TYPE           = "image";

  public static final String   TEXT_TYPE            = "text";

  public static final String   VIDEO_TYPE           = "video";

  public static final String   AUDIO_TYPE           = "audio";

  public static final String   MSWORD_TYPE          = "application/msword";

  public static final String   OPEN_WORD_TYPE       = "application/vnd.oasis.opendocument.text";

  public static final String   PDF_TYPE             = "application/pdf";

  public static final String   XLS_TYPE             = "application/xls";

  public static final String   OPEN_XLS_TYPE        = "application/vnd.oasis.opendocument.spreadsheet";

  public static final String   POWERPOINT_TYPE      = "application/vnd.ms-powerpoint";

  public static final String   OPEN_POWERPOINT_TYPE = "application/vnd.oasis.opendocument.presentation";

  public static final String[] FORBIDDEN_TYPES      = new String[] { "application/octet-stream" };

  public static boolean isEnoughMemory(int fileSize) {
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      int freeSpace = getFreeMemory(Environment.getExternalStorageDirectory().getAbsolutePath());
      return (freeSpace > fileSize);
    } else
      return false;
  }

  /*
   * Get free memory from path
   */
  public static int getFreeMemory(String path) {
    StatFs statFs = new StatFs(path);
    int free = (statFs.getAvailableBlocks() * statFs.getBlockSize());
    return Math.abs(free);
  }

  /*
   * Display file size by string decimal format
   */
  public static String getFileSize(long fileSize) {
    int freeUnit;
    for (freeUnit = 0; fileSize >= 100; freeUnit++) {
      fileSize /= 1024;
    }
    DecimalFormat decFormat = new DecimalFormat("0.0");
    String doubleString = decFormat.format(fileSize);
    StringBuffer buffer = new StringBuffer();
    buffer.append(doubleString);
    switch (freeUnit) {
    case 0:
      buffer.append("B");
      break;
    case 1:
      buffer.append("KB");
      break;
    case 2:
      buffer.append("MB");
      break;
    case 3:
      buffer.append("GB");
      break;
    case 4:
      buffer.append("TB");
      break;
    default:
      buffer.append("err");
      break;
    }
    return buffer.toString();
  }

  /*
   * If content type is image or text, open it in WebView else open in other
   * installed application
   */

  public static FileOpenRequest fileOpen(Context context, String fileType, String filePath, String fileName) {
    FileOpenRequest result = new FileOpenRequest();
    if (fileType == null) {
      new UnreadableFileDialog(context, null).show();
      result.mResult = FileOpenRequestResult.ERROR;
    } else if (fileType.startsWith(IMAGE_TYPE) || fileType.startsWith(TEXT_TYPE)) {
      Intent intent = new Intent(context, WebViewActivity.class);
      intent.putExtra(ExoConstants.WEB_VIEW_URL, filePath);
      intent.putExtra(ExoConstants.WEB_VIEW_TITLE, fileName);
      intent.putExtra(ExoConstants.WEB_VIEW_MIME_TYPE, fileType);
      intent.putExtra(ExoConstants.WEB_VIEW_ALLOW_JS, "false");
      context.startActivity(intent);
      result.mResult = FileOpenRequestResult.WEBVIEW;
    } else {
      result.mFileOpenController = new CompatibleFileOpen(context, fileType, filePath, fileName);
      result.mResult = FileOpenRequestResult.EXTERNAL;
    }
    Log.d(LOG_TAG, "File Open Result: " + result.mResult);
    return result;
  }

  /**
   * Check whether the given Mime Type is forbidden. The list of forbidden types
   * is in {@link ExoDocumentUtils#FORBIDDEN_TYPES}
   * 
   * @param mimeType
   * @return true if the given Mime Type is in the list
   */
  public static boolean isForbidden(String mimeType) {
    return Arrays.asList(FORBIDDEN_TYPES).contains(mimeType);
  }

  /**
   * Check if the device has an application to open this type of file.
   * 
   * @param context Context (mandatory)
   * @param mimeType Mime Type to check (mandatory)
   * @param url file url to guess the mime type from (optional)
   * @return true if an application can open the given Mime Type
   */
  public static boolean isCallable(Context context, String mimeType, String url) throws IllegalArgumentException {
    if (context == null || mimeType == null)
      throw new IllegalArgumentException("Context or mime-type cannot be null.");

    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setType(mimeType.toLowerCase(Locale.US));
    ComponentName activity = intent.resolveActivity(context.getPackageManager());

    if (activity == null && url != null) {
      // Fallback on a guessed mime type if the first one doesn't work
      String guessedMimeType = URLConnection.guessContentTypeFromName(url);
      if (guessedMimeType != null) {
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setType(guessedMimeType.toLowerCase(Locale.US));
        activity = intent.resolveActivity(context.getPackageManager());
      }
    }

    return activity != null;
  }

  public static String getFullFileType(String fileType) {
    String docFileType = fileType;
    if (fileType.startsWith(ExoDocumentUtils.AUDIO_TYPE)) {
      docFileType = ExoDocumentUtils.ALL_AUDIO_TYPE;
    } else if (fileType.startsWith(ExoDocumentUtils.VIDEO_TYPE)) {
      docFileType = ExoDocumentUtils.ALL_VIDEO_TYPE;
    } else if (fileType.startsWith(ExoDocumentUtils.IMAGE_TYPE)) {
      docFileType = ExoDocumentUtils.ALL_IMAGE_TYPE;
    } else if (fileType.startsWith(ExoDocumentUtils.TEXT_TYPE)) {
      docFileType = ExoDocumentUtils.ALL_TEXT_TYPE;
    }
    return docFileType;
  }

  /**
   * Returns the mimetype of the document located at the given URL.<br/>
   * Returns null if the mimetype is unknown (e.g. the URL points to a page).
   *
   * @param url the URL to check
   * @return
   */
  public static String mimeTypeFromUrl(String url) {
    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
    if (extension != null) {
      return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.US));
    }
    return null;
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
      return status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES;
    } catch (IOException e) {
      Log.d(ExoDocumentUtils.class.getSimpleName(), e.getMessage(), Log.getStackTraceString(e));
      return false;
    } finally {
      fileManager.delete();
    }

  }

  public static void setRepositoryHomeUrl(String userName, String userHomeNodePath, String domain) {
    String documentPath = getDocumenPath();
    StringBuilder buffer = new StringBuilder();
    buffer.append(domain);
    buffer.append(documentPath);
    buffer.append(userHomeNodePath);

    try {
      WebdavMethod copy = new WebdavMethod("HEAD", buffer.toString());
      int status = ExoConnectionUtils.httpClient.execute(copy).getStatusLine().getStatusCode();

      if (status >= 200 && status < 300) {
        DocumentHelper.getInstance().setRepositoryHomeUrl(buffer.toString());
      } else {
        buffer = new StringBuilder(domain);
        buffer.append(documentPath);
        buffer.append("/");
        buffer.append(userName);
        DocumentHelper.getInstance().setRepositoryHomeUrl(buffer.toString());
      }

    } catch (Exception e) {
      // XXX cannot replace because WebdavMethod, httpclient.execute can throw
      // exception
      Log.e(LOG_TAG, e.getMessage(), Log.getStackTraceString(e));
      DocumentHelper.getInstance().setRepositoryHomeUrl(null);
    }
  }

  /**
   * Get the content (files and folders) of the given folder.
   * 
   * @param context
   * @param file the folder to get content from
   * @return an ExoFile corresponding to the parent folder with its children
   *         ExoFile
   * @throws IOException
   */
  public static ExoFile getPersonalDriveContent(Context context, ExoFile file) throws IOException {
    SharedPreferences prefs = context.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);
    boolean isShowHidden = prefs.getBoolean(AccountSetting.getInstance().documentKey, true);
    ExoFile folder = file;
    String domain = AccountSetting.getInstance().getDomainName();
    HttpResponse response = null;
    String urlStr = null;
    /*
     * Put the current folder and its child list to mapping dictionary
     */
    if (DocumentHelper.getInstance().folderToChildrenMap == null) {
      DocumentHelper.getInstance().folderToChildrenMap = new Bundle();
    }

    // We're on the initial screen => list all drives
    if ("".equals(file.name) && "".equals(file.path)) {
      // personal drive
      ArrayList<ExoFile> arrFilesTmp = new ArrayList<ExoFile>();
      ArrayList<ExoFile> fileList = new ArrayList<ExoFile>();
      StringBuffer buffer = new StringBuffer();
      buffer.append(domain);
      buffer.append(ExoConstants.DOCUMENT_DRIVE_PATH_REST);
      buffer.append(ExoConstants.DOCUMENT_PERSONAL_DRIVER);
      buffer.append(ExoConstants.DOCUMENT_PERSONAL_DRIVER_SHOW_PRIVATE);
      buffer.append(isShowHidden);
      urlStr = buffer.toString();
      response = ExoConnectionUtils.getRequestResponse(urlStr);
      fileList = getDrives(response);
      if (fileList != null && fileList.size() > 0) {
        arrFilesTmp.add(new ExoFile(ExoConstants.DOCUMENT_PERSONAL_DRIVER));
        arrFilesTmp.addAll(fileList);
      }
      // general drive
      buffer = new StringBuffer();
      buffer.append(domain);
      buffer.append(ExoConstants.DOCUMENT_DRIVE_PATH_REST);
      buffer.append(ExoConstants.DOCUMENT_GENERAL_DRIVER);
      urlStr = buffer.toString();
      response = ExoConnectionUtils.getRequestResponse(urlStr);
      fileList = getDrives(response);
      if (fileList != null && fileList.size() > 0) {
        arrFilesTmp.add(new ExoFile(ExoConstants.DOCUMENT_GENERAL_DRIVER));
        arrFilesTmp.addAll(fileList);
      }

      // group drive
      buffer = new StringBuffer();
      buffer.append(domain);
      buffer.append(ExoConstants.DOCUMENT_DRIVE_PATH_REST);
      buffer.append(ExoConstants.DOCUMENT_GROUP_DRIVER);
      urlStr = buffer.toString();
      response = ExoConnectionUtils.getRequestResponse(urlStr);
      // "true" to generate the natural name for the folders in the group
      fileList = getDrives(response, true);
      if (fileList != null && fileList.size() > 0) {
        arrFilesTmp.add(new ExoFile(ExoConstants.DOCUMENT_GROUP_DRIVER));
        arrFilesTmp.addAll(fileList);
      }

      // store the drives root folders
      if (DocumentHelper.getInstance().folderToChildrenMap.containsKey(ExoConstants.DOCUMENT_JCR_PATH)) {
        DocumentHelper.getInstance().folderToChildrenMap.remove(ExoConstants.DOCUMENT_JCR_PATH);
        DocumentHelper.getInstance().folderToChildrenMap.putParcelableArrayList(ExoConstants.DOCUMENT_JCR_PATH, arrFilesTmp);
      } else {
        DocumentHelper.getInstance().folderToChildrenMap.putParcelableArrayList(ExoConstants.DOCUMENT_JCR_PATH, arrFilesTmp);
      }

      // create an empty root folder to hold all the drives
      folder.children = arrFilesTmp;
    } else {
      // We're in a drive or folder => list its content
      urlStr = getDriverUrl(file);
      urlStr = ExoUtils.encodeDocumentUrl(urlStr);
      response = ExoConnectionUtils.getRequestResponse(urlStr);
      folder = getContentOfFolder(response, file);
      // store the children of the loaded folder
      if (DocumentHelper.getInstance().folderToChildrenMap.containsKey(file.path)) {
        DocumentHelper.getInstance().folderToChildrenMap.remove(file.path);
        DocumentHelper.getInstance().folderToChildrenMap.putParcelableArrayList(file.path,
                                                                                new ArrayList<ExoFile>(folder.children));
      } else
        DocumentHelper.getInstance().folderToChildrenMap.putParcelableArrayList(file.path,
                                                                                new ArrayList<ExoFile>(folder.children));

    }

    return folder;

  }

  public static String fullURLofFile(String workSpaceName, String url) {
    String domain = AccountSetting.getInstance().getDomainName();
    StringBuffer buffer = new StringBuffer(domain);
    buffer.append(ExoConstants.DOCUMENT_JCR_PATH);
    buffer.append("/");
    buffer.append(DocumentHelper.getInstance().repository);
    buffer.append("/");
    buffer.append(workSpaceName);
    buffer.append(url);
    return buffer.toString();

  }

  private static String getDocumenPath() {
    StringBuilder documentPath = new StringBuilder();
    documentPath.append(ExoConstants.DOCUMENT_JCR_PATH);
    documentPath.append("/");
    documentPath.append(DocumentHelper.getInstance().repository);
    documentPath.append("/");
    documentPath.append(ExoConstants.DOCUMENT_COLLABORATION);
    return documentPath.toString();
  }

  /**
   * Get the list of folders in a drive, from the HTTP response.<br/>
   * The response's body is an XM document that is parsed to extract the
   * information of the folders. <br/>
   * This method simply calls
   * 
   * <pre>
   * ExoDocumentUtils.getDrives(response, false);
   * </pre>
   * 
   * @param response the HttpResponse from where to extract the list of folders
   * @return an ArrayList of ExoFile or an empty ArrayList if a problem happens
   * @see ExoDocumentUtils#getDrives(HttpResponse response, boolean
   *      isGroupDrive)
   */
  public static ArrayList<ExoFile> getDrives(HttpResponse response) {
    return getDrives(response, false);
  }

  /**
   * Get the list of folders in a drive, from the HTTP response.<br/>
   * The response's body is an XM document that is parsed to extract the
   * information of the folders. <br/>
   * If <i>isGroupDrive = true</i> , each folder's name is improved to be less
   * technical, by calling <i>ExoFile#createNaturalName()</i>
   * 
   * @param response the HttpResponse from where to extract the list of folders
   * @param isGroupDrive if <i>true</i> the file's natural name will be created
   * @return an ArrayList of ExoFile or an empty ArrayList if a problem happens
   */
  public static ArrayList<ExoFile> getDrives(HttpResponse response, boolean isGroupDrive) {
    // Initialize the blogEntries MutableArray that we declared in the
    // header
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
              // if (Config.GD_INFO_LOGS_ENABLED)
              Log.i(" Public file name", file.name);
              file.workspaceName = itemElement.getAttribute("workspaceName");
              file.driveName = file.name;
              file.currentFolder = itemElement.getAttribute("currentFolder");
              if (file.currentFolder == null)
                file.currentFolder = "";
              file.isFolder = true;
              // create the folder's natural name only for folders
              // in the group drive
              if (isGroupDrive)
                file.createNaturalName();
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
      Log.e(" ParserConfigurationException ", e.getMessage());
      folderArray = null;
    } catch (SAXException e) {
      Log.e(" SAXException ", e.getMessage());
      folderArray = null;
    } catch (IOException e) {
      Log.e(" IOException ", e.getMessage());
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
    urlStr = ExoUtils.encodeDocumentUrl(urlStr);
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
            path = fullURLofFile(ExoConstants.DOCUMENT_COLLABORATION, itemElement.getAttribute("path"));
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

  private static ExoFile getFileFromXMLElement(Element element, boolean isFolder) throws NullPointerException {
    if (element == null)
      throw new NullPointerException("Given element is null");

    ExoFile file = new ExoFile();
    if (element.hasAttribute("title")) {
      file.name = Html.fromHtml(element.getAttribute("title")).toString();
    } else {
      file.name = element.getAttribute("name");
    }
    file.workspaceName = element.getAttribute("workspaceName");
    file.path = fullURLofFile(file.workspaceName, element.getAttribute("path"));
    if (element.hasAttribute("driveName"))
      file.driveName = element.getAttribute("driveName");
    else
      file.driveName = file.name;
    file.currentFolder = element.getAttribute("currentFolder");
    if (file.currentFolder == null)
      file.currentFolder = "";
    file.isFolder = isFolder;
    if (element.hasAttribute("nodeType"))
      file.nodeType = element.getAttribute("nodeType");

    String canRemove = element.getAttribute("canRemove");
    file.canRemove = Boolean.parseBoolean(canRemove.trim());

    return file;
  }

  /**
   * Get a folder with its sub-files and sub-folders.<br/>
   * Parse the XML response with format:
   * 
   * <pre>
   * &lt;Folder canAddChild="bool" canRemove="bool" currentFolder="Name" driveName="DriveName" hasChild="bool" name="Name" nodeType="nt" path="..." title="Title" titlePath="Title" workspaceName="Name">
   *  &lt;Folders>
   *    &lt;Folder canAddChild="bool" canRemove="bool" currentFolder="Name" driveName="DriveName" hasChild="bool" name="Name" nodeType="nt" path="..." title="Title" titlePath="Title" workspaceName="Name"/>
   *  &lt;/Folders>
   *  &lt;Files>
   *    &lt;File canRemove="bool" creator="username" dateCreated="Date" dateModified="Date" name="doc.jpg" nodeType="nt" path="..." size="0" title="doc.jpg" workspaceName="Name"/>
   *  &lt;/Files>
   * &lt;/Folder>
   * </pre>
   * 
   * Example URL:
   * 
   * <pre>
   * https://SERVER/rest/managedocument/getFoldersAndFiles?driveName=Personal%
   * 20Documents&workspaceName=collaboration&currentFolder=Public
   * </pre>
   * 
   * @param response the response that contains the XML entity
   * @param file The folder to retrieve the content from
   * @return an ExoFile that represents the content of the given folder
   */
  public static ExoFile getContentOfFolder(HttpResponse response, ExoFile file) {

    ExoFile folder = file;
    ArrayList<ExoFile> childrenArray = new ArrayList<ExoFile>();

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

              if (i == 0) { // The first element is always the root folder

                // We copy properties from tmp to folder
                // to keep the pointer to the folder instance
                ExoFile tmp = getFileFromXMLElement(itemElement, true);
                // Unfortunate hack
                // The drive "Personal Documents" is a folder named "Private"
                // We do this to display the drive name in this case
                if ("Private".equals(tmp.name) && "".equals(tmp.currentFolder) && "Personal Documents".equals(tmp.driveName))
                  folder.name = tmp.driveName;
                else
                  folder.name = tmp.name;

                folder.workspaceName = tmp.workspaceName;
                folder.path = tmp.path;
                folder.driveName = tmp.driveName;
                folder.currentFolder = tmp.currentFolder;
                folder.isFolder = true;
                // Cannot delete the root folder of a drive
                if (folder.isFolder && "".equals(folder.currentFolder))
                  folder.canRemove = false;
                else
                  folder.canRemove = tmp.canRemove;

              } else { // Folders of the root folder

                ExoFile childFolder = getFileFromXMLElement(itemElement, true);
                childrenArray.add(childFolder);
              }
            }
          }

          // Get files
          obj_nod_list = obj_doc.getElementsByTagName("File");
          for (int i = 0; i < obj_nod_list.getLength(); i++) {
            Node itemNode = obj_nod_list.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
              Element itemElement = (Element) itemNode;
              ExoFile childFile = getFileFromXMLElement(itemElement, false);
              childrenArray.add(childFile);
            }
          }

        }
      }
      folder.children = childrenArray;
      return folder;
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
      else if (contentType.indexOf(MSWORD_TYPE) >= 0 || contentType.indexOf(OPEN_WORD_TYPE) >= 0)
        id = R.drawable.documenticonforword;
      else if (contentType.indexOf(PDF_TYPE) >= 0)
        id = R.drawable.documenticonforpdf;
      else if (contentType.indexOf(XLS_TYPE) >= 0 || contentType.indexOf(OPEN_XLS_TYPE) >= 0)
        id = R.drawable.documenticonforxls;
      else if (contentType.indexOf(POWERPOINT_TYPE) >= 0 || contentType.indexOf(OPEN_POWERPOINT_TYPE) >= 0)
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

  /**
   * Get the last path part of the given URL.<br/>
   * Example:
   * <ul>
   * <li>URL = http://my.server.com/path/to/file.png</li>
   * <li>Returns file.png</li>
   * </ul>
   * 
   * @param url
   * @return
   */
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

  /**
   * Delete remote folder or file
   * 
   * @param url the URL of the folder or file to delete
   * @return true if the file was deleted, false otherwise
   */
  public static boolean deleteFile(String url) {
    HttpResponse response;
    try {
      url = ExoUtils.encodeDocumentUrl(url);
      WebdavMethod delete = new WebdavMethod("DELETE", url);
      response = ExoConnectionUtils.httpClient.execute(delete);
      int status = response.getStatusLine().getStatusCode();
      return status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES;

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
      source = ExoUtils.encodeDocumentUrl(source);
      destination = ExoUtils.encodeDocumentUrl(destination);
      WebdavMethod copy = new WebdavMethod("COPY", source, destination);
      response = ExoConnectionUtils.httpClient.execute(copy);
      int status = response.getStatusLine().getStatusCode();
      return status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES;

    } catch (IOException e) {
      Log.d(ExoDocumentUtils.class.getSimpleName(), e.getMessage(), Log.getStackTraceString(e));
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
      source = ExoUtils.encodeDocumentUrl(source);
      destination = ExoUtils.encodeDocumentUrl(destination);
      WebdavMethod move = new WebdavMethod("MOVE", source, destination);
      response = ExoConnectionUtils.httpClient.execute(move);
      int status = response.getStatusLine().getStatusCode();
      return status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES;

    } catch (IOException e) {
      return false;
    }

  }

  public static boolean renameFolder(String source, String destination) {
    HttpResponse response;
    try {
      source = ExoUtils.encodeDocumentUrl(source);
      destination = ExoUtils.encodeDocumentUrl(destination);
      WebdavMethod create = new WebdavMethod("HEAD", destination);
      response = ExoConnectionUtils.httpClient.execute(create);
      int status = response.getStatusLine().getStatusCode();
      if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
        return true;
      } else {
        WebdavMethod move = new WebdavMethod("MOVE", source, destination);
        response = ExoConnectionUtils.httpClient.execute(move);
        status = response.getStatusLine().getStatusCode();
        return status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES;

      }
    } catch (IOException e) {
      return false;
    }

  }

  public static boolean createFolder(String destination) {
    HttpResponse response;
    try {

      destination = ExoUtils.encodeDocumentUrl(destination);
      WebdavMethod create = new WebdavMethod("HEAD", destination);
      response = ExoConnectionUtils.httpClient.execute(create);
      int status = response.getStatusLine().getStatusCode();
      if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
        return true;
      } else {
        create = new WebdavMethod("MKCOL", destination);
        response = ExoConnectionUtils.httpClient.execute(create);
        status = response.getStatusLine().getStatusCode();

        return status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES;
      }

    } catch (Exception e) {
      // XXX catch null of destination, WebdavMethod initial, httpclient
      // exception
      Log.e(LOG_TAG, e.getMessage(), e);
      return false;
    }
  }

  /**
   * Returns a DocumentInfo with info coming from the file at the given URI
   * 
   * @param document the URI of a file or a content
   * @param context
   * @return a DocumentInfo or null if an error occurs
   */
  public static DocumentInfo documentInfoFromUri(Uri document, Context context) {
    if (document == null)
      return null;

    if (document.toString().startsWith("content://")) {
      /*
       * Some apps send fake content:// URI with real file:// URI inside E.g.
       * open ASTRO File Manager > View File > Share :
       * content://authority/-1/1/file:///sdcard/path/file.jpg/ACTUAL/123 Then
       * we extract the real URI and pass it to documentFromFileUri(...)
       */
      String decodedUri = Uri.decode(document.toString());
      int fileIdx = decodedUri.indexOf("file://");
      if (fileIdx > -1) {
        long id = -1;
        try {
          id = ContentUris.parseId(document);
        } catch (NumberFormatException e) {
          Log.e(LOG_TAG, e.getMessage(), e);
        } catch (UnsupportedOperationException e) {
          Log.e(LOG_TAG, e.getMessage(), e);
        }
        String fileUri = decodedUri.substring(fileIdx);
        fileUri = fileUri.replaceAll("(/ACTUAL/)(" + id + ")", "");
        return documentFromFileUri(Uri.parse(fileUri));
      } else {
        return documentFromContentUri(document, context);
      }
    } else if (document.toString().startsWith("file://")) {
      return documentFromFileUri(document);
    } else {
      return null; // other formats not supported
    }
  }

  /**
   * Gets a DocumentInfo with info coming from the document at the given URI.
   * 
   * @param contentUri the content URI of the document (content:// ...)
   * @param context
   * @return a DocumentInfo or null if an error occurs
   */
  public static DocumentInfo documentFromContentUri(Uri contentUri, Context context) {
    if (contentUri == null)
      return null;

    try {
      ContentResolver cr = context.getContentResolver();
      Cursor c = cr.query(contentUri, null, null, null, null);
      int sizeIndex = c.getColumnIndex(OpenableColumns.SIZE);
      int nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
      int orientIndex = c.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION);
      c.moveToFirst();

      DocumentInfo document = new DocumentInfo();
      document.documentName = c.getString(nameIndex);
      document.documentSizeKb = c.getLong(sizeIndex) / 1024;
      document.documentData = cr.openInputStream(contentUri);
      document.documentMimeType = cr.getType(contentUri);
      if (orientIndex != -1) { // if found orientation column
        document.orientationAngle = c.getInt(orientIndex);
      }
      c.close();
      return document;
    } catch (FileNotFoundException e) {
      Log.d(LOG_TAG, e.getClass().getSimpleName(), e.getLocalizedMessage());
    } catch (Exception e) {
      Log.e(LOG_TAG, "Cannot retrieve the content at " + contentUri);
      Log.d(LOG_TAG, e.getMessage() + "\n" + Log.getStackTraceString(e));
    }
    return null;
  }

  /**
   * Gets a DocumentInfo with info coming from the file at the given URI.
   * 
   * @param fileUri the file URI (file:// ...)
   * @return a DocumentInfo or null if an error occurs
   */
  public static DocumentInfo documentFromFileUri(Uri fileUri) {
    if (fileUri == null)
      return null;

    try {
      URI uri = new URI(fileUri.toString());
      File file = new File(uri);

      DocumentInfo document = new DocumentInfo();
      document.documentName = file.getName();
      document.documentSizeKb = file.length() / 1024;
      document.documentData = new FileInputStream(file);
      // Guess the mime type in 2 ways
      try {
        // 1) by inspecting the file's first bytes
        document.documentMimeType = URLConnection.guessContentTypeFromStream(document.documentData);
      } catch (IOException e) {
        document.documentMimeType = null;
      }
      if (document.documentMimeType == null) {
        // 2) if it fails, by stripping the extension of the filename
        // and getting the mime type from it
        String extension = "";
        int dotPos = document.documentName.lastIndexOf('.');
        if (0 <= dotPos)
          extension = document.documentName.substring(dotPos + 1);
        document.documentMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
      }
      // Get the orientation angle from the EXIF properties
      if ("image/jpeg".equals(document.documentMimeType))
        document.orientationAngle = getExifOrientationAngleFromFile(file.getAbsolutePath());
      return document;
    } catch (URISyntaxException e) {
      Log.e(LOG_TAG, "Cannot retrieve the file at " + fileUri);
      Log.d(LOG_TAG, e.getMessage() + "\n" + Log.getStackTraceString(e));
    } catch (FileNotFoundException e) {
      Log.e(LOG_TAG, "Cannot retrieve the file at " + fileUri);
      Log.d(LOG_TAG, e.getMessage() + "\n" + Log.getStackTraceString(e));
    }
    return null;
  }

  /**
   * Delete the Files at the given paths
   * 
   * @param files a list of file paths
   * @return true if all files were deleted, false otherwise
   */
  public static boolean deleteLocalFiles(List<String> files) {
    boolean result = true;
    if (files != null) {
      for (String filePath : files) {
        File f = new File(filePath);
        boolean del = f.delete();
        Log.d(LOG_TAG, "File " + f.getName() + " deleted: " + (del ? "YES" : "NO"));
        result &= del;
      }
    }
    return result;
  }

  /**
   * On Platform 4.1-M2, the upload service renames the uploaded file. Therefore
   * the link to this file in the activity becomes incorrect. To fix this, we
   * rename the file before upload so the same name is used in the activity.
   * 
   * @param originalName the name to clean
   * @return a String without forbidden characters
   */
  public static String cleanupFilename(String originalName) {
    final String TILDE_HYPHENS_COLONS_SPACES = "[~_:\\s]";
    final String MULTIPLE_HYPHENS = "-{2,}";
    final String FORBIDDEN_CHARS = "[`!@#\\$%\\^&\\*\\|;\"'<>/\\\\\\[\\]\\{\\}\\(\\)\\?,=\\+\\.]+";
    String name = originalName;
    String ext = "";
    int lastDot = name.lastIndexOf('.');
    if (lastDot > 0 && lastDot < name.length()) {
      ext = name.substring(lastDot); // the ext with the dot
      name = name.substring(0, lastDot); // the name before the ext
    }
    // [~_:\s] Replaces ~ _ : and spaces by -
    name = Pattern.compile(TILDE_HYPHENS_COLONS_SPACES).matcher(name).replaceAll("-");
    // [`!@#\$%\^&\*\|;"'<>/\\\[\]\{\}\(\)\?,=\+\.]+ Deletes forbidden chars
    name = Pattern.compile(FORBIDDEN_CHARS).matcher(name).replaceAll("");
    // Converts accents to regular letters
    name = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    // Replaces upper case characters by lower case
    // Locale loc = new
    // Locale(SettingUtils.getPrefsLanguage(getApplicationContext()));
    name = name.toLowerCase(Locale.getDefault());
    // Remove consecutive -
    name = Pattern.compile(MULTIPLE_HYPHENS).matcher(name).replaceAll("-");
    // Save
    return (name + ext);
  }

  public static final int ROTATION_0   = 0;

  public static final int ROTATION_90  = 90;

  public static final int ROTATION_180 = 180;

  public static final int ROTATION_270 = 270;

  /**
   * Get the EXIF orientation of the given file
   * 
   * @param filePath
   * @return an int in ExoDocumentUtils.ROTATION_[0 , 90 , 180 , 270]
   */
  public static int getExifOrientationAngleFromFile(String filePath) {
    int ret = ROTATION_0;
    try {
      ret = new ExifInterface(filePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
      switch (ret) {
      case ExifInterface.ORIENTATION_ROTATE_90:
        ret = ROTATION_90;
        break;
      case ExifInterface.ORIENTATION_ROTATE_180:
        ret = ROTATION_180;
        break;
      case ExifInterface.ORIENTATION_ROTATE_270:
        ret = ROTATION_270;
        break;
      default:
        break;
      }
    } catch (IOException e) {
      Log.d(ExoDocumentUtils.class.getSimpleName(), e.getMessage(), Log.getStackTraceString(e));
    }
    return ret;
  }

  /**
   * Rotate the bitmap at its correct orientation
   * 
   * @param filePath the file where the bitmap is stored
   * @param source the bitmap itself
   * @return the bitmap rotated with
   *         {@link ExoDocumentUtils#rotateBitmapByAngle(Bitmap, int)}
   */
  public static Bitmap rotateBitmapToNormal(String filePath, Bitmap source) {
    Bitmap ret = source;

    int orientation = getExifOrientationAngleFromFile(filePath);
    // Sometimes we get an orientation = 1
    // To avoid a 1º rotation,
    // we rotate only when the orientation is exactly 90º or 180º or 270º
    if (orientation == ROTATION_90 || orientation == ROTATION_180 || orientation == ROTATION_270) {
      ret = rotateBitmapByAngle(source, orientation);
    }
    return ret;
  }

  /**
   * Rotate the bitmap by a certain angle. Uses {@link Matrix#postRotate(float)}
   * 
   * @param source the bitmap to rotate
   * @param angle the rotation angle
   * @return a new rotated bitmap
   */
  public static Bitmap rotateBitmapByAngle(Bitmap source, int angle) {
    Bitmap ret = source;
    int w, h;
    w = source.getWidth();
    h = source.getHeight();
    Matrix matrix = new Matrix();
    matrix.postRotate(angle);
    try {
      ret = Bitmap.createBitmap(source, 0, 0, w, h, matrix, true);
    } catch (OutOfMemoryError e) {
      Log.d(ExoDocumentUtils.class.getSimpleName(), "Exception : ", e, Log.getStackTraceString(e));
    }
    return ret;
  }

  private static String permissionForCode(int permCode) {
    String permission = null;
    switch (permCode) {
    case ExoConstants.REQUEST_TAKE_PICTURE_WITH_CAMERA:
      // We store the captured image on disk, so we need the
      // WRITE_EXTERNAL_STORAGE permission
      permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
      break;
    case ExoConstants.REQUEST_PICK_IMAGE_FROM_GALLERY:
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        // On Jelly Bean and after, return the actual READ_EXTERNAL_STORAGE
        // permission
        permission = permissionReadExternalStorage();
      else
        // Otherwise returning WRITE_EXTERNAL_STORAGE implicitly grants
        // READ_EXTERNAL_STORAGE
        permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
      break;
    default:
      throw new IllegalArgumentException("Given permission code is incorrect: " + permCode);
    }
    return permission;
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private static String permissionReadExternalStorage() {
    return Manifest.permission.READ_EXTERNAL_STORAGE;
  }

  /**
   * Check whether the application needs to request the permission required by
   * the activity. If yes, then the permission is requested (via
   * {@link ActivityCompat#requestPermissions(Activity, String[], int)}).
   * 
   * @param caller The activity that requires the permission. Must implement
   *          {@link OnRequestPermissionsResultCallback}.
   * @param permissionCode The code defined internally, e.g.
   *          {@link ExoConstants#REQUEST_PICK_IMAGE_FROM_GALLERY}.
   * @return true if the permission has been requested <br/>
   *         false if the permission was already granted
   */
  public static boolean didRequestPermission(Activity caller, int permissionCode) {
    if (caller == null || !(caller instanceof OnRequestPermissionsResultCallback))
      throw new IllegalArgumentException("Caller activity must implement OnRequestPermissionsResultCallback");

    boolean res = false;
    String permission = permissionForCode(permissionCode);
    int check = ContextCompat.checkSelfPermission(caller, permission);
    if (check != PackageManager.PERMISSION_GRANTED) {
      res = true;
      ActivityCompat.requestPermissions(caller, new String[] { permission }, permissionCode);
    }
    return res;
  }

  /**
   * Check whether the request for the specified permission should be explained
   * to the user. Calls
   * {@link ActivityCompat#shouldShowRequestPermissionRationale(Activity, String)}
   * .
   * 
   * @param activity The activity that requires the permission.
   * @param permCode The code defined internally, e.g.
   *          {@link ExoConstants#REQUEST_PICK_IMAGE_FROM_GALLERY}.
   * @return true if the user should receive more information about the
   *         permission request
   */
  public static boolean shouldDisplayExplanation(Activity activity, int permCode) {
    if (activity == null)
      throw new IllegalArgumentException("Caller activity must not be null");
    String permission = permissionForCode(permCode);
    return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
  }

  public static class DocumentInfo {

    public String      documentName;

    public long        documentSizeKb;

    public InputStream documentData;

    public String      documentMimeType;

    public int         orientationAngle = ROTATION_0;

    @Override
    public String toString() {
      return String.format(Locale.US, "File %s [%s - %s KB]", documentName, documentMimeType, documentSizeKb);
    }

    public void closeDocStream() {
      if (documentData != null)
        try {
          documentData.close();
        } catch (IOException e) {
          Log.d(LOG_TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * On Platform 4.1-M2, the upload service renames the uploaded file.
     * Therefore the link to this file in the activity becomes incorrect. To fix
     * this, we rename the file before upload so the same name is used in the
     * activity.
     */
    public void cleanupFilename(Context context) {
      final String TILDE_HYPHENS_COLONS_SPACES = "[~_:\\s]";
      final String MULTIPLE_HYPHENS = "-{2,}";
      final String FORBIDDEN_CHARS = "[`!@#\\$%\\^&\\*\\|;\"'<>/\\\\\\[\\]\\{\\}\\(\\)\\?,=\\+\\.]+";
      String name = documentName;
      String ext = "";
      int lastDot = name.lastIndexOf('.');
      if (lastDot > 0 && lastDot < name.length()) {
        ext = name.substring(lastDot); // the ext with the dot
        name = name.substring(0, lastDot); // the name before the ext
      }
      // [~_:\s] Replaces ~ _ : and spaces by -
      name = Pattern.compile(TILDE_HYPHENS_COLONS_SPACES).matcher(name).replaceAll("-");
      // [`!@#\$%\^&\*\|;"'<>/\\\[\]\{\}\(\)\?,=\+\.]+ Deletes forbidden chars
      name = Pattern.compile(FORBIDDEN_CHARS).matcher(name).replaceAll("");
      // Converts accents to regular letters
      name = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
      // Replaces upper case characters by lower case
      Locale loc = new Locale(SettingUtils.getPrefsLanguage(context.getApplicationContext()));
      name = name.toLowerCase(loc == null ? Locale.getDefault() : loc);
      // Remove consecutive -
      name = Pattern.compile(MULTIPLE_HYPHENS).matcher(name).replaceAll("-");
      // Save
      documentName = name + ext;
    }
  }
}
