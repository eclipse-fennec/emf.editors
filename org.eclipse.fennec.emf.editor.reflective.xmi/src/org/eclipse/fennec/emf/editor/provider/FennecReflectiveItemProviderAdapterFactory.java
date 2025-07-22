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

import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;

/**
 * Custom ReflectiveItemProviderAdapterFactory for Fennec EMF Editor
 * that creates enhanced reflective item providers with custom property descriptors
 * and choice filtering capabilities.
 * 
 * @author Juergen Albert
 * @since 6 Jun 2025
 */
public class FennecReflectiveItemProviderAdapterFactory extends ReflectiveItemProviderAdapterFactory {
    
    public FennecReflectiveItemProviderAdapterFactory() {
        super();
        // Replace the default reflective item provider with our enhanced version
        reflectiveItemProviderAdapter = new FennecReflectiveItemProvider(this);
    }
}