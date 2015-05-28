set CAT=build

dir "%%CAT%%"/s/b/a | sort /r >> %TEMP%\files2del.txt 
for /f "delims=;" %%D in (%TEMP%\files2del.txt) do (del /q "%%D" & rd "%%D") 
del /q %TEMP%\files2del.txt

set CAT=dist

dir "%%CAT%%"/s/b/a | sort /r >> %TEMP%\files2del.txt 
for /f "delims=;" %%D in (%TEMP%\files2del.txt) do (del /q "%%D" & rd "%%D") 
del /q %TEMP%\files2del.txt

rd build
rd dist