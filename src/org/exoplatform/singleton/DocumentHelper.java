/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.singleton;

import org.exoplatform.model.ExoFile;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jan
 * 31, 2012
 */
public class DocumentHelper {

  private static DocumentHelper documentHelper = new DocumentHelper();

  private ExoFile               _fileCopied;

  private ExoFile               _fileMoved;

  private DocumentHelper() {

  }

  public static DocumentHelper getInstance() {
    return documentHelper;
  }

  public void setFileCopy(ExoFile cFile) {
    _fileCopied = cFile;
  }

  public ExoFile getFileCopy() {
    return _fileCopied;
  }

  public void setFileMove(ExoFile mFile) {
    _fileMoved = mFile;
  }

  public ExoFile getFileMove() {
    return _fileMoved;
  }

}
