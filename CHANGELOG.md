# MyPy Pycharm Plugin Changelog

## [Unreleased]

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
     - Rescan: **added** - runs MyPy scan on the latest target(s)

[Unreleased]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.15...HEAD
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
