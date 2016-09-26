' Script to move to the previous slide of the current presentation.
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
	pptAppl.SlideShowWindows(1).View.Previous
	WScript.Echo "Successfully moved to previous slide"
End If
On Error Goto 0           ' Don't resume on Error