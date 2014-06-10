set hour=%time:~0,2%
if "%time:~0,1%"==" " set hour=0%time:~1,1%
"c:\Program Files\7-Zip\7z.exe" a -r e:\copy\bee%date:~2,2%%date:~5,2%%date:~8,2%%hour%.zip -xr@samples\cli\zip_exclude.txt