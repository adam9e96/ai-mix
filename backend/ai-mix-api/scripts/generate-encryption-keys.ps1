# PowerShell 스크립트: 암호화 키 생성
# 사용법: .\generate-encryption-keys.ps1

Write-Host "=== 암호화 키 생성 ===" -ForegroundColor Green
Write-Host ""

# 암호화 키 생성 (32바이트, Base64)
$encryptionKeyBytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RNGCryptoServiceProvider]::Create()
$rng.GetBytes($encryptionKeyBytes)
$encryptionKey = [Convert]::ToBase64String($encryptionKeyBytes)

# Salt 생성 (8바이트, 16진수)
$saltBytes = New-Object byte[] 8
$rng.GetBytes($saltBytes)
$salt = ($saltBytes | ForEach-Object { $_.ToString("x2") }) -join ""

Write-Host "암호화 키 (API_KEY_ENCRYPTION_KEY):" -ForegroundColor Yellow
Write-Host $encryptionKey
Write-Host ""

Write-Host "Salt (API_KEY_ENCRYPTION_SALT):" -ForegroundColor Yellow
Write-Host $salt
Write-Host ""

Write-Host "=== 환경변수 설정 (현재 세션) ===" -ForegroundColor Green
Write-Host ""
$env:API_KEY_ENCRYPTION_KEY = $encryptionKey
$env:API_KEY_ENCRYPTION_SALT = $salt
Write-Host "환경변수가 현재 PowerShell 세션에 설정되었습니다." -ForegroundColor Cyan
Write-Host ""

Write-Host "=== 영구적으로 설정하려면 ===" -ForegroundColor Green
Write-Host "[System.Environment]::SetEnvironmentVariable('API_KEY_ENCRYPTION_KEY', '$encryptionKey', 'User')"
Write-Host "[System.Environment]::SetEnvironmentVariable('API_KEY_ENCRYPTION_SALT', '$salt', 'User')"
Write-Host ""
