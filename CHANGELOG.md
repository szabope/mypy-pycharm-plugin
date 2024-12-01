# MyPy Pycharm Plugin Changelog

## [Unreleased]

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

[Unreleased]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.4...HEAD
[1.0.4]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/szabope/mypy-pycharm-plugin/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/szabope/mypy-pycharm-plugin/commits/v1.0.0
