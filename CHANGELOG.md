# Mypy Pycharm Plugin Changelog

## [Unreleased]

## [2.1.2] - 2025-11-23

- fix misleading description within configuration panel
- do not run annotator for virtual files (e.g. python console)
- move wsl validation to common
- initialize working directory on startup if empty

## [2.1.1] - 2025-11-18

- Changelog update - `v2.1.0` by @github-actions[bot] in https://github.com/szabope/mypy-pycharm-plugin/pull/117
- Annotator - add debug logging by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/122

## [2.1.0] - 2025-11-10

- Add code to type ignore by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/116
  * make ignore intention use code reported by mypy to add ignore comment

## [2.0.1] - 2025-11-09

- Changelog update - `v2.0.0` by @github-actions[bot] in https://github.com/szabope/mypy-pycharm-plugin/pull/113
- 2.0.1 by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/114
  * cleanup AsyncScanService
  * simplify scan actions and their tests
  * fix mypy cannot be killed
  * simplify avoiding posix
  * fix no intention for triple-quoted multiline strings
  * remove some ugliness from dialog
  * fix stopping scan leaves tree in what seems to be in-progress state

## [2.0.0] - 2025-11-05

- Changelog update - `v1.0.26` by @github-actions[bot] in https://github.com/szabope/mypy-pycharm-plugin/pull/110
- Add option to run mypy as module by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/112

## [1.0.26] - 2025-11-02

- Changelog update - `v1.0.25` by @github-actions[bot] in https://github.com/szabope/mypy-pycharm-plugin/pull/106
- rename message bundle to make it more unique by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/109

## [1.0.25] - 2025-10-12

- Changelog update - `v1.0.24` by @github-actions[bot] in https://github.com/szabope/mypy-pycharm-plugin/pull/103
- fix missing mypy inspection description by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/105

## [1.0.24] - 2025-09-24

- make plugin use local light package management service by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/101
- Changelog update - `v1.0.23` by @github-actions[bot] in https://github.com/szabope/mypy-pycharm-plugin/pull/99
- make PythonCore optional by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/102

## [1.0.23] - 2025-08-12

- Changelog update - `v1.0.22` by @github-actions[bot] in https://github.com/szabope/mypy-pycharm-plugin/pull/95
- fix conflict with pylint by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/98

## [1.0.22] - 2025-08-03

- Changelog update - `v1.0.21` by @github-actions[bot] in https://github.com/szabope/mypy-pycharm-plugin/pull/93
- fix package manager issue by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/94
- Support for 2025.2-rc

## [1.0.21] - 2025-08-03

Support for 2025.2-rc

## [1.0.20] - 2025-08-03

- Changelog update - `v1.0.19` by @github-actions[bot] in https://github.com/szabope/mypy-pycharm-plugin/pull/87
- Support 2025.2-rc by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/89
- Release 1.0.20 by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/90

## [1.0.19] - 2025-05-09

- Changelog update - `v1.0.18` by @github-actions in https://github.com/szabope/mypy-pycharm-plugin/pull/78
- Support IU 2025.1.1 by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/86

## [1.0.18] - 2025-03-20

- Changelog update - `v1.0.17` by @github-actions in https://github.com/szabope/mypy-pycharm-plugin/pull/72
- Fix capitalization of mypy by @gy-mate in https://github.com/szabope/mypy-pycharm-plugin/pull/75
- Release 1.0.18 by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/77
- @gy-mate made their first contribution in https://github.com/szabope/mypy-pycharm-plugin/pull/75

## [1.0.17] - 2025-03-11

- Changelog update - `v1.0.16` by @github-actions in https://github.com/szabope/mypy-pycharm-plugin/pull/67
- add debug logging to mypy command execution by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/70
- v1.0.17 by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/71

## [1.0.16] - 2025-03-10

- Changelog update - `v1.0.15` by @github-actions in https://github.com/szabope/mypy-pycharm-plugin/pull/63
- remove dependency to grazie plugin by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/65
- v1.0.16 by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/66

## [1.0.15] - 2025-02-23

- support for 2025.1 EAP by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/61

## [1.0.14] - 2025-02-03

- Changelog update - `v1.0.13` by @github-actions in https://github.com/szabope/mypy-pycharm-plugin/pull/56
- add test for fixing config file regression by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/57
- Fix/spaces in arguments by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/59

## [1.0.13] - 2025-02-02

- Changelog update - `v1.0.12` by @github-actions in https://github.com/szabope/mypy-pycharm-plugin/pull/51
- Fix/config usage regression by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/55

## [1.0.12] - 2025-01-27

- Changelog update - `v1.0.11` by @github-actions in https://github.com/szabope/mypy-pycharm-plugin/pull/47
- Accommodate spaces when executing commands by @seanf in https://github.com/szabope/mypy-pycharm-plugin/pull/48
- Add test for space in mypy path #48 by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/49
- bump version by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/50
- @seanf made their first contribution in https://github.com/szabope/mypy-pycharm-plugin/pull/48

## [1.0.11] - 2025-01-13

- fix falsely reported issues in editor in case of circular imports due to the check is running on a temp file (https://github.com/szabope/mypy-pycharm-plugin/issues/45 )

## [1.0.10] - 2025-01-07

- add install and detect buttons to mypy settings
- mypy settings: make validation error prevent user from saving the form
- mypy settings: installing mypy to automatically fill path to executable with autodetect result, if it is empty
- update readme

## [1.0.9] - 2025-01-05

- enable setting mypy.bat as executable on windows

## [1.0.8] - 2025-01-01

- organize tests
- increase coverage
- make sure there is a single mypy notification at a time
- Fix Issue #36 - do not automatically save the document loaded into the editor to avoid side effects

## [1.0.7] - 2024-12-10

- bump to 1.0.7
- notification to offer installing mypy only if local interpreter is in use
- handle mypy installation errors
- show mypy installation error in dialog
- fix remote sdk recognition
- fix autodetect output handling
- replace banner with sticky bubble to inform about missing configuration
- execute mypy configuration check when sdk is replaced
- show failure details in dialog
- make sure that there are no duplicate install balloon
- add progress bar to installation
- fix configuration migration from old plugin

## [1.0.6] - 2024-12-05

- make sure that working directory is set for when calling mypy
- enable configuring project directory

## [1.0.5] - 2024-12-03

- fix annotator's annoying balloon errors
- fix no module issue with scanning file being edited
- fix inconsistency between currently edited file's annotations and toolwindow scan results
- fix toolwindow not realizing that scan is done and keep showing in-progress state
- fix possible deadlock on EDT

## [1.0.4] - 2024-12-01

- Fix error handling
- Add debug logs
- Code cleanup
- Ensure that configured executable and config paths are valid before using them
- slow EDT bugfix
- extend recommended arguments
- notification of failing mypy to be shown in a balloon
- add option to exclude non-project directories (marked as excluded)
- extend README with some troubleshooting info
- Stop the error text mixed into mypy stdout to cause IDE error, debug log is enough here.
- Don't show IDE error when where/which mypy[.exe] exits with error

## [1.0.3] - 2024-11-25

- Fixed lagging annotation in editor

## [1.0.2] - 2024-11-23

- Make Terminal plugin a mandatory dependency by @szabope in https://github.com/szabope/mypy-pycharm-plugin/pull/16
- @github-actions made their first contribution in https://github.com/szabope/mypy-pycharm-plugin/pull/9

## [1.0.1] - 2024-11-23

Fix Naming collisions with old plugin

## [1.0.0] - 2024-11-22

### MVP

- Scan from right-click menu
   - editor
   - editor tab
   - project view on files or directories
- Toolbar actions were simplified (compared to the original plugin):
     - Close toolbar: **removed**
     - Check module: **removed**
     - Check project: **removed**
     - Check all modified files: **removed**
     - Check files in the current changelist: **removed**
     - Clear all: **removed**
     - Severity filters: **removed**
     - Rescan: **added** - runs mypy scan on the latest target(s)

[Unreleased]: https://github.com/szabope/mypy-pycharm-plugin/compare/v2.1.2...HEAD
[2.1.2]: https://github.com/szabope/mypy-pycharm-plugin/compare/v2.1.1...v2.1.2
[2.1.1]: https://github.com/szabope/mypy-pycharm-plugin/compare/v2.1.0...v2.1.1
[2.1.0]: https://github.com/szabope/mypy-pycharm-plugin/compare/v2.0.1...v2.1.0
[2.0.1]: https://github.com/szabope/mypy-pycharm-plugin/compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.26...v2.0.0
[1.0.26]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.25...v1.0.26
[1.0.25]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.24...v1.0.25
[1.0.24]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.23...v1.0.24
[1.0.23]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.22...v1.0.23
[1.0.22]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.21...v1.0.22
[1.0.21]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.20...v1.0.21
[1.0.20]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.19...v1.0.20
[1.0.19]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.18...v1.0.19
[1.0.18]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.17...v1.0.18
[1.0.17]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.16...v1.0.17
[1.0.16]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.15...v1.0.16
[1.0.15]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.14...v1.0.15
[1.0.14]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.13...v1.0.14
[1.0.13]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.12...v1.0.13
[1.0.12]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.11...v1.0.12
[1.0.11]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.10...v1.0.11
[1.0.10]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.9...v1.0.10
[1.0.9]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.8...v1.0.9
[1.0.8]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.7...v1.0.8
[1.0.7]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.6...v1.0.7
[1.0.6]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.5...v1.0.6
[1.0.5]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.4...v1.0.5
[1.0.4]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/szabope/mypy-pycharm-plugin/commits/v1.0.0
