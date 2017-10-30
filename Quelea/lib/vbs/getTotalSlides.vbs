' Script to return the total amount of slides of the current presentation.
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
	WScript.Echo pptAppl.ActivePresentation.Slides.Count 
End If
On Error Goto 0           ' Don't resume on Error