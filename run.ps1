Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "Starting DocMorph Document Converter Application..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

$mvnPath = "$env:USERPROFILE\Downloads\apache-maven-3.10.0-bin\apache-maven-3.10.0\bin\mvn.cmd"
if (-not (Test-Path $mvnPath)) {
    $cmd = Get-Command mvn -ErrorAction SilentlyContinue
    if ($cmd) {
        $mvnPath = "mvn"
    } else {
        Write-Host "[ERROR] Maven could not be found automatically." -ForegroundColor Red
        Write-Host "You can also run the built jar directly with: java -jar target/doc-converter-app-1.0.0.jar" -ForegroundColor Yellow
        exit 1
    }
}

Write-Host "Using Maven: $mvnPath" -ForegroundColor Green
& $mvnPath spring-boot:run
