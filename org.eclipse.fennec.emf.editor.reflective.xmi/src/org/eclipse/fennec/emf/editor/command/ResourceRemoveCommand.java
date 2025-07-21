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
package org.eclipse.fennec.emf.editor.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.EMFEditPlugin;
import org.eclipse.emf.edit.command.AbstractOverrideableCommand;
import org.eclipse.emf.edit.command.CommandActionDelegate;
import org.eclipse.emf.edit.domain.EditingDomain;

/**
 * The remove command logically acts upon EObjects that are direct children of a Resource
 * to remove them from the resource's contents.
 * 
 * <p>
 * The implementation of this class is low-level and EMF specific;
 * it allows one or more objects to be removed from a resource's contents,
 * i.e., it is equivalent of the call
 * <pre>
 *   resource.getContents().removeAll(collection);
 * </pre>
 * 
 * <p>
 * Like all the low-level commands in this package, the remove command is undoable.
 */
public class ResourceRemoveCommand extends AbstractOverrideableCommand implements CommandActionDelegate {

    /**
     * This is the resource from which the objects will be removed.
     */
    protected Resource resource;

    /**
     * This is the list from which the command will remove the collection.
     */
    protected EList<EObject> ownerList;

    /**
     * This is the collection of objects being removed from the resource.
     */
    protected Collection<EObject> collection;

    /**
     * This records the indices of the objects being removed so they can be restored during undo.
     */
    protected List<Integer> indices;

    /**
     * This is the value returned by {@link Command#getAffectedObjects}. 
     * The affected objects are different after an execute than after an undo, so we record it.
     */
    protected Collection<?> affectedObjects;

    /**
     * This constructs a command to remove a particular EObject from the resource.
     */
    public ResourceRemoveCommand(EditingDomain domain, Resource resource, EObject value) {
        this(domain, value.eClass().getName(), "Remove " + value.eClass().getName() + " from resource", 
             resource, Collections.singleton(value));
    }

    /**
     * This constructs a command to remove a collection of EObjects from the resource.
     */
    public ResourceRemoveCommand(EditingDomain domain, String label, String description, 
                                Resource resource, Collection<EObject> collection) {
        super(domain, label, description);
        this.resource = resource;
        this.collection = collection;
        this.ownerList = resource.getContents();
        this.indices = new ArrayList<>();
    }

    /**
     * This returns the resource from which objects will be removed.
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * This returns the collection of objects being removed.
     */
    public Collection<?> getCollection() {
        return collection;
    }

    @Override
    protected boolean prepare() {
        // If there is no resource, no collection or an empty collection to remove...
        if (resource == null || collection == null || collection.isEmpty()) {
            return false;
        }

        // Check that all objects in the collection are actually in the resource's contents
        for (EObject eObject : collection) {
            if (!ownerList.contains(eObject)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void doExecute() {
        // Record the indices of the objects being removed for undo
        indices.clear();
        for (EObject eObject : collection) {
            int index = ownerList.indexOf(eObject);
            if (index >= 0) {
                indices.add(index);
            }
        }

        // Remove the objects from the resource
        ownerList.removeAll(collection);

        // We'd like the resource to be selected after this command completes
        affectedObjects = Collections.singleton(resource);
    }

    @Override
    public void doUndo() {
        // Restore the objects to their original positions
        List<EObject> objectsList = new ArrayList<>(collection);
        for (int i = 0; i < objectsList.size() && i < indices.size(); i++) {
            EObject eObject = objectsList.get(i);
            int originalIndex = indices.get(i);
            // Make sure we don't go beyond the current list size
            int insertIndex = Math.min(originalIndex, ownerList.size());
            ownerList.add(insertIndex, eObject);
        }

        // We'd like the restored objects to be selected after undo completes
        affectedObjects = collection;
    }

    @Override
    public void doRedo() {
        // Simply remove the collection from the list again
        ownerList.removeAll(collection);

        // We'd like the resource to be selected after this command completes
        affectedObjects = Collections.singleton(resource);
    }

    @Override
    public Collection<?> doGetResult() {
        return collection;
    }

    @Override
    public Collection<?> doGetAffectedObjects() {
        return affectedObjects;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(super.toString());
        result.append(" (resource: " + resource + ")");
        result.append(" (collection: " + collection + ")");
        result.append(" (indices: " + indices + ")");
        result.append(" (affectedObjects: " + affectedObjects + ")");
        return result.toString();
    }

    @Override
    public Object getImage() {
        return EMFEditPlugin.INSTANCE.getImage("full/ctool16/Delete");
    }

    @Override
    public String getText() {
        return getLabel();
    }

    @Override
    public String getToolTipText() {
        return getDescription();
    }
}