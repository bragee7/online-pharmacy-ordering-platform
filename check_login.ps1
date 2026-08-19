$body='{"email":"admin@medicare.com","password":"password"}'
try {
  $r=Invoke-WebRequest 'http://localhost:8082/api/auth/login' -Method POST -ContentType 'application/json' -Body $body -TimeoutSec 5 -UseBasicParsing
  Write-Host "STATUS:$($r.StatusCode)"
  Write-Host $r.Content.Substring(0,[Math]::Min(600,$r.Content.Length))
} catch {
  Write-Host "CATCH:$($_.Exception.Message)"
  if ($_.ErrorDetails -and $_.ErrorDetails.Message) { Write-Host "BODY:"; Write-Host $_.ErrorDetails.Message.Substring(0,[Math]::Min(1000,$_.ErrorDetails.Message.Length)) }
  else { Write-Host "no ErrorDetails" }
}
