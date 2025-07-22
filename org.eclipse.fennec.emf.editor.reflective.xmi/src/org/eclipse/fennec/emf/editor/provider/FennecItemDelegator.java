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

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
import org.eclipse.emf.edit.provider.IItemLabelProvider;

/**
 * Custom ItemDelegator for Fennec EMF Editor that provides enhanced text 
 * generation for EObjects, particularly for named elements.
 * 
 * @author Juergen Albert
 * @since 6 Jun 2025
 */
public class FennecItemDelegator extends AdapterFactoryItemDelegator {
    
    public FennecItemDelegator(AdapterFactory adapterFactory, ResourceLocator resourceLocator) {
        super(adapterFactory);
        // Store the resource locator for potential future use if needed
    }
    
    @Override
    public String getText(Object object) {
        System.out.println("Getting Text for " + object);
        
        if (object == null) {
            return "";
        }
        
        if (object instanceof EObject) {
            return getEObjectText((EObject) object);
        }
        
        return adapterFactory.adapt(object, IItemLabelProvider.class) != null ? 
            ((IItemLabelProvider) adapterFactory.adapt(object, IItemLabelProvider.class)).getText(object) : 
            object.toString();
    }
    
    /**
     * Generates enhanced text representation for EObjects.
     * For named elements, includes both name and URI.
     */
    private String getEObjectText(EObject eObject) {
        EClass eClass = eObject.eClass();
        if (EcorePackage.Literals.ENAMED_ELEMENT.isSuperTypeOf(eClass)) {
            String name = (String) eObject.eGet(EcorePackage.Literals.ENAMED_ELEMENT__NAME);
            return name + " - " + EcoreUtil.getURI(eObject);
        }
        return adapterFactory.adapt(eObject, IItemLabelProvider.class) != null ? 
            ((IItemLabelProvider) adapterFactory.adapt(eObject, IItemLabelProvider.class)).getText(eObject) : 
            eObject.toString();
    }
}