# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** - Invalid `compileSdk` Block Syntax Causes Build Failure
  - **CRITICAL**: This test MUST FAIL on unfixed code — failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior — it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the invalid `compileSdk` block syntax causes a Gradle configuration failure
  - **Scoped PBT Approach**: Scope the property to the concrete failing case — `app/build.gradle.kts` containing `compileSdk { version = release(36) }` (block form)
  - Run `./gradlew assembleDebug` on the unfixed `app/build.gradle.kts` (with `compileSdk { version = release(36) }`)
  - Assert the build fails during Gradle configuration or dependency resolution with an error referencing the invalid `compileSdk` block or an unresolved reference
  - Also run `./gradlew dependencies --configuration debugRuntimeClasspath` and assert it fails or that `androidx.constraintlayout:constraintlayout:2.2.1` is absent from the resolved dependency tree
  - Document counterexamples found (e.g., "Gradle configuration phase throws MissingMethodException for `compileSdk { }` block; ConstraintLayout absent from classpath; AAPT2 reports `layout_constraintVerticalBias` not found on `activity_splash.xml:21`")
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Build FAILS (this is correct — it proves the bug exists)
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - All Other Build Configuration Remains Unchanged
  - **IMPORTANT**: Follow observation-first methodology — observe behavior on UNFIXED code for non-buggy inputs (all parts of the build configuration that are NOT the `compileSdk` syntax)
  - Observe: `minSdk = 24`, `targetSdk = 36`, `versionCode = 1`, `versionName = "1.0"` are present and unchanged in `app/build.gradle.kts`
  - Observe: `activity_splash.xml` is byte-for-byte identical (all `app:layout_constraint*` attribute values, color references, and view hierarchy are intact)
  - Observe: `gradle/libs.versions.toml` declares `constraintlayout = "2.2.1"` and all other dependency versions are unchanged
  - Observe: All other layout files under `app/src/main/res/layout/` are syntactically valid XML with no modifications
  - Write property-based tests capturing these observed behaviors:
    - For all layout XML files in `app/src/main/res/layout/`, assert each file is syntactically valid XML and contains no modifications relative to the pre-fix baseline
    - Assert `app/build.gradle.kts` contains `minSdk = 24`, `targetSdk = 36`, `versionCode = 1`, `versionName = "1.0"` unchanged
    - Assert `activity_splash.xml` contains all expected `app:layout_constraint*` attributes (e.g., `layout_constraintVerticalBias="0.35"`) and color references (`@color/colorSplashBackground`, `@color/colorPrimary`, `@color/colorTextSecondary`, `@color/colorDivider`) unchanged
    - Assert `gradle/libs.versions.toml` still declares `constraintlayout = "2.2.1"` with no version drift
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 3. Fix invalid `compileSdk` block syntax in `app/build.gradle.kts`

  - [x] 3.1 Implement the fix
    - In `app/build.gradle.kts`, inside the `android { }` block, replace the block form:
      ```kotlin
      compileSdk {
          version = release(36)
      }
      ```
      with the direct assignment form:
      ```kotlin
      compileSdk = 36
      ```
    - Make no other changes to `app/build.gradle.kts` — all other lines (`minSdk`, `targetSdk`, `buildTypes`, `compileOptions`, `dependencies`, plugins) must remain exactly as they are
    - Do not modify `activity_splash.xml`, any other layout or resource file, `gradle/libs.versions.toml`, or `AndroidManifest.xml`
    - _Bug_Condition: isBugCondition(X) where X.compileSdkSyntax = BLOCK_FORM ("compileSdk { version = release(36) }")_
    - _Expected_Behavior: Gradle configures successfully → ConstraintLayout 2.2.1 resolved → AAPT2 links resources without errors → build exits with code 0_
    - _Preservation: All layout attributes, color references, dependency declarations, SDK settings (minSdk=24, targetSdk=36), and all other build configuration remain identical_
    - _Requirements: 2.1, 2.2, 2.3, 3.1, 3.2, 3.3, 3.4, 3.5_

  - [x] 3.2 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Invalid `compileSdk` Block Syntax Causes Build Failure
    - **IMPORTANT**: Re-run the SAME test from task 1 — do NOT write a new test
    - The test from task 1 encodes the expected behavior (build succeeds with `compileSdk = 36`)
    - Run `./gradlew assembleDebug` on the FIXED `app/build.gradle.kts`
    - Assert Gradle configuration succeeds, `androidx.constraintlayout:constraintlayout:2.2.1` is present in the resolved dependency tree, AAPT2 processes `activity_splash.xml` without errors, and the build exits with code 0
    - **EXPECTED OUTCOME**: Build PASSES (confirms bug is fixed)
    - _Requirements: 2.1, 2.2, 2.3_

  - [x] 3.3 Verify preservation tests still pass
    - **Property 2: Preservation** - All Other Build Configuration Remains Unchanged
    - **IMPORTANT**: Re-run the SAME tests from task 2 — do NOT write new tests
    - Run all preservation property tests from step 2 against the fixed codebase
    - Confirm `activity_splash.xml`, all other layout files, `gradle/libs.versions.toml`, and all `defaultConfig` values are byte-for-byte identical to the pre-fix baseline
    - Run `./gradlew test` to confirm no unit test regressions
    - Run `./gradlew lint` to confirm no new lint warnings introduced by the syntax change
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all tests still pass after fix (no regressions)

- [x] 4. Checkpoint — Ensure all tests pass
  - Run `./gradlew assembleDebug` and assert exit code 0
  - Run `./gradlew test` and assert all unit tests pass
  - Confirm the bug condition exploration test (task 1 / Property 1) now passes
  - Confirm all preservation tests (task 2 / Property 2) still pass
  - Ensure all tests pass; ask the user if any questions arise
