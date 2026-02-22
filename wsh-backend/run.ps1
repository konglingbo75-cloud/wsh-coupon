$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
Set-Location "d:\acewillqoder\wsh-backend\wsh-service\target"
& java -jar wsh-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=local 2>&1
