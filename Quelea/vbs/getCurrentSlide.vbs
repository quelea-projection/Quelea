' Script to return the active slide of the current presentation.
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
    If pptAppl.SlideShowWindows.Count > 0 Then
        WScript.Echo pptAppl.SlideShowWindows(1).View.Slide.SlideIndex
    End If
End If
On Error Goto 0           ' Don't resume on Error

