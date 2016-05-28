' Script to move to the desired slide of the current presentation.
' Returns warning if PowerPoint is not running or message if it was successful.
' @author Arvid

Option Explicit

' Get input from Java/command line
Dim oArgs
Set oArgs = WScript.Arguments

On Error Resume Next
dim pptAppl
set pptAppl = GetObject(, "Powerpoint.Application")
If Err.Number <> 0 Then
    WScript.Echo "PowerPoint is not running"
    Err.Clear             ' Clear the Error
else
	pptAppl.SlideShowWindows(1).View.GotoSlide oArgs(0)
	WScript.Echo "Successfully moved to slide " + oArgs(0)
End If
On Error Goto 0           ' Don't resume on Error