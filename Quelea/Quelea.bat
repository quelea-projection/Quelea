java -Xms1024m -Xmx1024m -jar Quelea.jar
IF %ERRORLEVEL% == 0 exit
SETLOCAL ENABLEDELAYEDEXPANSION
SET count=1
FOR /F "tokens=* USEBACKQ" %%F IN (`java -Xms1024m -Xmx1024m -jar Quelea.jar`) DO (
SET var!count!=%%F
SET /a count=!count!+1
)
cscript MessageBox.vbs "It seems Quelea failed to start. A message is about to be displayed which will hopefully offer some technical debugging information that can get you some help (though it won't make much sense to you!) Please take a screenshot of it and send it to "michael@quelea.org"."
cscript MessageBox.vbs "%var1%   ;   %var2%   ;   %var3%   ;   %var4%   ;   %var5%"
ENDLOCAL