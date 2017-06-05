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
        dim state
        state = pptAppl.ActivePresentation.View.State
	WScript.Echo state 'Returns 2 for normal and 3 for black, blank if not started
End If
On Error Goto 0           ' Don't resume on Error

