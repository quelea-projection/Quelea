' Script to toggle black screen of the current presentation.
' Returns warning if PowerPoint is not running.
' @author Arvid

Option Explicit
On Error Resume Next
dim pptAppl
set pptAppl = GetObject(, "Powerpoint.Application")
If Err.Number <> 0 Then
    WScript.Echo "PowerPoint is not running"
    Err.Clear             ' Clear the Error
else
	If pptAppl.SlideShowWindows(1).View.State <> 3 then
		pptAppl.SlideShowWindows(1).View.State = 3
	elseif pptAppl.SlideShowWindows(1).View.State = 3 then
		pptAppl.SlideShowWindows(1).View.State = 1
	End If
WScript.Echo pptAppl.SlideShowWindows(1).View.State ' Returns 2 for normal and 3 for black
End If
On Error Goto 0           ' Don't resume on Error

