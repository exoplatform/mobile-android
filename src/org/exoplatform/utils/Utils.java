/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author :  MinhTDH
 *           MinhTDH@exoplatform.com
 * Jul 14, 2015
 * utilities class for common use code  
 */
public class Utils {
  
  public static int getSize(Collection<?> list) {
      return list != null ? list.size() : 0 ;
  }

  public static <E> E getItem(List<E> list, int pos) {
      return list == null ? null : list.get(pos);
  }
  
  public static <T> T getVal(WeakReference<T> ref) {
      return ref == null ? null : ref.get();
  }
}
