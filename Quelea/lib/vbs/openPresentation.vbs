' Script to open a new PowerPoint presentation.
' Returns warning if PowerPoint is not running or message if it was successful.
' @author Arvid, based on code by Kelvin Sung

Option Explicit

' Get input from Java/command line
Dim oArgs
Set oArgs = WScript.Arguments

' Object variable
dim objShell
Set objShell = CreateObject("WScript.Shell")

On Error Resume Next
dim pptAppl
set pptAppl = GetObject(, "Powerpoint.Application")
If Err.Number <> 0 Then
	' Create new PowerPoint instanse
	set pptAppl = CreateObject("Powerpoint.Application")


	if (openPPTDocument()) then
		' Minimize the main window
		pptAppl.WindowState = 2

		' Move focus to window whose title begins with PowerPoint
		objShell.AppActivate "PowerPoint"
   
	else
		MsgBox("No PowerPoint document found")
		pptAppl.Quit
	end if
else
	WScript.Echo "PowerPoint is already running."
    Err.Clear             ' Clear the Error
End If
On Error Goto 0           ' Don't resume on Error

'-----------------------------------------------------------------------------
' FUNCTION: openPPTDocument()
'
' Input:   none (uses the global dlgOpen to let user open a .ppt document)
' Returns: none (changes the global pptAppl variable to have an opened document)
' Error:   Checks to make sure input is a number
'
'   Remark: 
FUNCTION openPPTDocument()
	dim objPresentation
	dim objSlideShow
	
	openPPTDocument = FALSE
    pptAppl.Visible = TRUE                    ' first set ppt to visible
    Set objPresentation = pptAppl.Presentations.Open(oArgs(0))   ' now open the file 
	'objPresentation.SlideShowSettings.ShowType = 3	' start slide show
	objPresentation.SlideShowSettings.Run ' apply changes
	
	
    openPPTDocument = TRUE
end FUNCTION
