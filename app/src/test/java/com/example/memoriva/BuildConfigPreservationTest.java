package com.example.memoriva;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Property 2: Preservation — All Other Build Configuration Remains Unchanged
 *
 * These tests observe and lock in the baseline state of all build configuration
 * and resource files BEFORE the compileSdk fix is applied. They must PASS on
 * the unfixed code (since they test parts of the build that are NOT the bug),
 * and must continue to PASS after the fix to confirm no regressions.
 *
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5
 */
public class BuildConfigPreservationTest {

    // Resolve workspace root relative to the test's working directory.
    // When run via Gradle on the host, the working directory is the app/ module directory.
    private static final File APP_DIR = resolveAppDir();
    private static final File PROJECT_ROOT = APP_DIR.getParentFile();

    private static File resolveAppDir() {
        // Gradle sets the working directory to the module root (app/) for unit tests.
        File cwd = new File(System.getProperty("user.dir"));
        // If cwd is already the project root (contains app/), step into app/
        if (new File(cwd, "app").isDirectory()) {
            return new File(cwd, "app");
        }
        return cwd;
    }

    // -------------------------------------------------------------------------
    // Property 2a: defaultConfig values in app/build.gradle.kts are unchanged
    // -------------------------------------------------------------------------

    /**
     * Asserts that app/build.gradle.kts contains minSdk = 24.
     * Validates: Requirement 3.5 (SDK settings preservation)
     */
    @Test
    public void buildGradle_containsMinSdk24() throws IOException {
        String content = readFile(new File(APP_DIR, "build.gradle.kts"));
        assertTrue(
            "app/build.gradle.kts must contain 'minSdk = 24'",
            content.contains("minSdk = 24")
        );
    }

    /**
     * Asserts that app/build.gradle.kts contains targetSdk = 36.
     * Validates: Requirement 3.5 (SDK settings preservation)
     */
    @Test
    public void buildGradle_containsTargetSdk36() throws IOException {
        String content = readFile(new File(APP_DIR, "build.gradle.kts"));
        assertTrue(
            "app/build.gradle.kts must contain 'targetSdk = 36'",
            content.contains("targetSdk = 36")
        );
    }

    /**
     * Asserts that app/build.gradle.kts contains versionCode = 1.
     * Validates: Requirement 3.5 (SDK settings preservation)
     */
    @Test
    public void buildGradle_containsVersionCode1() throws IOException {
        String content = readFile(new File(APP_DIR, "build.gradle.kts"));
        assertTrue(
            "app/build.gradle.kts must contain 'versionCode = 1'",
            content.contains("versionCode = 1")
        );
    }

    /**
     * Asserts that app/build.gradle.kts contains versionName = "1.0".
     * Validates: Requirement 3.5 (SDK settings preservation)
     */
    @Test
    public void buildGradle_containsVersionName1_0() throws IOException {
        String content = readFile(new File(APP_DIR, "build.gradle.kts"));
        assertTrue(
            "app/build.gradle.kts must contain 'versionName = \"1.0\"'",
            content.contains("versionName = \"1.0\"")
        );
    }

    // -------------------------------------------------------------------------
    // Property 2b: gradle/libs.versions.toml declares constraintlayout = "2.2.1"
    // -------------------------------------------------------------------------

    /**
     * Asserts that gradle/libs.versions.toml declares constraintlayout = "2.2.1".
     * Validates: Requirement 3.5 (dependency version preservation)
     */
    @Test
    public void libsVersionsToml_declaresConstraintLayout221() throws IOException {
        File toml = new File(PROJECT_ROOT, "gradle/libs.versions.toml");
        String content = readFile(toml);
        assertTrue(
            "gradle/libs.versions.toml must declare 'constraintlayout = \"2.2.1\"'",
            content.contains("constraintlayout = \"2.2.1\"")
        );
    }

    // -------------------------------------------------------------------------
    // Property 2c: activity_splash.xml constraint attributes are unchanged
    // -------------------------------------------------------------------------

    /**
     * Asserts that activity_splash.xml contains layout_constraintVerticalBias="0.35".
     * Validates: Requirement 3.2 (constraint attribute preservation)
     */
    @Test
    public void splashLayout_containsVerticalBias035() throws IOException {
        String content = readSplashLayout();
        assertTrue(
            "activity_splash.xml must contain 'app:layout_constraintVerticalBias=\"0.35\"'",
            content.contains("layout_constraintVerticalBias=\"0.35\"")
        );
    }

    /**
     * Asserts that activity_splash.xml contains layout_constraintTop_toTopOf.
     * Validates: Requirement 3.2 (constraint attribute preservation)
     */
    @Test
    public void splashLayout_containsConstraintTopToTopOf() throws IOException {
        String content = readSplashLayout();
        assertTrue(
            "activity_splash.xml must contain 'app:layout_constraintTop_toTopOf'",
            content.contains("layout_constraintTop_toTopOf")
        );
    }

    /**
     * Asserts that activity_splash.xml contains layout_constraintStart_toStartOf.
     * Validates: Requirement 3.2 (constraint attribute preservation)
     */
    @Test
    public void splashLayout_containsConstraintStartToStartOf() throws IOException {
        String content = readSplashLayout();
        assertTrue(
            "activity_splash.xml must contain 'app:layout_constraintStart_toStartOf'",
            content.contains("layout_constraintStart_toStartOf")
        );
    }

    /**
     * Asserts that activity_splash.xml contains layout_constraintEnd_toEndOf.
     * Validates: Requirement 3.2 (constraint attribute preservation)
     */
    @Test
    public void splashLayout_containsConstraintEndToEndOf() throws IOException {
        String content = readSplashLayout();
        assertTrue(
            "activity_splash.xml must contain 'app:layout_constraintEnd_toEndOf'",
            content.contains("layout_constraintEnd_toEndOf")
        );
    }

    /**
     * Asserts that activity_splash.xml contains layout_constraintBottom_toTopOf.
     * Validates: Requirement 3.2 (constraint attribute preservation)
     */
    @Test
    public void splashLayout_containsConstraintBottomToTopOf() throws IOException {
        String content = readSplashLayout();
        assertTrue(
            "activity_splash.xml must contain 'app:layout_constraintBottom_toTopOf'",
            content.contains("layout_constraintBottom_toTopOf")
        );
    }

    /**
     * Asserts that activity_splash.xml contains layout_constraintTop_toBottomOf.
     * Validates: Requirement 3.2 (constraint attribute preservation)
     */
    @Test
    public void splashLayout_containsConstraintTopToBottomOf() throws IOException {
        String content = readSplashLayout();
        assertTrue(
            "activity_splash.xml must contain 'app:layout_constraintTop_toBottomOf'",
            content.contains("layout_constraintTop_toBottomOf")
        );
    }

    // -------------------------------------------------------------------------
    // Property 2d: activity_splash.xml color references are unchanged
    // -------------------------------------------------------------------------

    /**
     * Asserts that activity_splash.xml references @color/colorSplashBackground.
     * Validates: Requirement 3.3 (color reference preservation)
     */
    @Test
    public void splashLayout_containsColorSplashBackground() throws IOException {
        String content = readSplashLayout();
        assertTrue(
            "activity_splash.xml must reference '@color/colorSplashBackground'",
            content.contains("@color/colorSplashBackground")
        );
    }

    /**
     * Asserts that activity_splash.xml references @color/colorPrimary.
     * Validates: Requirement 3.3 (color reference preservation)
     */
    @Test
    public void splashLayout_containsColorPrimary() throws IOException {
        String content = readSplashLayout();
        assertTrue(
            "activity_splash.xml must reference '@color/colorPrimary'",
            content.contains("@color/colorPrimary")
        );
    }

    /**
     * Asserts that activity_splash.xml references @color/colorTextSecondary.
     * Validates: Requirement 3.3 (color reference preservation)
     */
    @Test
    public void splashLayout_containsColorTextSecondary() throws IOException {
        String content = readSplashLayout();
        assertTrue(
            "activity_splash.xml must reference '@color/colorTextSecondary'",
            content.contains("@color/colorTextSecondary")
        );
    }

    /**
     * Asserts that activity_splash.xml references @color/colorDivider.
     * Validates: Requirement 3.3 (color reference preservation)
     */
    @Test
    public void splashLayout_containsColorDivider() throws IOException {
        String content = readSplashLayout();
        assertTrue(
            "activity_splash.xml must reference '@color/colorDivider'",
            content.contains("@color/colorDivider")
        );
    }

    // -------------------------------------------------------------------------
    // Property 2e: All layout XML files are syntactically valid (well-formed XML)
    // -------------------------------------------------------------------------

    /**
     * Property-based test: for every layout XML file in app/src/main/res/layout/,
     * assert the file exists, is non-empty, and is well-formed XML (starts with
     * the XML declaration or a root element tag).
     *
     * This encodes the baseline that all 34 layout files are valid XML before and
     * after the fix.
     *
     * Validates: Requirement 3.4 (other layout files preservation)
     */
    @Test
    public void allLayoutFiles_areWellFormedXml() throws Exception {
        File layoutDir = new File(APP_DIR, "src/main/res/layout");
        assertTrue(
            "Layout directory must exist: " + layoutDir.getAbsolutePath(),
            layoutDir.isDirectory()
        );

        File[] xmlFiles = layoutDir.listFiles(
            (dir, name) -> name.endsWith(".xml")
        );
        assertNotNull("Layout directory must contain XML files", xmlFiles);
        assertTrue("Layout directory must contain at least one XML file", xmlFiles.length > 0);

        // Property: for all layout XML files, the file is well-formed XML
        for (File xmlFile : xmlFiles) {
            assertTrue(
                "Layout file must exist: " + xmlFile.getName(),
                xmlFile.exists()
            );
            assertTrue(
                "Layout file must not be empty: " + xmlFile.getName(),
                xmlFile.length() > 0
            );

            String content = readFile(xmlFile);

            // Well-formed XML must start with the XML declaration or an element
            assertTrue(
                "Layout file must start with XML declaration or element: " + xmlFile.getName(),
                content.trim().startsWith("<?xml") || content.trim().startsWith("<")
            );

            // Must contain a closing tag (not truncated)
            assertTrue(
                "Layout file must contain at least one closing tag: " + xmlFile.getName(),
                content.contains("/>") || content.contains("</")
            );

            // Parse with javax.xml to confirm well-formedness
            assertXmlWellFormed(xmlFile, content);
        }
    }

    /**
     * Asserts the expected set of layout files is present (no files added or removed).
     * Validates: Requirement 3.4 (other layout files preservation)
     */
    @Test
    public void layoutDirectory_containsExpectedFiles() {
        File layoutDir = new File(APP_DIR, "src/main/res/layout");
        assertTrue("Layout directory must exist", layoutDir.isDirectory());

        List<String> expectedFiles = Arrays.asList(
            "activity_calendar.xml",
            "activity_dream_board.xml",
            "activity_dream_destination_create_edit.xml",
            "activity_dream_destination_detail.xml",
            "activity_forgot_password.xml",
            "activity_friends_discover.xml",
            "activity_main.xml",
            "activity_map.xml",
            "activity_memory_create_edit.xml",
            "activity_memory_detail.xml",
            "activity_memory_list.xml",
            "activity_my_trips.xml",
            "activity_on_this_day.xml",
            "activity_place_detail.xml",
            "activity_profile.xml",
            "activity_review_create_edit.xml",
            "activity_sign_in.xml",
            "activity_sign_up.xml",
            "activity_splash.xml",
            "activity_time_capsule.xml",
            "activity_travel_stats.xml",
            "activity_trip_create_edit.xml",
            "activity_trip_detail.xml",
            "activity_trip_timeline.xml",
            "item_calendar_day.xml",
            "item_dream_destination.xml",
            "item_memory.xml",
            "item_photo_thumbnail.xml",
            "item_profile_card.xml",
            "item_review.xml",
            "item_stat_card.xml",
            "item_timeline_entry.xml",
            "item_trip.xml",
            "item_user.xml"
        );

        for (String fileName : expectedFiles) {
            File f = new File(layoutDir, fileName);
            assertTrue(
                "Expected layout file must be present: " + fileName,
                f.exists()
            );
        }

        // Also assert the total count matches (no extra files added)
        File[] actualFiles = layoutDir.listFiles((dir, name) -> name.endsWith(".xml"));
        assertNotNull(actualFiles);
        assertEquals(
            "Layout directory must contain exactly " + expectedFiles.size() + " XML files",
            expectedFiles.size(),
            actualFiles.length
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String readSplashLayout() throws IOException {
        File splashXml = new File(APP_DIR, "src/main/res/layout/activity_splash.xml");
        return readFile(splashXml);
    }

    private String readFile(File file) throws IOException {
        assertTrue("File must exist: " + file.getAbsolutePath(), file.exists());
        return new String(Files.readAllBytes(file.toPath()));
    }

    /**
     * Parses the XML content using the JDK's built-in SAX parser to confirm
     * the file is well-formed XML. Throws AssertionError on parse failure.
     */
    private void assertXmlWellFormed(File xmlFile, String content) throws Exception {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory =
                javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            // Suppress external entity resolution warnings
            builder.setEntityResolver((publicId, systemId) -> new org.xml.sax.InputSource(
                new java.io.StringReader("")
            ));
            builder.parse(new java.io.ByteArrayInputStream(content.getBytes("UTF-8")));
        } catch (Exception e) {
            fail("Layout file is not well-formed XML: " + xmlFile.getName()
                + " — " + e.getMessage());
        }
    }
}
