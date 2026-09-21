# ============================================================================
#  MTEC Informatica - gera o executavel (.exe) completo
#  Faz sozinho: baixa Java 21 (JDK) e o banco H2 (se precisar), compila o
#  projeto, empacota Java + banco dentro do programa e roda um autoteste.
#  Resultado:  SAIDA\MTEC Informatica\MTEC Informatica.exe
# ============================================================================
$ErrorActionPreference = 'Stop'
$ProgressPreference    = 'SilentlyContinue'
try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12 } catch {}

$raiz     = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $raiz                      # todos os caminhos abaixo sao relativos a esta pasta

$nomeApp  = 'MTEC Informatica'
$h2Versao = '2.3.232'
$h2Url    = "https://repo1.maven.org/maven2/com/h2database/h2/$h2Versao/h2-$h2Versao.jar"
$jdkUrl   = 'https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse'

$ferr     = 'ferramentas'               # cache: JDK e H2 (baixados uma unica vez)
$temp     = 'build_exe\temp'
$saida    = 'SAIDA'

function Passo($n, $texto) { Write-Host ""; Write-Host "[$n/7] $texto" -ForegroundColor Cyan }

function Versao-Jdk($jdkHome) {
    # devolve o numero maior da versao (ex.: 21) ou 0 se nao for um JDK utilizavel
    $javac = Join-Path $jdkHome 'bin\javac.exe'
    $jpack = Join-Path $jdkHome 'bin\jpackage.exe'
    if (-not (Test-Path $javac) -or -not (Test-Path $jpack)) { return 0 }
    try {
        $txt = (& $javac -version 2>&1 | Out-String)
        if ($txt -match 'javac\s+(\d+)') { return [int]$Matches[1] }
    } catch {}
    return 0
}

try {
    Write-Host "=====================================================" -ForegroundColor Green
    Write-Host "  Gerando o executavel do sistema MTEC Informatica"     -ForegroundColor Green
    Write-Host "=====================================================" -ForegroundColor Green

    # ---------------------------------------------------------------- 1) JDK
    Passo 1 'Procurando o Java 21 (JDK)...'
    $jdk = $null
    $candidatos = @("$ferr\jdk", $env:JAVA_HOME) | Where-Object { $_ }
    foreach ($c in $candidatos) {
        if ((Versao-Jdk $c) -ge 21) { $jdk = (Resolve-Path $c).Path; break }
    }
    if (-not $jdk) {
        Write-Host '      JDK 21 nao encontrado. Baixando (cerca de 190 MB, so acontece uma vez)...'
        New-Item -ItemType Directory -Force -Path $ferr | Out-Null
        $zip = "$ferr\jdk21.zip"
        Invoke-WebRequest -Uri $jdkUrl -OutFile $zip -UseBasicParsing
        $tmpJdk = "$ferr\jdk_tmp"
        if (Test-Path $tmpJdk) { Remove-Item $tmpJdk -Recurse -Force }
        Expand-Archive -Path $zip -DestinationPath $tmpJdk -Force
        $interna = Get-ChildItem $tmpJdk -Directory | Select-Object -First 1
        if (Test-Path "$ferr\jdk") { Remove-Item "$ferr\jdk" -Recurse -Force }
        Move-Item $interna.FullName "$ferr\jdk"
        Remove-Item $tmpJdk -Recurse -Force
        Remove-Item $zip -Force
        if ((Versao-Jdk "$ferr\jdk") -lt 21) { throw 'O JDK baixado nao funcionou.' }
        $jdk = (Resolve-Path "$ferr\jdk").Path
    }
    Write-Host "      Usando o JDK em: $jdk"
    $javac    = Join-Path $jdk 'bin\javac.exe'
    $jarTool  = Join-Path $jdk 'bin\jar.exe'
    $jpackage = Join-Path $jdk 'bin\jpackage.exe'

    # ---------------------------------------------------------------- 2) H2
    Passo 2 'Procurando o banco de dados embutido (H2)...'
    New-Item -ItemType Directory -Force -Path $ferr | Out-Null
    $h2Jar = "$ferr\h2-$h2Versao.jar"
    if (-not (Test-Path $h2Jar)) {
        Write-Host '      Baixando o H2 (cerca de 2,5 MB)...'
        Invoke-WebRequest -Uri $h2Url -OutFile $h2Jar -UseBasicParsing
        Invoke-WebRequest -Uri "$h2Url.sha1" -OutFile "$h2Jar.sha1" -UseBasicParsing
        $esperado = ((Get-Content "$h2Jar.sha1" -Raw).Trim() -split '\s+')[0].ToLower()
        $obtido   = (Get-FileHash $h2Jar -Algorithm SHA1).Hash.ToLower()
        if ($esperado -ne $obtido) {
            Remove-Item $h2Jar -Force
            throw 'O arquivo do H2 baixado esta corrompido (SHA-1 nao confere). Tente de novo.'
        }
        Write-Host '      H2 baixado e verificado (SHA-1 confere).'
    } else {
        Write-Host '      H2 ja esta na pasta ferramentas.'
    }

    # ---------------------------------------------------------------- 3) compilar
    Passo 3 'Compilando o projeto...'
    if (Test-Path $temp) { Remove-Item $temp -Recurse -Force }
    New-Item -ItemType Directory -Force -Path "$temp\classes", "$temp\input" | Out-Null

    $jars = @(Get-ChildItem 'lib' -Filter '*.jar' | ForEach-Object { 'lib/' + $_.Name })
    $cp   = ($jars -join ';')
    $fontes = @(Get-ChildItem 'src' -Recurse -Filter '*.java' | ForEach-Object {
        ($_.FullName.Substring($raiz.Length + 1)) -replace '\\', '/'
    })
    # arquivo de argumentos SEM BOM (o javac nao aceita BOM)
    $lista = ($fontes | ForEach-Object { '"' + $_ + '"' })
    [System.IO.File]::WriteAllLines("$raiz\$temp\fontes.txt", $lista, (New-Object System.Text.UTF8Encoding($false)))
    & $javac -encoding UTF-8 -nowarn -cp $cp -d "$temp\classes" "@$temp\fontes.txt"
    if ($LASTEXITCODE -ne 0) { throw 'Erro ao compilar o projeto (veja as mensagens acima).' }
    Write-Host "      $($fontes.Count) arquivos .java compilados."

    # ---------------------------------------------------------------- 4) jar
    Passo 4 'Montando o programa (.jar) com imagens, fontes e script do banco...'
    & robocopy 'src' "$temp\classes" /E /XF '*.java' /NFL /NDL /NJH /NJS /NP | Out-Null
    if ($LASTEXITCODE -ge 8) { throw 'Falha ao copiar imagens/recursos.' }
    & $jarTool --create --file "$temp\input\MtecInformatica.jar" --main-class DAO.Main -C "$temp\classes" .
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar o .jar.' }
    Copy-Item 'lib\*.jar' "$temp\input\"
    Copy-Item $h2Jar "$temp\input\"

    # ---------------------------------------------------------------- 5) jpackage
    Passo 5 'Criando o executavel com o Java embutido (pode levar 1-2 minutos)...'
    New-Item -ItemType Directory -Force -Path $saida | Out-Null
    if (Test-Path "$saida\$nomeApp") { Remove-Item "$saida\$nomeApp" -Recurse -Force }
    $modulos = 'java.se,jdk.unsupported'
    $listaMod = (& (Join-Path $jdk 'bin\java.exe') --list-modules | Out-String)
    if ($listaMod -match 'jdk\.crypto\.ec') { $modulos = $modulos + ',jdk.crypto.ec' }
    $jpArgs = @(
        '--type', 'app-image',
        '--name', $nomeApp,
        '--input', "$temp\input",
        '--main-jar', 'MtecInformatica.jar',
        '--main-class', 'DAO.Main',
        '--dest', $saida,
        '--icon', 'build_exe\app.ico',
        '--app-version', '1.0',
        '--vendor', 'MTEC Informatica',
        '--description', 'Sistema de gestao MTEC Informatica',
        '--add-modules', $modulos,
        '--java-options', '-Dfile.encoding=UTF-8'
    )
    & $jpackage @jpArgs
    if ($LASTEXITCODE -ne 0) { throw 'Falha no jpackage (veja as mensagens acima).' }
    $pastaApp = Join-Path $saida $nomeApp
    $exe      = Join-Path $pastaApp "$nomeApp.exe"
    if (-not (Test-Path $exe)) { throw "O executavel nao foi gerado: $exe" }

    # ---------------------------------------------------------------- 6) autoteste
    Passo 6 'Testando o banco embutido com o Java que vai dentro do programa...'
    $javaEmbutido = Join-Path $pastaApp 'runtime\bin\java.exe'
    $pastaTeste   = Join-Path $env:TEMP ('mtec_autoteste_' + [guid]::NewGuid().ToString('N'))
    $cpTeste      = (Join-Path $pastaApp 'app') + '\*'
    & $javaEmbutido "-Dmtec.dados=$pastaTeste" -Dfile.encoding=UTF-8 -cp $cpTeste DAO.AutoTeste
    $codigoTeste = $LASTEXITCODE
    if (Test-Path $pastaTeste) { Remove-Item $pastaTeste -Recurse -Force -ErrorAction SilentlyContinue }
    if ($codigoTeste -ne 0) {
        throw 'O AUTOTESTE FALHOU: o banco embutido nao passou nas verificacoes (veja as linhas [FALHOU] acima). Envie essa tela para quem esta ajudando com o projeto.'
    }

    # ---------------------------------------------------------------- 7) finalizar
    Passo 7 'Finalizando...'
    Copy-Item 'LEIA-ME.txt' (Join-Path $pastaApp 'LEIA-ME.txt') -ErrorAction SilentlyContinue
    $zipFinal = Join-Path $saida 'MTEC_Informatica_Portatil.zip'
    if (Test-Path $zipFinal) { Remove-Item $zipFinal -Force }
    Compress-Archive -Path $pastaApp -DestinationPath $zipFinal -Force

    Write-Host ""
    Write-Host "=====================================================" -ForegroundColor Green
    Write-Host "  PRONTO!" -ForegroundColor Green
    Write-Host "  Executavel : $exe" -ForegroundColor Green
    Write-Host "  Para levar para outro PC, copie a pasta inteira ou o ZIP:" -ForegroundColor Green
    Write-Host "  $zipFinal" -ForegroundColor Green
    Write-Host "=====================================================" -ForegroundColor Green
    exit 0
}
catch {
    Write-Host ""
    Write-Host "=====================================================" -ForegroundColor Red
    Write-Host "  ERRO: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "=====================================================" -ForegroundColor Red
    exit 1
}
