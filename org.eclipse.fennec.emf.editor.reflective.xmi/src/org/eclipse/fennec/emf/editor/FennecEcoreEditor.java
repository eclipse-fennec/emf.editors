/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.emf.editor;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.presentation.EcoreEditor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.provider.DiagnosticDecorator;
import org.eclipse.fennec.emf.editor.provider.FennecEcoreItemProviderAdapterFactory;
import org.eclipse.fennec.emf.editor.provider.FennecReflectiveItemProviderAdapterFactory;
import org.eclipse.fennec.emf.editor.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.properties.PropertySheetPage;

/**
 * Enhanced EMF Ecore Editor for the Fennec EMF Editor framework.
 * 
 * <p>This editor extends the standard Eclipse EMF {@link EcoreEditor} to provide
 * enhanced editing capabilities for EMF models through custom item providers and
 * improved property descriptors. The editor supports both reflective editing
 * (for any EMF model) and specialized Ecore model editing with advanced features.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Enhanced Property Descriptors:</strong> Custom property descriptors with
 *       improved choice filtering and text generation</li>
 *   <li><strong>Reflective Editing:</strong> Dynamic property descriptors that adapt to
 *       any EMF model structure without code generation</li>
 *   <li><strong>Resource Management:</strong> Enhanced support for adding/editing EObjects
 *       at the resource level</li>
 *   <li><strong>Choice Filtering:</strong> Intelligent filtering of property choices to
 *       hide inappropriate options (e.g., EcorePackage elements)</li>
 *   <li><strong>Generic Type Support:</strong> Advanced handling of EGenericType and
 *       type parameters in Ecore models</li>
 *   <li><strong>Custom Text Generation:</strong> Enhanced text labels for better object
 *       identification in property sheets and trees</li>
 * </ul>
 * 
 * <h3>Architecture:</h3>
 * <p>The editor uses a modular architecture with specialized adapter factories:</p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.emf.editor.provider.resource.ResourceItemProviderAdapterFactory} 
 *       - Handles Resource-level operations</li>
 *   <li>{@link FennecEcoreItemProviderAdapterFactory} - Enhanced Ecore model editing</li>
 *   <li>{@link FennecReflectiveItemProviderAdapterFactory} - Reflective editing for any EMF model</li>
 * </ul>
 * 
 * <h3>Usage:</h3>
 * <p>This editor is typically registered as the default editor for XMI files containing
 * EMF models. It automatically detects the model structure and provides appropriate
 * editing capabilities through the Properties view and context menus.</p>
 * 
 * <h3>Editing Domain Configuration:</h3>
 * <p>The editor configures a specialized editing domain with:</p>
 * <ul>
 *   <li>Custom command stack with diagnostic cancellation</li>
 *   <li>Property sheet synchronization</li>
 *   <li>Read-only resource detection for various file types</li>
 *   <li>Automatic selection propagation after command execution</li>
 * </ul>
 * 
 * @author Juergen Albert
 * @since 6 Jun 2025
 * @see EcoreEditor
 * @see org.eclipse.fennec.emf.editor.provider.FennecEcoreItemProviderAdapterFactory
 * @see org.eclipse.fennec.emf.editor.provider.FennecReflectiveItemProviderAdapterFactory
 */
public class FennecEcoreEditor extends EcoreEditor {

	/**
	 * Creates a new instance of the Fennec Ecore Editor.
	 * 
	 * <p>The editor will be initialized with enhanced adapter factories that provide
	 * improved editing capabilities for EMF models.</p>
	 */
	public FennecEcoreEditor() {
		super();
	}


	/**
	 * Initializes the editing domain with enhanced adapter factories and command stack.
	 * 
	 * <p>This method sets up the core editing infrastructure including:</p>
	 * <ul>
	 *   <li>Custom adapter factories for enhanced item providers</li>
	 *   <li>Command stack with diagnostic handling and property sheet synchronization</li>
	 *   <li>Editing domain with specialized read-only resource detection</li>
	 * </ul>
	 * 
	 * <p>The initialization order is important as the editing domain depends on both
	 * the adapter factory and command stack being properly configured.</p>
	 */
	@Override
	protected void initializeEditingDomain() {
		createAdapterFactory();
		BasicCommandStack commandStack = createCommandStack();
		createEditingDomain(commandStack);
	}
	
	/**
	 * Creates and configures the composed adapter factory with all necessary providers.
	 * 
	 * <p>The adapter factory is composed of three specialized factories in order:</p>
	 * <ol>
	 *   <li>{@link ResourceItemProviderAdapterFactory} - Handles Resource objects and 
	 *       provides context menu support for adding EObjects to resources</li>
	 *   <li>{@link FennecEcoreItemProviderAdapterFactory} - Enhanced item providers for 
	 *       Ecore model elements with advanced type handling and choice filtering</li>
	 *   <li>{@link FennecReflectiveItemProviderAdapterFactory} - Reflective item providers 
	 *       that work with any EMF model without requiring generated code</li>
	 * </ol>
	 * 
	 * <p>This layered approach ensures that specialized providers take precedence while
	 * maintaining broad compatibility with any EMF model structure.</p>
	 */
	private void createAdapterFactory() {
		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
		
		// Add resource-level support first
		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		
		// Add Ecore-specific enhancements
		ecoreItemProviderAdapterFactory = new FennecEcoreItemProviderAdapterFactory();
		adapterFactory.addAdapterFactory(ecoreItemProviderAdapterFactory);
		
		// Add reflective support as fallback for any EMF model
		adapterFactory.addAdapterFactory(new FennecReflectiveItemProviderAdapterFactory());
	}
	
	/**
	 * Creates the command stack with enhanced command execution handling and UI synchronization.
	 * 
	 * <p>The command stack provides the following enhancements over the standard implementation:</p>
	 * <ul>
	 *   <li><strong>Diagnostic Cancellation:</strong> Automatically cancels diagnostic decorations
	 *       when executing commands that modify the model</li>
	 *   <li><strong>Automatic Selection:</strong> Propagates the affected objects from executed
	 *       commands to the active viewer for better user experience</li>
	 *   <li><strong>Property Sheet Sync:</strong> Ensures property sheets are refreshed after
	 *       command execution and removes disposed property sheet pages</li>
	 *   <li><strong>Dirty State Management:</strong> Properly fires property change events to
	 *       update the editor's dirty state in the UI</li>
	 * </ul>
	 * 
	 * @return configured BasicCommandStack with enhanced listeners
	 */
	private BasicCommandStack createCommandStack() {
		BasicCommandStack commandStack = new BasicCommandStack() {
			@Override
			public void execute(Command command) {
				// Cancel any existing diagnostic decorations before executing commands
				// that might modify the model (non-dirtying commands don't change the model)
				if (!(command instanceof AbstractCommand.NonDirtying)) {
					DiagnosticDecorator.cancel(editingDomain);
				}
				super.execute(command);
			}
		};

		// Add listener for UI synchronization after command execution
		commandStack.addCommandStackListener(new CommandStackListener() {
			public void commandStackChanged(final EventObject event) {
				// Use asyncExec to ensure UI updates happen on the UI thread
				getContainer().getDisplay().asyncExec(new Runnable() {
					public void run() {
						// Update the editor's dirty state
						firePropertyChange(IEditorPart.PROP_DIRTY);

						// Automatically select the objects affected by the command
						// This provides better user feedback about what was changed
						Command mostRecentCommand = ((CommandStack) event.getSource()).getMostRecentCommand();
						if (mostRecentCommand != null) {
							setSelectionToViewer(mostRecentCommand.getAffectedObjects());
						}
						
						// Refresh all active property sheet pages and clean up disposed ones
						for (Iterator<PropertySheetPage> i = propertySheetPages.iterator(); i.hasNext();) {
							PropertySheetPage propertySheetPage = i.next();
							if (propertySheetPage.getControl() == null || propertySheetPage.getControl().isDisposed()) {
								i.remove();
							} else {
								propertySheetPage.refresh();
							}
						}
					}
				});
			}
		});
		
		return commandStack;
	}
	
	/**
	 * Creates the editing domain with specialized read-only resource detection.
	 * 
	 * <p>The editing domain is configured with enhanced read-only detection that automatically
	 * marks certain resource types as read-only to prevent accidental modification:</p>
	 * 
	 * <h4>Read-Only Resource Types:</h4>
	 * <ul>
	 *   <li><strong>Java scheme resources:</strong> {@code java://*} URIs</li>
	 *   <li><strong>Xcore files:</strong> {@code *.xcore}, {@code *.xcoreiq}</li>
	 *   <li><strong>OCL files:</strong> {@code *.oclinecore}</li>
	 *   <li><strong>GenModel files:</strong> {@code *.genmodel}</li>
	 *   <li><strong>Platform plugin resources:</strong> Resources from installed plugins</li>
	 *   <li><strong>Normalized platform resources:</strong> Resources that normalize to 
	 *       non-platform locations (indicating they're external)</li>
	 * </ul>
	 * 
	 * <p>This protection helps prevent users from accidentally modifying generated or
	 * system-provided model files that shouldn't be edited directly.</p>
	 * 
	 * @param commandStack the configured command stack for the editing domain
	 */
	private void createEditingDomain(BasicCommandStack commandStack) {
		editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack) {
			{
				// Initialize the read-only map for caching resource read-only status
				resourceToReadOnlyMap = new HashMap<Resource, Boolean>();
			}

			@Override
			public boolean isReadOnly(Resource resource) {
				// Delegate to parent for null resources or already read-only resources
				if (super.isReadOnly(resource) || resource == null) {
					return true;
				} else {
					URI uri = resource.getURI();
					
					// Determine read-only status based on URI characteristics
					boolean result = "java".equals(uri.scheme()) 
							|| "xcore".equals(uri.fileExtension())
							|| "xcoreiq".equals(uri.fileExtension()) 
							|| "oclinecore".equals(uri.fileExtension())
							|| "genmodel".equals(uri.fileExtension())
							|| uri.isPlatformResource() && !resourceSet.getURIConverter().normalize(uri).isPlatformResource()
							|| uri.isPlatformPlugin();
					
					// Cache the result for performance
					if (resourceToReadOnlyMap != null) {
						resourceToReadOnlyMap.put(resource, result);
					}
					return result;
				}
			}
		};
	}

}
