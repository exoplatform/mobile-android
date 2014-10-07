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
 * @author Square Inc RobolectricTestRunner extension used to load resources
 *         from apklib dependencies in Maven projects
 *         http://stackoverflow.com/questions
 *         /16907838/robolectric-2-x-maven-on-jenkins
 *         -failed-with-apklib-dependencies
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
