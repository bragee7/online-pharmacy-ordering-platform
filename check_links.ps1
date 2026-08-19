Write-Host "---SWAGGER---"
try { $s=Invoke-WebRequest 'http://localhost:8082/swagger-ui.html' -TimeoutSec 5 -UseBasicParsing; Write-Host "swagger $($s.StatusCode) len $($s.Content.Length)" } catch { Write-Host "swagger fail $($_.Exception.Message)" }
Write-Host "---INDEX---"
try { $i=Invoke-WebRequest 'http://localhost:8082/index.html' -TimeoutSec 5 -UseBasicParsing; Write-Host "index $($i.StatusCode) len $($i.Content.Length)" } catch { Write-Host "index fail $($_.Exception.Message)" }
Write-Host "---CATEGORIES---"
try { $c=Invoke-WebRequest 'http://localhost:8082/api/categories' -TimeoutSec 5 -UseBasicParsing; Write-Host "cat $($c.StatusCode) $($c.Content.Substring(0,120))" } catch { Write-Host "cat fail $($_.Exception.Message)" }
Write-Host "---HEALTH---"
try { $h=Invoke-WebRequest 'http://localhost:8082/health' -TimeoutSec 5 -UseBasicParsing; Write-Host "health $($h.Content)" } catch { Write-Host "health fail" }
