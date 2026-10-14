param(
    [ValidateSet('local', 'dev')][string]$Profile = 'dev',
    [ValidateSet('run', 'test', 'package')][string]$Task = 'run'
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
    if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin/java.exe'))) {
        $jdkCandidates = @(
            (Join-Path $env:USERPROFILE '.jdks'),
            'C:/Program Files/Java',
            'C:/Program Files/Eclipse Adoptium'
        )
        $jdk = $jdkCandidates | Where-Object { Test-Path $_ } | ForEach-Object {
            Get-ChildItem -LiteralPath $_ -Directory | Where-Object {
                $_.Name -match '25' -and (Test-Path (Join-Path $_.FullName 'bin/java.exe'))
            }
        } | Select-Object -First 1
        if (-not $jdk) { throw 'JDK 25를 설치하고 JAVA_HOME을 지정해 주세요.' }
        $env:JAVA_HOME = $jdk.FullName
    }
    $previousErrorAction = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $javaVersion = & (Join-Path $env:JAVA_HOME 'bin/java.exe') -version 2>&1 | Out-String
        $javaExit = $LASTEXITCODE
    } finally { $ErrorActionPreference = $previousErrorAction }
    if ($javaExit -ne 0 -or $javaVersion -notmatch 'version "25[."]') {
        throw '이 프로젝트는 JDK 25로 실행해 주세요.'
    }
    if (-not $env:MAVEN_USER_HOME) { $env:MAVEN_USER_HOME = Join-Path $projectRoot '.m2' }
    $repository = '-Dmaven.repo.local=' + (Join-Path $projectRoot '.m2/repository')
    # Spring imports .env for Supabase; never evaluate its contents as shell code.
    if ($Task -eq 'run') {
        & ./mvnw.cmd $repository '-B' '-ntp' 'spring-boot:run' "-Dspring-boot.run.profiles=$Profile"
    } else {
        # Clean removes compiled classes/resources from the previous application.
        & ./mvnw.cmd $repository '-B' '-ntp' 'clean' $Task
    }
    if ($LASTEXITCODE -ne 0) { throw "Maven 실행 실패 (exit $LASTEXITCODE)" }
} finally { Pop-Location }
