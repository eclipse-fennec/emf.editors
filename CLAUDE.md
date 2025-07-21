# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This project is **Eclipse Fennec EMF Editors** - a reflective EMF editor implementation that extends the standard Eclipse EMF editing framework. It provides enhanced editing capabilities for EMF models through a custom reflective editor.

## Key Architecture

The project consists of two main OSGi bundles:
- `org.eclipse.fennec.emf.editor.reflective.xmi` - The main editor implementation
- `org.eclipse.fennec.emf.editor.reflective.xmi.launch` - Launch configurations and test setup

### Core Components

- **FennecEcoreEditor** (`org.eclipse.fennec.emf.editor.FennecEcoreEditor`) - Main editor class extending EcoreEditor with custom adapter factories and resource handling
- **ResourceItemProviderAdapterFactory** - Custom adapter factory for resource providers
- **ResourceItemProvider** - Enhanced item provider for resource objects with context menu support for adding EObjects
- **ResourceCommand** - Command for adding EObjects to resources (supports single and multiple objects)
- **ResourcePropertyDescriptor** - Custom property descriptors for resource editing

## Build System

This project uses **BND Tools** with Gradle wrapper for building OSGi bundles.

### Core Build Commands

- `./gradlew build` - Build all bundles and generate artifacts
- `./gradlew clean build` - Clean build from scratch
- `./gradlew test` - Run JUnit tests (uses JUnit Platform)

### Code Quality Commands

- `./gradlew codeCoverageReport` - Generate Jacoco coverage report (XML and HTML)
- `./gradlew sonar` - Run SonarQube analysis (depends on coverage report)

## Eclipse RCP Launch

The project includes launch configurations for running the Eclipse IDE with the Fennec editor:

- **Test Launch**: Use `org.eclipse.fennec.emf.editor.reflective.xmi.launch/testlaunch.bndrun` - Full Eclipse IDE with editor for testing
- **P2 Feature Definition**: `org.eclipse.fennec.emf.editor.reflective.xmi/editors.bndrun` - Defines the P2 feature for installation inside Eclipse
- **P2 Update Site Export**: `org.eclipse.fennec.emf.editor.reflective.xmi/p2updatesite.bndrun` - Provides everything necessary for BND to export the P2 update site

### Running in Eclipse

To launch the editor in a development environment:
1. Use `testlaunch.bndrun` to run full Eclipse IDE with the editor loaded
2. The editor registers itself for XMI content type (`org.eclipse.emf.ecore.xmi`)
3. Eclipse product: `org.eclipse.sdk.ide`
4. Eclipse application: `org.eclipse.ui.ide.workbench`

## Configuration Files

- `cnf/build.bnd` - Main BND workspace configuration with repositories and plugins
- `cnf/central.mvn` - Maven Central repository index 
- `*.bndrun` - OSGi launch configurations with required bundles
- `plugin.xml` - Eclipse plugin extension points for editor registration

## Dependencies

Key dependencies include:
- Eclipse EMF framework (ecore, edit, edit.ui)
- Eclipse Platform UI components (jface, ui.workbench)
- OSGi framework and services
- Gecko Eclipse compatibility bundles

## P2 Repository Generation

The build generates P2 update sites:
- `org.eclipse.fennec.emf.editor.reflective.xmi/generated/p2updatesite.zip` - P2 repository for distribution
- The `editors.bndrun` file defines the P2 feature that can be installed inside Eclipse
- The `p2updatesite.bndrun` file provides everything necessary for BND to export the P2 update site

## Testing

- Uses JUnit Platform for unit testing
- Gradle is configured for JUnit support in submodules  
- OSGi testing supported through BND tools
- Test artifacts generated in `generated/jacoco/test.exec`

## CI/CD

Jenkins pipeline configured in `Jenkinsfile`:
- Builds on `main` and `snapshot` branches
- Archives P2 update site artifacts
- Uses OpenJDK 17