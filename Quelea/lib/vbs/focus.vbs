' Move the PowerPoint to front.
' Returns warning if PowerPoint is not running or message if it was successful.
' @author Arvid

Option Explicit
On Error Resume Next
dim pptAppl
set pptAppl = GetObject(, "Powerpoint.Application")
If Err.Number <> 0 Then
    WScript.Echo "PowerPoint is not running"
    Err.Clear             ' Clear the Error
else
	pptAppl.ActivePresentation.Windows(1).Activate
        pptAppl.SlideShowWindows(1).Activate
	WScript.Echo "Successfully moved focus to PowerPoint"
End If
On Error Goto 0           ' Don't resume on Error