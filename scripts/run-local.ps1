param(
    [ValidateSet('demo', 'demo,dev', 'supabase', 'supabase,dev', 'supabase,shared', 'supabase,shared,dev', 'supabase,prod')][string]$Profile = 'supabase,dev',
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
            Get-ChildItem -LiteralPath $_ -Directory | Where-Object { $_.Name -match '25' -and (Test-Path (Join-Path $_.FullName 'bin/java.exe')) }
        } | Select-Object -First 1
        if (-not $jdk) { throw 'JDK 25를 설치하고 JAVA_HOME을 지정해 주세요.' }
        $env:JAVA_HOME = $jdk.FullName
    }
    # Windows PowerShell treats java -version's normal stderr output as an error.
    $previousErrorAction = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $javaVersion = & (Join-Path $env:JAVA_HOME 'bin/java.exe') -version 2>&1 | Out-String
        $javaExit = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorAction
    }
    if ($javaExit -ne 0) { throw 'Java version check failed.' }
    if ($javaVersion -notmatch 'version "25[."]') { throw '이 프로젝트는 JDK 25로 실행해 주세요.' }
    # Parse plain KEY=VALUE only; never evaluate .env contents as shell code.
    if (Test-Path '.env') {
        foreach ($line in Get-Content '.env' -Encoding utf8) {
            if ($line -match '^\s*([A-Z][A-Z0-9_]*)=(.*)$') {
                $key = $Matches[1]
                $value = $Matches[2].Trim()
                if (-not [Environment]::GetEnvironmentVariable($key, 'Process')) {
                    [Environment]::SetEnvironmentVariable($key, $value, 'Process')
                }
            }
        }
    }
    if (-not $env:MAVEN_USER_HOME) { $env:MAVEN_USER_HOME = Join-Path $projectRoot '.m2' }
    $repository = '-Dmaven.repo.local=' + (Join-Path $projectRoot '.m2/repository')
    if ($Task -eq 'run') {
        $isDemo = $Profile.Split(',') -contains 'demo'
        if (-not $isDemo -and (-not $env:DB_PASSWORD -or $env:DB_PASSWORD.StartsWith('replace-'))) {
            throw '.env 또는 환경 변수에 실제 DB_PASSWORD를 설정해 주세요.'
        }
        if (-not $isDemo -and (-not $env:DB_URL -or -not $env:DB_URL.StartsWith('jdbc:postgresql://') -or -not $env:DB_USERNAME)) {
            throw '.env에 Supabase DB_URL(jdbc:postgresql://)과 DB_USERNAME을 설정해 주세요.'
        }
        & ./mvnw.cmd $repository '-B' '-ntp' 'spring-boot:run' "-Dspring-boot.run.profiles=$Profile"
    } else {
        & ./mvnw.cmd $repository '-B' '-ntp' $Task
    }
    if ($LASTEXITCODE -ne 0) { throw "Maven 실행 실패 (exit $LASTEXITCODE)" }
} finally { Pop-Location }
