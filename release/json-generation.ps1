param(
    [string]$Version,
    [string]$Protocol,
    [string]$Sha,
    [string]$Output = "metadata.json"
)

@{
    appVersion      = $Version
    protocolVersion = [int]$Protocol
    sha256          = $Sha
    releaseDate     = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
} |
ConvertTo-Json |
Set-Content $Output -Encoding UTF8NoBOM
