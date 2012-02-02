package org.exoplatform.model;

import java.util.Date;

public class ExoFile {

  public String  path;         // File's jcr url

  public boolean isFolder;     // Is folder

  public String  name;         // name of the file/folder

  public boolean canAddChild;  // can add new file or folder as it
                                // content

  public boolean canRemove;    // can remove the file/folder

  public String  currentFolder; // the path of file

  public String  driveName;    // drive name of file

  public boolean hasChild;     // if the folder contains any files or
                                // folders

  public String  workspaceName; // work space of file

  public String  nodeType;     // file content type

  public String  creator;      // Name of the one who created the file

  public Date    dateCreated;  // the time that the file is created

  public Date    dateModified; // the time that the file is modified

  public int     size;         // size of file

  // Default constructors
  public ExoFile() {
  }

  public ExoFile(String url, String _name, boolean folder) {
    path = url;
    name = _name;
    isFolder = folder;
  }

}
