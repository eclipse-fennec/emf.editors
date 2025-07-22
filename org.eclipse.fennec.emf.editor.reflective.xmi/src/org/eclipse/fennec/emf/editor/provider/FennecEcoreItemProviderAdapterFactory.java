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

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.provider.EAttributeItemProvider;
import org.eclipse.emf.ecore.provider.EReferenceItemProvider;
import org.eclipse.emf.ecore.provider.EcoreItemProviderAdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;

/**
 * Custom EcoreItemProviderAdapterFactory for Fennec EMF Editor that provides
 * enhanced item providers for EReference and EAttribute with custom type handling
 * and choice filtering.
 * 
 * @author Juergen Albert
 * @since 6 Jun 2025
 */
public class FennecEcoreItemProviderAdapterFactory extends EcoreItemProviderAdapterFactory {

    @Override
    public Adapter createEReferenceAdapter() {
        if (eReferenceItemProvider == null) {
            eReferenceItemProvider = new EReferenceItemProvider(this) {
                @Override
                public String getText(Object object) {
                    // Can be overridden to change the label for EReferences in the eClass editor
                    System.out.println("I'm called for - " + object);
                    return super.getText(object);
                }

                @Override
                protected void addETypePropertyDescriptor(Object object) {
                    itemPropertyDescriptors.add(createETypePropertyDescriptor());
                }
                
                private ItemPropertyDescriptorWithUniqueChoiceOfValueLabels createETypePropertyDescriptor() {
                    return new ItemPropertyDescriptorWithUniqueChoiceOfValueLabels(
                            ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
                            getResourceLocator(), 
                            getString("_UI_ETypedElement_eType_feature"),
                            getString("_UI_ETypedElement_eType_description"),
                            EcorePackage.Literals.ETYPED_ELEMENT__ETYPE, 
                            true, false, true, null, null, null) {
                        
                        @Override
                        public Collection<?> getChoiceOfValues(Object object) {
                            return getFilteredChoices(object);
                        }

                        @Override
                        public void setPropertyValue(Object object, Object value) {
                            handlePropertyValueSetting(object, value);
                        }
                    };
                }
                
                private Collection<?> getFilteredChoices(Object object) {
                    // Get all available choices from the editing domain
                    Collection<Object> result = new ArrayList<>();
                    
                    // Add all EClassifiers from loaded packages
                    EditingDomain domain = AdapterFactoryEditingDomain.getEditingDomainFor(object);
                    if (domain != null) {
                        for (Resource resource : domain.getResourceSet().getResources()) {
                            for (EObject rootObject : resource.getContents()) {
                                if (rootObject instanceof EPackage) {
                                    EPackage ePackage = (EPackage) rootObject;
                                    result.addAll(ePackage.getEClassifiers());
                                }
                            }
                        }
                    }

                    // Add Ecore classifiers for EReference
                    if (object instanceof EReference) {
                        for (Object classifier : EcorePackage.eINSTANCE.getEClassifiers()) {
                            if (!result.contains(classifier)) {
                                result.add(classifier);
                            }
                        }
                    }
                    
                    // Remove EClasses for EAttribute
                    if (object instanceof EAttribute) {
                        result.removeIf(item -> item instanceof EClass);
                    }

                    // Add type parameters
                    addTypeParameters(object, result);
                    
                    // Note: uniqueNameMap computation removed for simplification

                    return result;
                }
                
                private void addTypeParameters(Object object, Collection<Object> result) {
                    for (EObject eObject = (EObject) object; eObject != null; eObject = eObject.eContainer()) {
                        if (eObject instanceof EClassifier) {
                            result.addAll(((EClassifier) eObject).getETypeParameters());
                        } else if (eObject instanceof EOperation) {
                            result.addAll(((EOperation) eObject).getETypeParameters());
                        }
                    }
                }
                
                private void handlePropertyValueSetting(Object object, Object value) {
                    EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(object);
                    if (editingDomain == null) {
                        // Cannot call super.setPropertyValue with different signature
                        // Use the editing domain directly instead
                    } else {
                        EGenericType eGenericType = createGenericType(value);
                        editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, object,
                                EcorePackage.Literals.ETYPED_ELEMENT__EGENERIC_TYPE, eGenericType));
                    }
                }
                
                private EGenericType createGenericType(Object value) {
                    EGenericType eGenericType = null;
                    
                    if (value instanceof EClassifier) {
                        EClassifier eClassifier = (EClassifier) value;
                        eGenericType = EcoreFactory.eINSTANCE.createEGenericType();
                        eGenericType.setEClassifier(eClassifier);
                        
                        // Add empty type arguments for each type parameter
                        for (int i = 0, size = eClassifier.getETypeParameters().size(); i < size; ++i) {
                            eGenericType.getETypeArguments().add(EcoreFactory.eINSTANCE.createEGenericType());
                        }
                    } else if (value instanceof ETypeParameter) {
                        eGenericType = EcoreFactory.eINSTANCE.createEGenericType();
                        eGenericType.setETypeParameter((ETypeParameter) value);
                    }
                    
                    return eGenericType;
                }
            };
        }

        return eReferenceItemProvider;
    }

    @Override
    public Adapter createEAttributeAdapter() {
        if (eAttributeItemProvider == null) {
            eAttributeItemProvider = new EAttributeItemProvider(this) {
                @Override
                public String getText(Object object) {
                    // Can be overridden to change the label for EAttributes in the eClass editor
                    return super.getText(object);
                }
            };
        }

        return eAttributeItemProvider;
    }
}