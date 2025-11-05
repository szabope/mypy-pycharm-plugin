# mypy-pycharm
[![Apache-2.0 license](https://img.shields.io/github/license/szabope/mypy-pycharm-plugin.svg?style=plastic)](https://github.com/szabope/mypy-pycharm-plugin/blob/master/LICENSE)

<!-- Plugin description -->
This plugin provides PyCharm with both real-time and on-demand scanning capabilities using an external mypy tool.\
It is the rework of [Roberto Leinardi](https://github.com/szabope/mypy-pycharm-plugin?tab=readme-ov-file#acknowledgements)'s [mypy-pycharm](https://github.com/leinardi/mypy-pycharm) plugin.[ Click here](https://github.com/szabope/mypy-pycharm-plugin?tab=readme-ov-file#differences-from-the-original-plugin) to see differences.

[Mypy](https://github.com/python/mypy), as described by its authors:
>Mypy is a static type checker for Python.
>
>Type checkers help ensure that you're using variables and functions in your code correctly. With mypy, add type hints (PEP 484) to your Python programs, and mypy will warn you when you use those types incorrectly.

![low_res_mypy plugin screenshot](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/3b600281f84ecec09d345ec7541c39c6b705ddff/art/results_lowres.png)
<!-- Plugin description end -->

## Requirements
- mypy >= 1.11.0
- mypy must be executable by the IDE. *e.g. mypy in WSL won't work with IDE running on Windows*
- mypy does not need to be installed into the project's environment, it can be configured independently


## Installation steps
https://www.jetbrains.com/help/pycharm/managing-plugins.html#Managing_Plugins.topic

## Configuration
Configuration is done on a project basis. `mypy` executable validation **executes the candidate** with `-V` to validate its version.

### Automated configuration
Upon project load, the plugin looks for existing settings for Leinardi's mypy plugin and makes a copy of them. Executable only set if the version of mypy is supported.\
In case such configuration was not found `Use project SDK` option is selected. 
If there is no python SDK set for the project or `mypy` is not installed for it, the user gets notified:
![mypy_plugin_incomplete_configuration_screenshot](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/3b600281f84ecec09d345ec7541c39c6b705ddff/art/mypy_not_set.png)

### Manual configuration
You can modify settings at [Tools](https://www.jetbrains.com/help/pycharm/settings-tools.html#Settings_Tools.topic) / **Mypy**.
![mypy plugin screenshot](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/829f08ceec6ba06c8b7b25a9c819b5c7fc9350ef/art/settings.png)

### Inspection severity
Mypy severity level is set to `Error` by default. You can change this in [inspection settings](https://www.jetbrains.com/help/pycharm/inspections-settings.html#Inspections_Settings.topic).

## Usage

**Scan with Mypy** ![](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/338908f67473081858a50cf55ecf6e4c37e69fd4/art/mypyScanAction.svg) 
action is available in right-click menus for the Python file loaded into the editor, its tab, 
and Python files and directories in the project and changes views. You may select multiple targets, 
but all of them has to be either a Python file or a directory.\
**Rescan Latest** ![](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/338908f67473081858a50cf55ecf6e4c37e69fd4/art/refresh.svg) 
action is available within Mypy toolwindow. It clears the results and re-runs mypy for the latest target. 
Mypy configuration is not retained from the previous run.\
**Scan Editor** ![](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/338908f67473081858a50cf55ecf6e4c37e69fd4/art/execute.svg) 
action is available within Mypy toolwindow. It clears the results and runs mypy for the one file that is open 
and currently focused in the Editor.

![mypy plugin screenshot](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/3b600281f84ecec09d345ec7541c39c6b705ddff/art/menu.png)

![mypy plugin screenshot](https://raw.githubusercontent.com/szabope/mypy-pycharm-plugin/3b600281f84ecec09d345ec7541c39c6b705ddff/art/results.png)

## FAQ
### Scan fails with: `External tool failed with error.` or `External tool returned unexpected output.`
This indicates that the external mypy tool has exited with an error. The plugin can't fix these.
#### Details may contain something like this: `mypy: "mypy/typeshed/stubs/mypy-extensions/mypy_extensions.pyi" shadows library module "mypy_extensions"`
In this case you may want to add `--exclude \.pyi$` to the arguments in mypy settings. 
Another switch `--explicit-package-bases` may also work.
#### Or details may be like `Duplicate module named "a"`
You can exclude containing directory:
 - make sure that `Settings > Tools > Mypy > Exclude non-project files` is checked, so all directories that are marked as excluded will also be excluded from mypy scan.
 - `Mark Directory as > Excluded`

For further mypy configuration options, please see `mypy -h`

You may get more insight into the plugin here: [Debug](https://github.com/szabope/mypy-pycharm-plugin?tab=readme-ov-file#debug) 

## Debug
Open `Help > Diagnostic Tools > Debug Log Settings...`\
Enter `works.szabope.plugins.mypy:trace`\
Hit `[Ok]`\
Then you can see debug logs in idea.log (`Help > Open Log in Editor`)\
**_Keep in mind that the log may contain sensitive information._**

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
