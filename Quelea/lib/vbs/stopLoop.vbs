' Script to stop looping the current presentation.
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
	pptAppl.ActivePresentation.Slides.Range.SlideShowTransition.AdvanceOnTime = FALSE
    pptAppl.ActivePresentation.SlideShowSettings.Run
	WScript.Echo "Successfully stopped loop"
End If
On Error Goto 0           ' Don't resume on Error