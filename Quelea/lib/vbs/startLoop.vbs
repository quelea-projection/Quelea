' Script to start looping the current presentation.
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
	pptAppl.ActivePresentation.Slides.Range.SlideShowTransition.AdvanceOnTime = TRUE
	pptAppl.ActivePresentation.SlideShowSettings.AdvanceMode = 2
	pptAppl.ActivePresentation.Slides.Range.SlideShowTransition.AdvanceTime = oArgs(0)
	pptAppl.ActivePresentation.SlideShowSettings.LoopUntilStopped = TRUE
        pptAppl.ActivePresentation.SlideShowSettings.Run
	WScript.Echo "Successfully started loop with " + oArgs(0) + " seconds appart"
End If
On Error Goto 0           ' Don't resume on Error