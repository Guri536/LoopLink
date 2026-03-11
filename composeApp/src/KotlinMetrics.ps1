# Kotlin Metrics.ps1
$files = Get-ChildItem -Recurse -Filter *.kt

$metrics = foreach ($file in $files) {
    $content = Get-Content $file.FullName

    $totalLines   = $content.Count
    $emptyLines   = ($content | Where-Object { $_ -match '^\s*$' }).Count
    $commentLines = ($content | Where-Object { $_ -match '^\s*(//|/\*|\*|\*/)' }).Count
    $codeLines    = $totalLines - $emptyLines - $commentLines
    $functionCount = ($content | Select-String -Pattern '^\s*fun\s+' | Measure-Object).Count
    $classCount    = ($content | Select-String -Pattern '^\s*(class|object)\s+' | Measure-Object).Count
    $dataClassCount = ($content | Select-String -Pattern '^\s*data\s+class\s+' | Measure-Object).Count
    $importCount   = ($content | Select-String -Pattern '^\s*import\s+' | Measure-Object).Count

    [PSCustomObject]@{
        File           = $file
        Lines          = $totalLines
        CodeLines      = $codeLines
        EmptyLines     = $emptyLines
        CommentLines   = $commentLines
        Functions      = $functionCount
        Classes        = $classCount
        DataClasses    = $dataClassCount
        Imports        = $importCount
        SizeKB         = [math]::Round($file.Length / 1KB, 1)
    }
}

# Display per-file breakdown
$metrics | Format-Table -AutoSize

# Totals summary
""
"=== Summary ==="
$summary = [PSCustomObject]@{
    Files        = $metrics.Count
    TotalLines   = ($metrics.Lines | Measure-Object -Sum).Sum
    CodeLines    = ($metrics.CodeLines | Measure-Object -Sum).Sum
    Comments     = ($metrics.CommentLines | Measure-Object -Sum).Sum
    Functions    = ($metrics.Functions | Measure-Object -Sum).Sum
    Classes      = ($metrics.Classes | Measure-Object -Sum).Sum
    DataClasses  = ($metrics.DataClasses | Measure-Object -Sum).Sum
    Imports      = ($metrics.Imports | Measure-Object -Sum).Sum
    TotalSizeKB  = ($metrics.SizeKB | Measure-Object -Sum).Sum
}
$summary | Format-List | Export-Csv KotlinMetrics.csv -NoTypeInformation