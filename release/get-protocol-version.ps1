param(
    [string]$Pom = "pom.xml"
)

[xml]$xml = Get-Content $Pom
$xml.project.properties.'protocol.version'
