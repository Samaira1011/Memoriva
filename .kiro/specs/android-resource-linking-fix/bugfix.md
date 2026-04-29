# Bugfix Requirements Document

## Introduction

After moving the Memoriva Android project from OneDrive to the C drive, the build fails during the AAPT2 resource linking phase with the error:

```
com.example.memoriva.app-main-6:/layout/activity_splash.xml:21: error: attribute layout_constraint... error: failed linking file resources.
```

The failure occurs because AAPT2 cannot resolve `app:` namespace attributes (ConstraintLayout constraint attributes) in `activity_splash.xml`. This is caused by a stale or corrupted Gradle/AAPT2 cache from the previous drive location, preventing the ConstraintLayout library's attribute declarations from being found during resource linking. The build must be restored to a working state without altering any existing layout or resource definitions.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN the project is built after being relocated from OneDrive to the C drive THEN the system fails with "Android resource linking failed" during AAPT2 resource processing.

1.2 WHEN AAPT2 processes `activity_splash.xml` at line 21 THEN the system reports an unresolvable `app:` namespace attribute (e.g., `layout_constraintVerticalBias`) and aborts the build.

1.3 WHEN the Gradle build executes with a stale cache referencing the old drive path THEN the system cannot locate the ConstraintLayout AAR's attribute declarations, causing all `app:layout_constraint*` attributes in the layout to be treated as unknown.

### Expected Behavior (Correct)

2.1 WHEN the project is built from its new location on the C drive THEN the system SHALL complete the AAPT2 resource linking phase without errors.

2.2 WHEN AAPT2 processes `activity_splash.xml` THEN the system SHALL successfully resolve all `app:layout_constraint*` attributes declared in the ConstraintLayout namespace.

2.3 WHEN the Gradle build executes after cache invalidation THEN the system SHALL locate and use the ConstraintLayout AAR from the local Gradle dependency cache, resolving all `app:` namespace attributes correctly.

### Unchanged Behavior (Regression Prevention)

3.1 WHEN the build succeeds THEN the system SHALL CONTINUE TO render `activity_splash.xml` with the same visual layout, including the CardView logo, app name TextView, tagline, progress bar, and footer elements.

3.2 WHEN the build succeeds THEN the system SHALL CONTINUE TO apply all existing `app:layout_constraint*` attribute values in `activity_splash.xml` without modification (e.g., `layout_constraintVerticalBias="0.35"`, `layout_constraintTop_toBottomOf`, etc.).

3.3 WHEN the build succeeds THEN the system SHALL CONTINUE TO resolve all color references (`@color/colorSplashBackground`, `@color/colorPrimary`, `@color/colorTextSecondary`, `@color/colorDivider`) used in `activity_splash.xml`.

3.4 WHEN the build succeeds THEN the system SHALL CONTINUE TO compile and run all other activities and resources in the project without regression.

3.5 WHEN the project is built on a clean environment THEN the system SHALL CONTINUE TO resolve the `androidx.constraintlayout:constraintlayout:2.2.1` dependency as declared in `gradle/libs.versions.toml`.

---

## Bug Condition

**Bug Condition Function:**

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type BuildEnvironment
  OUTPUT: boolean

  // Returns true when the build environment has a stale cache
  // from a previous drive location that prevents ConstraintLayout
  // attribute resolution during AAPT2 resource linking
  RETURN X.gradleCacheContainsStalePathReferences = true
      OR X.aapt2CacheReferencesOldDrivePath = true
END FUNCTION
```

**Property: Fix Checking**

```pascal
// Property: Fix Checking - Resource Linking Succeeds After Cache Invalidation
FOR ALL X WHERE isBugCondition(X) DO
  result ← build'(X)
  ASSERT result.aapt2ResourceLinkingSucceeded = true
      AND result.activitySplashXmlProcessedWithoutErrors = true
END FOR
```

**Property: Preservation Checking**

```pascal
// Property: Preservation Checking
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT build(X) = build'(X)
  // All existing layout attributes, color references, and resource
  // definitions remain identical before and after the fix
END FOR
```
