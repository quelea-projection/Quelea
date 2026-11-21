Option Explicit
Dim pptAppl, presentation, savedState
If WScript.Arguments.Count = 0 Then
    WScript.Echo "Could not close presentation: no file was supplied"
    WScript.Quit 1
End If
On Error Resume Next
Set pptAppl = GetObject(, "PowerPoint.Application")
If Err.Number <> 0 Then
    WScript.Echo "PowerPoint is not running"
    WScript.Quit 0
End If
For Each presentation In pptAppl.Presentations
    If StrComp(presentation.FullName, WScript.Arguments(0), vbTextCompare) = 0 Then
        Err.Clear
        savedState = presentation.Saved
        If Err.Number <> 0 Then
            WScript.Echo "Could not close presentation: unable to check for unsaved changes"
            WScript.Quit 1
        End If
        If savedState <> -1 Then
            WScript.Echo "Could not close presentation: it has unsaved changes"
            WScript.Quit 1
        End If
        Err.Clear
        presentation.SlideShowWindow.View.Exit
        Err.Clear ' The user may already have ended this show.
        savedState = presentation.Saved
        If Err.Number <> 0 Or savedState <> -1 Then
            WScript.Echo "Could not close presentation: it may have unsaved changes"
            WScript.Quit 1
        End If
        presentation.Close
        If Err.Number <> 0 Then
            WScript.Echo "Could not close presentation: " & Err.Description
            WScript.Quit 1
        End If
        WScript.Echo "Quelea presentation closed"
        WScript.Quit 0
    End If
Next
WScript.Echo "Quelea presentation is no longer open"
On Error Goto 0

