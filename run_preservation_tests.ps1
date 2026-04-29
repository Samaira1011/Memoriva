# Script to compile and run BuildConfigPreservationTest directly with javac/java
# bypassing Gradle's Android resource processing (which fails due to the bug)

$junitJar = "C:\Users\Samaaira\.gradle\caches\modules-2\files-2.1\junit\junit\4.13.2\8ac9e16dd933b6fb43bc7f576336b8f4d7eb5ba12\junit-4.13.2.jar"
$hamcrestJar = "C:\Users\Samaaira\.gradle\caches\modules-2\files-2.1\org.hamcrest\hamcrest-core\1.3\42a25dc3219429f0e5d060061f71acb49bf010a0\hamcrest-core-1.3.jar"
$srcFile = "C:\Memoriva\app\src\test\java\com\example\memoriva\BuildConfigPreservationTest.java"
$outDir = "C:\Memoriva\app\build\test-classes-standalone"

New-Item -ItemType Directory -Force -Path $outDir | Out-Null

# Build classpath string with semicolon separator (Windows)
$cp = $junitJar + ";" + $hamcrestJar

Write-Host "Compiling BuildConfigPreservationTest..."
$compileResult = & javac "-cp" $cp "-d" $outDir $srcFile 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "COMPILATION FAILED:"
    $compileResult | ForEach-Object { Write-Host $_ }
    exit 1
}
Write-Host "Compilation succeeded."

# Run with JUnit console runner
$runCp = $outDir + ";" + $junitJar + ";" + $hamcrestJar
Write-Host "Running tests..."
$runResult = & java "-cp" $runCp "org.junit.runner.JUnitCore" "com.example.memoriva.BuildConfigPreservationTest" 2>&1
$runResult | ForEach-Object { Write-Host $_ }
exit $LASTEXITCODE
