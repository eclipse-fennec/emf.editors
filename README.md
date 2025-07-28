[![CI Build](https://github.com/eclipse-fennec/emf.editors/actions/workflows/build.yml/badge.svg)](https://github.com/eclipse-fennec/emf.editors/actions/workflows/build.yml)[![License](https://github.com/eclipse-fennec/emf.editors/actions/workflows/license.yml/badge.svg)](https://github.com/eclipse-fennec/emf.editors/actions/workflows/license.yml )[![Sonar](https://github.com/eclipse-fennec/emf.editors/actions/workflows/sonar.yml/badge.svg)](https://github.com/eclipse-fennec/emf.editors/actions/workflows/sonar.yml )

# Eclipse Fennec EMF Editors

Eclipse Fennec EMF Editors is a reflective EMF editor implementation that extends the standard Eclipse EMF editing framework. It provides enhanced editing capabilities for EMF models through a custom reflective editor with improved resource handling and context menu support.

## Features

- **Enhanced EMF Editor**: Extends the standard EcoreEditor with custom adapter factories
- **Reflective Editing**: Provides reflective editing capabilities for EMF models
- **Resource Management**: Enhanced resource item providers with context menu support
- **XMI Content Support**: Integrated support for XMI content type editing
- **OSGi Architecture**: Built as modular OSGi bundles for easy integration

## Installation

### Install from Update Site

1. Open Eclipse IDE
2. Go to **Help** → **Install New Software...**
3. Click **Add...** to add a new update site
4. Enter the following details:
   - **Name**: Eclipse Fennec EMF Editors
   - **Location**: `jar:https://devel.data-in-motion.biz/jenkins/job/Eclipse-Fennec/job/emf.editors/job/snapshot/lastSuccessfulBuild/artifact/org.eclipse.fennec.emf.editor.reflective.xmi/generated/p2updatesite.zip!/`
5. Click **OK**
6. Select the Eclipse Fennec EMF Editors feature from the list
7. Follow the installation wizard to complete the installation
8. Restart Eclipse when prompted

### Manual Installation

Alternatively, you can download the P2 update site zip file directly and install it locally:
1. Download the latest P2 update site from the Jenkins build artifacts
2. In Eclipse, go to **Help** → **Install New Software...**
3. Click **Add...** → **Local...** and select the downloaded zip file
4. Follow the installation process

## Usage

After installation, the Fennec EMF Editor will automatically register itself for XMI content types. To use the editor:

1. Open any XMI file in your Eclipse workspace
2. The Fennec EMF Editor should open automatically for XMI files
3. Use the enhanced context menus and editing features provided by the reflective editor

## Architecture

The project consists of two main OSGi bundles:

- **org.eclipse.fennec.emf.editor.reflective.xmi** - The main editor implementation
- **org.eclipse.fennec.emf.editor.reflective.xmi.launch** - Launch configurations and test setup

### Core Components

- **FennecEcoreEditor** - Main editor class extending EcoreEditor
- **ResourceItemProviderAdapterFactory** - Custom adapter factory for resource providers
- **ResourceItemProvider** - Enhanced item provider with context menu support
- **ResourceCommand** - Command for adding EObjects to resources
- **ResourcePropertyDescriptor** - Custom property descriptors for resource editing

## Development

### Building from Source

This project uses BND Tools with Gradle wrapper:

```bash
# Build all bundles
./gradlew build

# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Generate coverage report
./gradlew codeCoverageReport
```

### Testing

To test the editor in a development environment:
1. Use the `testlaunch.bndrun` configuration to run Eclipse IDE with the editor
2. The editor will be available for XMI files in the test Eclipse instance

## Links

* [Documentation](https://github.com/eclipse-fennec/emf.editors)
* [Source Code](https://github.com/eclipse-fennec/emf.editors) (clone with `scm:git:git@github.com:eclipse-fennec/emf.editors.git`)


## Developers

* **Juergen Albert** (jalbert) / [j.albert@data-in-motion.biz](mailto:j.albert@data-in-motion.biz) @ [Data In Motion](https://www.datainmotion.de) - *architect*, *developer*
* **Mark Hoffmann** (mhoffmann) / [m.hoffmann@data-in-motion.biz](mailto:m.hoffmann@data-in-motion.biz) @ [Data In Motion](https://www.datainmotion.de) - *developer*, *architect*

## Licenses

**Eclipse Public License 2.0**

## Copyright

Data In Motion Consuling GmbH - All rights reserved

-+
Data In Motion Consuling GmbH - [info@data-in-motion.biz](mailto:info@data-in-motion.biz)
