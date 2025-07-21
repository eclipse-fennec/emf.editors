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
package org.eclipse.fennec.emf.editor.provider.resource;

import java.util.Collection;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.ReflectiveItemProvider;
import org.eclipse.fennec.emf.editor.command.ResourceRemoveCommand;

/**
 * This is an item provider for EObjects that are direct children of a Resource.
 * It extends the reflective item provider to add resource-specific commands
 * like removing the EObject from the resource.
 */
public class ResourceChildItemProvider extends ReflectiveItemProvider implements IEditingDomainItemProvider {

    /**
     * This constructs an instance from a factory and a notifier.
     */
    public ResourceChildItemProvider(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    @Override
    public Command createCommand(Object object, EditingDomain domain, Class<? extends Command> commandClass,
            CommandParameter commandParameter) {
        
        // Handle remove command for objects that are direct children of a resource
        if (commandClass == RemoveCommand.class && object instanceof EObject) {
            EObject eObject = (EObject) object;
            Resource resource = eObject.eResource();
            
            // Only handle if this object is a direct child of the resource
            if (resource != null && resource.getContents().contains(eObject)) {
                Collection<?> collection = commandParameter.getCollection();
                if (collection != null && !collection.isEmpty()) {
                    // Create ResourceRemoveCommand for multiple objects
                    @SuppressWarnings("unchecked")
                    Collection<EObject> eObjects = (Collection<EObject>) collection;
                    return new ResourceRemoveCommand(domain, 
                        "Remove from Resource", 
                        "Remove selected objects from the resource", 
                        resource, eObjects);
                } else {
                    // Create ResourceRemoveCommand for single object
                    return new ResourceRemoveCommand(domain, resource, eObject);
                }
            }
        }
        
        // Delegate to parent for other commands
        return super.createCommand(object, domain, commandClass, commandParameter);
    }
}