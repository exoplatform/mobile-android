/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.mobile.tests;

import org.exoplatform.ui.setting.ServerEditionActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(ExoRobolectricTestRunner.class)
public class ServerEditionActivityTest extends ExoActivityTestUtils<ServerEditionActivity>{
	
	@Override
	@Before
	public void setup() {
		controller = Robolectric.buildActivity(ServerEditionActivity.class);
	}
	
	@Test
	public void verifyDefaultLayout() {
		super.create();
	}
	
	@Test
	public void verifyCreateOneAccount() {
		super.create();
	}
	
	@Test
	public void verifyEditOneAccount() {
		super.create();
	}
	
	@Test
	public void verifyDeleteOneAccount() {
		super.create();
	}
	
	@Test
	public void verifyCreateAndEditFailWithIncorrectAccountInfo() {
		super.create();
	}
	
	@Test
	public void verifyAccountIsSelectedWhenOnlyOneExists() {
		super.create();
	}
	
}