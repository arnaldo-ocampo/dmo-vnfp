# Muestra para un frente que solucion elige cada metodo, como queda la red y cual queda mejor.
# Uso:  .\ver-red.ps1 [algoritmo] [ronda] [ventana] [prioridad]
# Ej.:  .\ver-red.ps1 DNSGAII-A 1 0 enlaces
# Prioridades: equilibrado (pesos iguales), latencia, enlaces, pocas_vnfs, costo
param(
    [string]$Algoritmo = "DNSGAII-A",
    [int]$Ronda = 1,
    [int]$Ventana = 0,
    [string]$Prioridad = "equilibrado"
)

$carpeta = $PSScriptRoot
$project = Join-Path $carpeta "..\..\Code\DMOP"
$appHome = ($carpeta -replace '\\', '/') + "/"
$log4j   = "file:///" + ($carpeta -replace '\\', '/') + "/log4j-quiet.properties"

# Si existe el repositorio maven local del proyecto se usa sin internet
$repo = Join-Path $carpeta "..\..\run-local\maven-repository"
$mvn = @("-q")
if (Test-Path $repo) { $mvn += @("-o", "-Dmaven.repo.local=$repo") }

# Para que los acentos se vean bien en la consola
$encoding = [Console]::OutputEncoding
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Push-Location $project
try {
    mvn @mvn compile
    if ($LASTEXITCODE -ne 0) { throw "Fallo la compilacion" }

    mvn @mvn org.codehaus.mojo:exec-maven-plugin:3.0.0:exec `
        "-Dexec.executable=java" `
        "-Dexec.args=-Dapp.home=$appHome -Dstdout.encoding=UTF-8 -Dlog4j.configuration=$log4j -classpath %classpath py.edu.fiuni.dmop.NetworkStateViewer $Algoritmo $Ronda $Ventana $Prioridad"
}
finally {
    Pop-Location
    [Console]::OutputEncoding = $encoding
}
