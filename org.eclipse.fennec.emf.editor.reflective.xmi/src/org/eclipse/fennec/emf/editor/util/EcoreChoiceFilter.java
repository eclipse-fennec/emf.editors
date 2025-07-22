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
package org.eclipse.fennec.emf.editor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;

/**
 * Utility class for filtering choice collections in property descriptors.
 * Provides methods to filter out unwanted elements from choice lists.
 * 
 * @author Juergen Albert
 * @since 6 Jun 2025
 */
public class EcoreChoiceFilter {
    
    /**
     * Filters a collection of choices by removing elements that belong to the Ecore package.
     * 
     * @param originalChoices the original collection of choices
     * @return filtered collection with Ecore package elements removed
     */
    public static Collection<Object> filterChoices(Collection<Object> originalChoices) {
        Collection<Object> result = new ArrayList<>(originalChoices);
        
        for (Iterator<Object> i = result.iterator(); i.hasNext();) {
            Object next = i.next();
            if (shouldRemoveFromChoices(next)) {
                i.remove();
            }
        }
        
        return result;
    }
    
    /**
     * Determines if an object should be removed from choice collections.
     * Elements belonging to the EcorePackage are typically removed.
     */
    private static boolean shouldRemoveFromChoices(Object choice) {
        if (choice instanceof EModelElement) {
            EModelElement element = (EModelElement) choice;
            return getEPackage(element) == EcorePackage.eINSTANCE;
        }
        return false;
    }
    
    /**
     * Recursively finds the EPackage that contains the given EObject.
     * 
     * @param eObject the object to find the package for
     * @return the containing EPackage, or null if not found
     */
    public static EPackage getEPackage(EObject eObject) {
        if (eObject == null || eObject instanceof EPackage) {
            return (EPackage) eObject;
        }
        return getEPackage(eObject.eContainer());
    }
}