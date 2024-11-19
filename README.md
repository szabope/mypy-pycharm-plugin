# mypy-pycharm
[![GitHub license](https://img.shields.io/github/license/leinardi/mypy-pycharm.svg?style=plastic)](https://github.com/leinardi/mypy-pycharm/blob/master/LICENSE)

<!-- Plugin description -->
This plugin provides PyCharm with both real-time and on-demand scanning capabilities using an external MyPy tool.\
It is the rework of [Roberto Leinardi](https://github.com/szabope/mypy-pycharm-plugin?tab=readme-ov-file#acknowledgements)'s [mypy-pycharm](https://github.com/leinardi/mypy-pycharm) plugin.[ Click here](https://github.com/szabope/mypy-pycharm-plugin?tab=readme-ov-file#differences-from-the-original-plugin) to see differences.

[MyPy](https://github.com/python/mypy), as described by its authors:
>Mypy is a static type checker for Python.
>
>Type checkers help ensure that you're using variables and functions in your code correctly. With mypy, add type hints (PEP 484) to your Python programs, and mypy will warn you when you use those types incorrectly.

![low_res_mypy plugin screenshot](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/146a7eaccf3ad32a127dbdcef7bf4042e943411a/art/results_lowres.png)
<!-- Plugin description end -->

## Installation steps
https://www.jetbrains.com/help/pycharm/managing-plugins.html#Managing_Plugins.topic

## Configuration
Configuration is done on a project basis. Regardless of whether it is set up via the automated way or manually, mypy executable setting validation **executes the candidate** with `-V` to validate its version.

### Automated configuration
Upon project load, the plugin looks for existing settings for Leinardi's mypy plugin and makes a copy of them. Executable only set if the version of mypy is supported.\
If such configuration was not found, the plugin tries to detect the executable by running `where mypy.exe` on Windows, `which mypy` otherwise.

### Manual configuration
You can modify settings at [Tools](https://www.jetbrains.com/help/pycharm/settings-tools.html#Settings_Tools.topic) / **Mypy**.

![mypy plugin screenshot](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/338908f67473081858a50cf55ecf6e4c37e69fd4/art/settings.png)

### Inspection severity
MyPy severity level is set to `Error` by default. You can change this in [inspection settings](https://www.jetbrains.com/help/pycharm/inspections-settings.html#Inspections_Settings.topic).

## Usage

**Scan with Mypy** ![](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/338908f67473081858a50cf55ecf6e4c37e69fd4/art/mypyScanAction.svg) 
action is available in right-click menus for the Python file loaded into the editor, its tab, 
and Python files and directories in the project and changes views. You may select multiple targets, 
but all of them has to be either a Python file or a directory.\
**Rescan Latest** ![](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/338908f67473081858a50cf55ecf6e4c37e69fd4/art/refresh.svg) 
action is available within MyPy toolwindow. It clears the results and re-runs mypy for the latest target. 
Mypy configuration is not retained from the previous run.\
**Scan Editor** ![](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/338908f67473081858a50cf55ecf6e4c37e69fd4/art/execute.svg) 
action is available within MyPy toolwindow. It clears the results and runs mypy for the one file that is open 
and currently focused in the Editor.

![mypy plugin screenshot](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/338908f67473081858a50cf55ecf6e4c37e69fd4/art/menu.png)

![mypy plugin screenshot](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/338908f67473081858a50cf55ecf6e4c37e69fd4/art/results.png)

## FAQ
### Scan fails with: `Mypy executable has thrown an error.`
This indicates that the external mypy tool has exited with an error. The plugin can't fix these. You can do research on the causes, or you can try to run mypy on a subset of targets.

### How can I prevent the code inspection from running on a specific folder?
[Exclude it](https://www.jetbrains.com/help/pycharm/configuring-folders-within-a-content-root.html#mark) from the project.

## Differences from the original plugin
- Toolbar actions were simplified:
  - Close toolbar: **removed**
  - Check module: **removed**
  - Check project: **removed**
  - Check all modified files: **removed**
  - Check files in the current changelist: **removed**
  - Clear all: **removed**
  - Severity filters: **removed**
  - Rescan: **added**

[//]: # (  TODO - severity filter: **grouped**)
- Scan can now be started from the right-click menu within the editor, on an editor tab, and on files or directories 
in the project and changes views.

## Acknowledgements
A huge thanks to [Roberto Leinardi](https://github.com/leinardi) for the creation and maintenance of the original plugin and for the support and guidance in the rework.

## License
```
Copyright 2024 Peter Szabo.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
```
