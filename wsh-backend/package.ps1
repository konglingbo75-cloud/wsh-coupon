$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
Set-Location "d:\acewillqoder\wsh-backend"
mvn package -pl wsh-service -am -DskipTests
