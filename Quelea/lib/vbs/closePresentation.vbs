' Script to close a PowerPoint.
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
' Avoid getting asked to save changes
	pptAppl.Saved = True
' Close all windows of PowerPoint
	pptAppl.Quit
Set pptAppl = Nothing
End If
On Error Goto 0           ' Don't resume on Error

