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
package org.exoplatform.mobile.tests;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.io.File;
import java.util.List;

import org.junit.runners.model.InitializationError;
import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

/**
 * RobolectricTestRunner extension used to load resources from apklib
 * dependencies in Maven projects http://stackoverflow.com/questions
 * /16907838/robolectric-2-x-maven-on-jenkins -failed-with-apklib-dependencies
 * 
 * @author Square Inc
 */
public class ExoRobolectricTestRunner extends RobolectricTestRunner {
    private static boolean alreadyRegisteredAbs = false;

    public ExoRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetsDir) {
        return new MavenAndroidManifest(Fs.newFile(new File(".")));
    }

    public static class MavenAndroidManifest extends AndroidManifest {
        @SuppressWarnings("deprecation")
        public MavenAndroidManifest(FsFile baseDir) {
            super(baseDir);
        }

        public MavenAndroidManifest(FsFile androidManifestFile, FsFile resDirectory) {
            super(androidManifestFile, resDirectory);
        }

        @Override
        protected List<FsFile> findLibraries() {
            // Try unpack folder from maven.
            FsFile unpack = getBaseDir().join("target/unpack/apklibs");
            if (unpack.exists()) {
                FsFile[] libs = unpack.listFiles();
                if (libs != null) {
                    return asList(libs);
                }
            }
            return emptyList();
        }

        @Override
        protected AndroidManifest createLibraryAndroidManifest(FsFile libraryBaseDir) {
            return new MavenAndroidManifest(libraryBaseDir);
        }
    }
}
