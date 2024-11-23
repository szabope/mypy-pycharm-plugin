# MyPy Pycharm Plugin Changelog

## [Unreleased]

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

[Unreleased]: https://github.com/szabope/mypy-pycharm/compare/v1.0.0...HEAD
[1.0.1]: https://github.com/szabope/mypy-pycharm/commits/v1.0.1
[1.0.0]: https://github.com/szabope/mypy-pycharm/commits/v1.0.0
