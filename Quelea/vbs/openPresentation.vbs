Option Explicit
Dim oArgs, pptAppl, presentation, show, objShell
Set oArgs = WScript.Arguments
If oArgs.Count = 0 Then
    WScript.Echo "Could not open presentation: no file was supplied"
    WScript.Quit 1
End If

On Error Resume Next
Set pptAppl = GetObject(, "PowerPoint.Application")
If Err.Number <> 0 Then
    Err.Clear
    Set pptAppl = CreateObject("PowerPoint.Application")
End If
If Err.Number <> 0 Then
    WScript.Echo "Could not start PowerPoint: " & Err.Description
    WScript.Quit 1
End If

' The control scripts use the current slide show. Do not interfere with any
' independently opened presentation or claim ownership of it.
Err.Clear
Dim presentationCount, showCount
presentationCount = pptAppl.Presentations.Count
showCount = pptAppl.SlideShowWindows.Count
If Err.Number <> 0 Then
    WScript.Echo "Could not check whether PowerPoint is already in use: " & Err.Description
    WScript.Quit 1
End If
If presentationCount > 0 Or showCount > 0 Then
    WScript.Echo "PowerPoint is already running a presentation."
    WScript.Quit 0
End If

pptAppl.Visible = True
Err.Clear
Set presentation = Nothing
Set presentation = pptAppl.Presentations.Open(oArgs(0))
If Err.Number <> 0 Or presentation Is Nothing Then
    WScript.Echo "Could not open presentation: " & Err.Description
    WScript.Quit 1
End If
Err.Clear
Set show = Nothing
Set show = presentation.SlideShowSettings.Run
If Err.Number <> 0 Or show Is Nothing Then
    WScript.Echo "Could not start presentation: " & Err.Description
    WScript.Quit 1
End If
pptAppl.WindowState = 2
Set objShell = CreateObject("WScript.Shell")
objShell.AppActivate "PowerPoint"
WScript.Echo "Quelea presentation opened"
On Error Goto 0
