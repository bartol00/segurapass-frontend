param(
    [string]$Pom = "pom.xml"
)

[xml]$xml = Get-Content $Pom
$xml.project.properties.'segurapass.protocol.version'
