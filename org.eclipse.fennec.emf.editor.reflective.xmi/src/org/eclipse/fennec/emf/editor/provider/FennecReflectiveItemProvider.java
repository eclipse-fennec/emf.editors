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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ReflectiveItemProvider;
import org.eclipse.fennec.emf.editor.util.EcoreChoiceFilter;

/**
 * Enhanced ReflectiveItemProvider for Fennec EMF Editor that provides 
 * custom property descriptors with enhanced choice filtering and text generation.
 * 
 * @author Juergen Albert
 * @since 6 Jun 2025
 */
public class FennecReflectiveItemProvider extends ReflectiveItemProvider {
    
    public FennecReflectiveItemProvider(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }
    
    @Override
    public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
        itemPropertyDescriptors = new ArrayList<>();
        addPropertyDescriptors((EObject) object);
        return itemPropertyDescriptors;
    }
    
    /**
     * Adds property descriptors for all appropriate structural features of the EObject.
     */
    private void addPropertyDescriptors(EObject eObject) {
        for (EStructuralFeature feature : eObject.eClass().getEAllStructuralFeatures()) {
            if (shouldCreatePropertyDescriptor(feature)) {
                if (feature instanceof EReference) {
                    addReferencePropertyDescriptor(feature);
                } else {
                    addAttributePropertyDescriptor(feature);
                }
            }
        }
    }
    
    /**
     * Determines whether a property descriptor should be created for the given feature.
     * Excludes containment references.
     */
    private boolean shouldCreatePropertyDescriptor(EStructuralFeature feature) {
        return !(feature instanceof EReference) || !((EReference) feature).isContainment();
    }
    
    /**
     * Creates and adds a property descriptor for an EReference feature.
     */
    private void addReferencePropertyDescriptor(EStructuralFeature feature) {
        String propertyEditorFactory = getPropertyEditorFactory(feature);
        itemPropertyDescriptors.add(createReferencePropertyDescriptor(feature, propertyEditorFactory));
    }
    
    /**
     * Creates and adds a property descriptor for an EAttribute feature.
     */
    private void addAttributePropertyDescriptor(EStructuralFeature feature) {
        String propertyEditorFactory = getPropertyEditorFactory(feature);
        itemPropertyDescriptors.add(createAttributePropertyDescriptor(feature, propertyEditorFactory));
    }
    
    /**
     * Gets the property editor factory annotation for a feature.
     */
    private String getPropertyEditorFactory(EStructuralFeature feature) {
        String factory = EcoreUtil.getAnnotation(feature, EcoreUtil.GEN_MODEL_ANNOTATION_URI, "propertyEditorFactory");
        if (factory == null) {
            factory = EcoreUtil.getAnnotation(feature.getEType(), EcoreUtil.GEN_MODEL_ANNOTATION_URI, "propertyEditorFactory");
        }
        return factory;
    }
    
    /**
     * Creates a custom property descriptor for EReference features with enhanced choice filtering.
     */
    private IItemPropertyDescriptor createReferencePropertyDescriptor(EStructuralFeature feature, String propertyEditorFactory) {
        return new ItemPropertyDescriptor(
                ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
                getResourceLocator(), 
                getFeatureText(feature),
                getResourceLocator().getString("_UI_Property_description",
                        new Object[] { getFeatureText(feature), feature.getEType().getName() }),
                feature, 
                feature.isChangeable(), 
                false, 
                false,
                ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, 
                null, 
                null,
                propertyEditorFactory == null ? null : URI.createURI(propertyEditorFactory)) {
            
            {
                this.itemDelegator = new FennecItemDelegator(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), resourceLocator);
            }
            
            @Override
            public Collection<?> getChoiceOfValues(Object object) {
                Collection<Object> originalChoices = new ArrayList<>(super.getChoiceOfValues(object));
                return EcoreChoiceFilter.filterChoices(originalChoices);
            }
        };
    }
    
    /**
     * Creates a standard property descriptor for EAttribute features.
     */
    private IItemPropertyDescriptor createAttributePropertyDescriptor(EStructuralFeature feature, String propertyEditorFactory) {
        return new ItemPropertyDescriptor(
                ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
                getResourceLocator(), 
                getFeatureText(feature),
                getResourceLocator().getString("_UI_Property_description",
                        new Object[] { getFeatureText(feature), feature.getEType().getName() }),
                feature, 
                feature.isChangeable(), 
                false, 
                false,
                ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, 
                null, 
                null,
                propertyEditorFactory == null ? null : URI.createURI(propertyEditorFactory));
    }
}