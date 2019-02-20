set isxpath="c:\program files (x86)\inno setup 5"
set isx=%isxpath%\iscc.exe
REM set iwz=quelea.iss
REM %isx% "%iwz%" /dMyAppVersion=%1 /O /F /q"Quelea"
set iwz=quelea64.iss
%isx% "%iwz%" /dMyAppVersion=%1 /O /F /q"Quelea"