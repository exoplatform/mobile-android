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
package org.exoplatform.ui.social;

import android.support.v4.view.ViewPager;

/**
 * A PageIndicator is responsible to show an visual indicator on the total views
 * number and the current visible view.
 */
public interface PageIndicator extends ViewPager.OnPageChangeListener {
    /**
     * Bind the indicator to a ViewPager.
     * 
     * @param view
     */
    void setViewPager(ViewPager view);

    /**
     * Bind the indicator to a ViewPager.
     * 
     * @param view
     * @param initialPosition
     */
    void setViewPager(ViewPager view, int initialPosition);

    /**
     * <p>
     * Set the current page of both the ViewPager and indicator.
     * </p>
     * <p>
     * This <strong>must</strong> be used if you need to set the page before the
     * views are drawn on screen (e.g., default start page).
     * </p>
     * 
     * @param item
     */
    void setCurrentItem(int item);

    /**
     * Set a page change listener which will receive forwarded events.
     * 
     * @param listener
     */
    void setOnPageChangeListener(ViewPager.OnPageChangeListener listener);

    /**
     * Notify the indicator that the fragment list has changed.
     */
    void notifyDataSetChanged();
}
