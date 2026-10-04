; Meta Monjurul - Windows installer (Inno Setup 6)
; Mirrors the Meta Hawladar installer/windows.iss contract:
; the CI workflow calls ISCC.exe with these /D defines:
;   MyAppExeName         - main exe file name, e.g. "MetaMonjurul.exe"
;   MyAppVersion          - app version, e.g. "1.0.0"
;   MyProfile             - build profile, e.g. "template"
;   MySourceDir           - folder with the portable build (exe + runtime)
;   MyOutputDir           - where to write the installer
;   MyOutputBaseFilename  - installer file name WITHOUT extension
;
; Installs per-user by default (no admin needed), adds Start-menu + Desktop
; shortcuts and an "Apps & features" uninstall entry, upgrades in place.

#define MyAppName "Meta Monjurul"
#define MyAppPublisher "Meta Monjurul"
#define MyAppURL "https://github.com/"
#define MyAppExeName "MetaMonjurul.exe"

[Setup]
AppId={{8F3A2B1C-4D5E-4F60-9A1B-METAMONJURUL01}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
PrivilegesRequiredOverridesAllowed=dialog
OutputDir={#MyOutputDir}
OutputBaseFilename={#MyOutputBaseFilename}
Compression=lzma2/max
SolidCompression=yes
WizardStyle=modern
UninstallDisplayName={#MyAppName}
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
CloseApplications=yes
RestartApplications=no

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "{#MySourceDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent
