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
        dim index
        index = pptAppl.ActivePresentation.SlideShowWindow.View.Slide.SlideIndex
        pptAppl.ActivePresentation.SlideShowWindow.View.Exit
	pptAppl.ActivePresentation.SlideShowSettings.LoopUntilStopped = FALSE
        pptAppl.ActivePresentation.SlideShowSettings.Run
        pptAppl.SlideShowWindows(1).View.GotoSlide index
	WScript.Echo "Successfully stopped looping"
End If
On Error Goto 0           ' Don't resume on Error