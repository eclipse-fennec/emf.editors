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
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.provider.DiagnosticDecorator;
import org.eclipse.fennec.emf.editor.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.emf.ecore.provider.EcoreItemProviderAdapterFactory;

/**
 * 
 * @author Juergen Albert
 * @since 6 Jun 2025
 */
public class FennecEcoreEditor extends EcoreEditor{

	/**
	 * Creates a new instance.
	 */
	public FennecEcoreEditor() {
		super();
	}
	
	
	@Override
	protected void initializeEditingDomain() {
		 // Create an adapter factory that yields item providers.
	    //
	    adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

	    adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
	    ecoreItemProviderAdapterFactory = new EcoreItemProviderAdapterFactory();
	    adapterFactory.addAdapterFactory(ecoreItemProviderAdapterFactory);
	    adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

	    // Create the command stack that will notify this editor as commands are executed.
	    //
	    BasicCommandStack commandStack = new BasicCommandStack()
	      {
	        @Override
	        public void execute(Command command)
	        {
	          if (!(command instanceof AbstractCommand.NonDirtying))
	          {
	            DiagnosticDecorator.cancel(editingDomain);
	          }
	          super.execute(command);
	        }
	      };

	    // Add a listener to set the most recent command's affected objects to be the selection of the viewer with focus.
	    //
	    commandStack.addCommandStackListener
	      (new CommandStackListener()
	       {
	         public void commandStackChanged(final EventObject event)
	         {
	           getContainer().getDisplay().asyncExec
	             (new Runnable()
	              {
	                public void run()
	                {
	                  firePropertyChange(IEditorPart.PROP_DIRTY);

	                  // Try to select the affected objects.
	                  //
	                  Command mostRecentCommand = ((CommandStack)event.getSource()).getMostRecentCommand();
	                  if (mostRecentCommand != null)
	                  {
	                    setSelectionToViewer(mostRecentCommand.getAffectedObjects());
	                  }
	                  for (Iterator<PropertySheetPage> i = propertySheetPages.iterator(); i.hasNext(); )
	                  {
	                    PropertySheetPage propertySheetPage = i.next();
	                    if (propertySheetPage.getControl() == null || propertySheetPage.getControl().isDisposed())
	                    {
	                      i.remove();
	                    }
	                    else
	                    {
	                      propertySheetPage.refresh();
	                    }
	                  }
	                }
	              });
	         }
	       });

	    // Create the editing domain with a special command stack.
	    //
	    editingDomain =
	      new AdapterFactoryEditingDomain(adapterFactory, commandStack)
	      {
	        {
	          resourceToReadOnlyMap = new HashMap<Resource, Boolean>();
	        }
	        @Override
	        public boolean isReadOnly(Resource resource)
	        {
	          if (super.isReadOnly(resource) || resource == null)
	          {
	            return true;
	          }
	          else
	          {
	            URI uri = resource.getURI();
	            boolean result =
	                "java".equals(uri.scheme()) ||
	                 "xcore".equals(uri.fileExtension()) ||
	                 "xcoreiq".equals(uri.fileExtension()) ||
	                 "oclinecore".equals(uri.fileExtension()) ||
	                 "genmodel".equals(uri.fileExtension()) ||
	                 uri.isPlatformResource() && !resourceSet.getURIConverter().normalize(uri).isPlatformResource() ||
	                 uri.isPlatformPlugin();
	            if (resourceToReadOnlyMap != null)
	            {
	              resourceToReadOnlyMap.put(resource, result);
	            }
	            return result;
	          }
	        }
	      };
	}
	
}
