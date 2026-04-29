# Android Resource Linking Fix — Bugfix Design

## Overview

The Memoriva Android project fails to build with an AAPT2 resource linking error on
`activity_splash.xml` line 21. The root cause is an **invalid `compileSdk` block syntax** in
`app/build.gradle.kts` that prevents Gradle from configuring the project at all. Because the
project never configures successfully, no dependencies (including
`androidx.constraintlayout:constraintlayout:2.2.1`) are resolved, so AAPT2 cannot find the
ConstraintLayout attribute declarations and rejects every `app:layout_constraint*` attribute in
the layout file.

The fix is a single-line change in `app/build.gradle.kts`: replace the invalid block form
`compileSdk { version = release(36) }` with the correct assignment form `compileSdk = 36`.
No layout files, resource files, or dependency declarations require modification.

---

## Glossary

- **Bug_Condition (C)**: The condition that triggers the build failure — the `compileSdk` property
  in `app/build.gradle.kts` is expressed as a block (`compileSdk { version = release(36) }`)
  instead of a direct assignment (`compileSdk = 36`), which is invalid Kotlin DSL.
- **Property (P)**: The desired outcome when the bug condition holds — the Gradle project
  configures successfully, all dependencies resolve, and AAPT2 links resources without errors.
- **Preservation**: All existing layout attributes, resource references, dependency declarations,
  and build behaviour that must remain identical after the fix.
- **`compileSdk`**: An `android {}` DSL property in `app/build.gradle.kts` that specifies the
  Android API level used to compile the app. It must be set as a simple integer assignment.
- **AAPT2**: Android Asset Packaging Tool 2 — the build step that compiles and links XML resource
  files. It fails when referenced library attributes (e.g., ConstraintLayout constraints) are not
  found in the resolved dependency set.
- **`activity_splash.xml`**: The splash-screen layout at
  `app/src/main/res/layout/activity_splash.xml`. It uses `app:layout_constraint*` attributes that
  require the ConstraintLayout AAR to be present on the compile classpath.

---

## Bug Details

### Bug Condition

The build fails because `compileSdk` is written as a configuration block rather than a property
assignment. The Android Gradle Plugin (AGP) 9.x Kotlin DSL does not expose a `compileSdk { }`
lambda; the only valid form is `compileSdk = <Int>`. The invalid block causes a Gradle
configuration-phase exception, which prevents dependency resolution entirely. Without the
ConstraintLayout AAR on the classpath, AAPT2 cannot resolve `app:layout_constraint*` attributes
and aborts with "Android resource linking failed".

**Formal Specification:**

```
FUNCTION isBugCondition(X)
  INPUT: X of type BuildConfiguration
  OUTPUT: boolean

  // Returns true when compileSdk is expressed as a block instead of an assignment
  RETURN X.compileSdkSyntax = BLOCK_FORM   // i.e., "compileSdk { version = release(36) }"
         AND X.compileSdkSyntax ≠ ASSIGNMENT_FORM  // i.e., "compileSdk = 36"
END FUNCTION
```

### Examples

- **Bug present**: `compileSdk { version = release(36) }` → Gradle configuration fails →
  dependencies unresolved → AAPT2 reports "attribute layout_constraintVerticalBias not found" on
  `activity_splash.xml:21` → build aborts.
- **Bug absent**: `compileSdk = 36` → Gradle configures successfully → ConstraintLayout 2.2.1
  resolved → AAPT2 finds all `app:layout_constraint*` attribute declarations → build succeeds.
- **Edge case — other SDK values**: Any integer value (e.g., `compileSdk = 35`) in assignment
  form is valid syntax; the bug is purely syntactic, not value-dependent.
- **Edge case — other layout files**: All other layout files that use `app:layout_constraint*`
  attributes are equally affected while the bug is present and equally fixed once the syntax is
  corrected.

---

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**

- `activity_splash.xml` must render with the same visual layout (CardView logo, app name
  TextView, tagline, ProgressBar, footer) after the fix.
- All existing `app:layout_constraint*` attribute values in `activity_splash.xml` must remain
  unmodified (e.g., `layout_constraintVerticalBias="0.35"`, `layout_constraintTop_toBottomOf`,
  etc.).
- All color references (`@color/colorSplashBackground`, `@color/colorPrimary`,
  `@color/colorTextSecondary`, `@color/colorDivider`) must continue to resolve correctly.
- All other activities and resources in the project must compile and run without regression.
- The `androidx.constraintlayout:constraintlayout:2.2.1` dependency declared in
  `gradle/libs.versions.toml` must continue to be resolved as-is.
- `minSdk = 24`, `targetSdk = 36`, `versionCode`, `versionName`, and all other `defaultConfig`
  values must remain unchanged.

**Scope:**

All inputs that do NOT involve the `compileSdk` syntax in `app/build.gradle.kts` are completely
unaffected by this fix. This includes:

- All Java source files under `app/src/main/java/`
- All XML layout and resource files under `app/src/main/res/`
- `gradle/libs.versions.toml` and all dependency declarations
- `AndroidManifest.xml`
- All other `android {}` block settings (`minSdk`, `targetSdk`, `buildTypes`, `compileOptions`)

---

## Hypothesized Root Cause

Based on inspection of `app/build.gradle.kts`, the root cause is confirmed:

1. **Invalid Kotlin DSL Syntax for `compileSdk`**: The file contains:
   ```kotlin
   compileSdk {
       version = release(36)
   }
   ```
   The Android Gradle Plugin Kotlin DSL does not define a `compileSdk` lambda/block. The only
   valid form is the direct property assignment `compileSdk = 36`. The block form causes a
   `MissingMethodException` or unresolved reference during Gradle's configuration phase.

2. **Cascading Dependency Resolution Failure**: Because the configuration phase fails, Gradle
   never reaches the dependency resolution phase. The ConstraintLayout AAR is never downloaded
   or placed on the compile classpath.

3. **AAPT2 Attribute Resolution Failure**: With no ConstraintLayout AAR on the classpath, AAPT2
   has no source for the `app:layout_constraint*` attribute declarations. It treats every such
   attribute in `activity_splash.xml` as unknown and aborts resource linking.

4. **No Cache or Path Issue**: Despite the original bug report attributing the failure to a stale
   Gradle cache from an OneDrive-to-C-drive migration, the actual root cause is the syntactically
   invalid `compileSdk` block. The cache hypothesis is a red herring; the build would fail on any
   machine or clean environment with this syntax.

---

## Correctness Properties

Property 1: Bug Condition — Invalid `compileSdk` Syntax Causes Build Failure

_For any_ build configuration where `isBugCondition` returns true (i.e., `compileSdk` is written
as a block), the fixed `app/build.gradle.kts` SHALL use the assignment form `compileSdk = 36`,
causing Gradle to configure the project successfully, resolve all declared dependencies including
`androidx.constraintlayout:constraintlayout:2.2.1`, and allow AAPT2 to complete resource linking
without errors.

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation — All Other Build Configuration Remains Unchanged

_For any_ build configuration where `isBugCondition` returns false (i.e., `compileSdk` is already
in valid assignment form, or any other part of the build configuration is evaluated), the fixed
`app/build.gradle.kts` SHALL produce exactly the same Gradle configuration, dependency graph, and
build output as the original file would produce if the `compileSdk` syntax were valid, preserving
all layout rendering, resource references, dependency versions, and SDK settings.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

---

## Fix Implementation

### Changes Required

**File**: `app/build.gradle.kts`

**Function/Block**: `android { }` block, `compileSdk` property

**Specific Changes**:

1. **Replace block syntax with assignment syntax**:
   - **Before**:
     ```kotlin
     compileSdk {
         version = release(36)
     }
     ```
   - **After**:
     ```kotlin
     compileSdk = 36
     ```

2. **No other changes**: All other lines in `app/build.gradle.kts` are valid and must remain
   exactly as they are. No changes to `minSdk`, `targetSdk`, dependencies, plugins, or build
   types are required.

3. **No layout or resource changes**: `activity_splash.xml` is syntactically correct and must
   not be modified.

4. **No version catalog changes**: `gradle/libs.versions.toml` correctly declares
   `constraintlayout = "2.2.1"` and must not be modified.

5. **No cache invalidation required**: Once the syntax is corrected, a standard Gradle build
   will resolve dependencies normally. Manual cache clearing is not necessary (though a
   `./gradlew clean` before the first build after the fix is harmless).

---

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, confirm the bug manifests on the
unfixed code (exploratory checking), then verify the fix resolves the build failure and preserves
all existing behaviour (fix checking and preservation checking).

### Exploratory Bug Condition Checking

**Goal**: Confirm that the invalid `compileSdk` block syntax causes a Gradle configuration
failure on the unfixed code, and that this failure cascades into the AAPT2 resource linking
error. This validates the root cause hypothesis before applying the fix.

**Test Plan**: Attempt a full Gradle build (`./gradlew assembleDebug`) on the unfixed
`app/build.gradle.kts`. Observe the error output to confirm the configuration-phase failure and
the subsequent AAPT2 error.

**Test Cases**:

1. **Full Build on Unfixed Code**: Run `./gradlew assembleDebug` with the original
   `compileSdk { version = release(36) }` syntax. Expected: build fails during Gradle
   configuration or dependency resolution, with an error referencing the invalid `compileSdk`
   block or an unresolved reference. (Will fail on unfixed code.)
2. **AAPT2 Error Confirmation**: If Gradle reaches the resource processing phase, confirm the
   error message references `activity_splash.xml:21` and `layout_constraintVerticalBias`.
   (Will fail on unfixed code.)
3. **Dependency Resolution Check**: Run `./gradlew dependencies --configuration
   debugRuntimeClasspath` on unfixed code. Expected: task fails or ConstraintLayout is absent
   from the resolved dependency tree. (Will fail on unfixed code.)
4. **Clean Environment Test**: Run the build on a machine with no prior Gradle cache for this
   project. Expected: same failure, confirming the bug is not cache-related. (Will fail on
   unfixed code.)

**Expected Counterexamples**:

- Gradle configuration phase throws an exception for the unrecognised `compileSdk { }` block.
- Possible causes confirmed: invalid Kotlin DSL syntax for `compileSdk` property assignment.

### Fix Checking

**Goal**: Verify that for all build configurations where the bug condition holds, the fixed
`app/build.gradle.kts` produces a successful build.

**Pseudocode:**

```
FOR ALL X WHERE isBugCondition(X) DO
  result := build_fixed(X)
  ASSERT result.gradleConfigurationSucceeded = true
  ASSERT result.constraintLayoutResolved = true
  ASSERT result.aapt2ResourceLinkingSucceeded = true
  ASSERT result.activitySplashXmlProcessedWithoutErrors = true
END FOR
```

### Preservation Checking

**Goal**: Verify that for all build configurations where the bug condition does NOT hold, the
fixed build produces the same output as a correctly configured build would.

**Pseudocode:**

```
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT build_original_valid(X) = build_fixed(X)
  // Same APK contents, same resource IDs, same compiled classes
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:

- It can generate many combinations of resource configurations and verify none are broken.
- It catches edge cases (e.g., other layout files using ConstraintLayout attributes) that manual
  tests might miss.
- It provides strong guarantees that the single-line syntax fix has no unintended side effects.

**Test Plan**: After applying the fix, observe that the build succeeds and all existing
behaviours are preserved. Write property-based tests to verify resource attribute resolution
across all layout files.

**Test Cases**:

1. **Layout Rendering Preservation**: Verify `activity_splash.xml` compiles with all
   `app:layout_constraint*` attributes intact and unchanged after the fix.
2. **Color Reference Preservation**: Verify all `@color/` references in `activity_splash.xml`
   resolve correctly after the fix.
3. **Dependency Version Preservation**: Verify `constraintlayout:2.2.1` is still the resolved
   version after the fix (no version drift introduced).
4. **Other Layout Files Preservation**: Verify all other layout files in the project compile
   without errors after the fix.
5. **SDK Settings Preservation**: Verify `minSdk = 24`, `targetSdk = 36`, and all other
   `defaultConfig` values are unchanged after the fix.

### Unit Tests

- Verify the `compileSdk` value in the fixed `build.gradle.kts` is `36` (integer assignment,
  not a block).
- Verify no other lines in `build.gradle.kts` were modified by the fix.
- Verify `activity_splash.xml` is byte-for-byte identical before and after the fix.

### Property-Based Tests

- Generate all layout XML files in `app/src/main/res/layout/` and verify each compiles
  successfully after the fix (no AAPT2 errors for any layout).
- Generate random subsets of `app:layout_constraint*` attributes and verify they all resolve
  correctly once ConstraintLayout is on the classpath.
- Verify that the resolved dependency graph after the fix contains exactly the same libraries
  as declared in `gradle/libs.versions.toml`, with no additions or removals.

### Integration Tests

- Run `./gradlew assembleDebug` on the fixed project and assert exit code 0.
- Run `./gradlew test` to confirm no unit test regressions are introduced by the fix.
- Run `./gradlew lint` to confirm no new lint warnings are introduced by the syntax change.
- Launch the app on an emulator or device and verify the splash screen renders correctly with
  the expected ConstraintLayout positioning.
