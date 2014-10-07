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
package org.exoplatform.model;

//gadget info
public class GadgetInfo {
    private String _strGadgetName;       // Gadget name

    private String _strGadgetDescription; // Gadget description

    private String _strGadgetUrl;        // Gadget url

    private String _strGadgetIcon;       // Gadget icon string

    private int    _intGatdetIndex;      // index for background setting

    private String _strTabName;

    // Constructor

    public GadgetInfo(String gadgetName,
                      String gadgetDescription,
                      String gadgetUrl,
                      String strGadgetIcon,
                      String strTabName,
                      int gadgetIndex) {
        _strGadgetName = gadgetName;
        _strGadgetDescription = gadgetDescription;
        _strGadgetUrl = gadgetUrl;
        _strGadgetIcon = strGadgetIcon;
        _strTabName = strTabName;
        _intGatdetIndex = gadgetIndex;
    }

    public GadgetInfo(String tabName) {
        _strTabName = tabName;
    }

    // Gettors
    public String getGadgetName() {
        return _strGadgetName;
    }

    public String getGadgetDescription() {
        return _strGadgetDescription;
    }

    public String getGadgetUrl() {
        return _strGadgetUrl;
    }

    public void setGadgetUrl(String url) {
        _strGadgetUrl = url;
    }

    public String getStrGadgetIcon() {
        return _strGadgetIcon;
    }

    public String getTabName() {
        return _strTabName;
    }

    public int getGadgetIndex() {
        return _intGatdetIndex;
    }
}
