$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
Set-Location "d:\acewillqoder\wsh-backend"
mvn clean compile -pl wsh-service -am
