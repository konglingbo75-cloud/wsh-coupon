$ErrorActionPreference = "Stop"

$url = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
$downloadPath = Join-Path $env:TEMP "maven.zip"
$installPath = "C:\maven"

Write-Host "Downloading Maven 3.9.6 from archive..."
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
Invoke-WebRequest -Uri $url -OutFile $downloadPath -UseBasicParsing
Write-Host "Download complete."

if (Test-Path $installPath) {
    Remove-Item -Recurse -Force $installPath
}
Expand-Archive -Path $downloadPath -DestinationPath $installPath -Force
Write-Host "Extracted."

$mavenHome = Join-Path $installPath "apache-maven-3.9.6"
$mavenBin = Join-Path $mavenHome "bin"

$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
if (-not ($currentPath -like "*maven*")) {
    [Environment]::SetEnvironmentVariable("Path", "$currentPath;$mavenBin", "User")
}
[Environment]::SetEnvironmentVariable("MAVEN_HOME", $mavenHome, "User")

Write-Host "MAVEN_HOME=$mavenHome"
Write-Host "Maven bin added to PATH: $mavenBin"
Write-Host "Done!"
