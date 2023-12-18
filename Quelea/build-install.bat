set isxpath="c:\program files (x86)\inno setup 6"
set isx=%isxpath%\iscc.exe
set iwz=quelea64.iss
%isx% "%iwz%" /dMyAppVersion=%1 /O /F /q"Quelea"