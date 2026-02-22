$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
Set-Location "d:\acewillqoder\wsh-backend"
& mvn compile 2>&1 | Out-File -FilePath "d:\acewillqoder\build.log" -Encoding UTF8
Get-Content "d:\acewillqoder\build.log"
