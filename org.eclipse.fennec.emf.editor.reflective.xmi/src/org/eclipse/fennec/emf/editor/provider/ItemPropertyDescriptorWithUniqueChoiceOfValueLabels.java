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
package org.eclipse.fennec.emf.editor.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;

/**
 * Extended ItemPropertyDescriptor that provides unique labels for choice values.
 * This helps disambiguate items that might have the same display name.
 * 
 * @author Juergen Albert
 * @since 6 Jun 2025
 */
public abstract class ItemPropertyDescriptorWithUniqueChoiceOfValueLabels extends ItemPropertyDescriptor {
    
    protected Map<Object, String> uniqueNameMap;
    
    public ItemPropertyDescriptorWithUniqueChoiceOfValueLabels(
            AdapterFactory adapterFactory,
            ResourceLocator resourceLocator,
            String displayName,
            String description,
            EStructuralFeature feature,
            boolean isSettable,
            boolean multiLine,
            boolean sortChoices,
            Object staticImage,
            String category,
            String[] filterFlags) {
        super(adapterFactory, resourceLocator, displayName, description, feature, isSettable, multiLine, sortChoices, staticImage, category, filterFlags);
        this.uniqueNameMap = new HashMap<>();
    }
    
    public ItemPropertyDescriptorWithUniqueChoiceOfValueLabels(
            AdapterFactory adapterFactory,
            ResourceLocator resourceLocator,
            String displayName,
            String description,
            EStructuralFeature feature,
            boolean isSettable,
            boolean multiLine,
            boolean sortChoices,
            Object staticImage,
            String category,
            String[] filterFlags,
            URI propertyEditorFactory) {
        super(adapterFactory, resourceLocator, displayName, description, feature, isSettable, multiLine, sortChoices, staticImage, category, filterFlags, propertyEditorFactory);
        this.uniqueNameMap = new HashMap<>();
    }
    
    /**
     * Computes unique labels for objects in the choice collection to avoid ambiguity.
     * 
     * @param object the object being edited
     * @param choices the collection of available choices
     * @return map of objects to their unique labels
     */
    protected Map<Object, String> computeUniqueLabels(Object object, Collection<Object> choices) {
        Map<Object, String> labelMap = new HashMap<>();
        
        // For now, just use the default text representation
        // This can be enhanced to provide more sophisticated unique labeling
        for (Object choice : choices) {
            String label = choice.toString();
            labelMap.put(choice, label);
        }
        
        return labelMap;
    }
}