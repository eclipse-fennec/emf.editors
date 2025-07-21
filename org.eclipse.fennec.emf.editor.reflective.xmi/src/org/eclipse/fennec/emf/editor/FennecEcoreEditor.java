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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.presentation.EcoreEditor;
import org.eclipse.emf.ecore.provider.EAttributeItemProvider;
import org.eclipse.emf.ecore.provider.EReferenceItemProvider;
import org.eclipse.emf.ecore.provider.EcoreItemProviderAdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ReflectiveItemProvider;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.provider.DiagnosticDecorator;
import org.eclipse.fennec.emf.editor.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.properties.PropertySheetPage;

/**
 * 
 * @author Juergen Albert
 * @since 6 Jun 2025
 */
public class FennecEcoreEditor extends EcoreEditor {

//	private static List<EClass> ePackageOjectToReach = new ArrayList<>();
//	
//	static {
//		ePackageOjectToReach.add(EcorePackage.Literals.EOBJECT);
//		ePackageOjectToReach.add(EcorePackage.Literals.ETYPED_ELEMENT);
//		ePackageOjectToReach.add(EcorePackage.Literals.ENAMED_ELEMENT);
//		ePackageOjectToReach.add(EcorePackage.Literals.EATTRIBUTE);
//		ePackageOjectToReach.add(EcorePackage.Literals.EREFERENCE);
//		ePackageOjectToReach.add(EcorePackage.Literals.ESTRUCTURAL_FEATURE);
//		ePackageOjectToReach.add(EcorePackage.Literals.ECLASS);
//		ePackageOjectToReach.add(EcorePackage.Literals.ECLASSIFIER);
//		ePackageOjectToReach.add(EcorePackage.Literals.EANNOTATION);
//		ePackageOjectToReach.add(EcorePackage.Literals.EOPERATION);
//		ePackageOjectToReach.add(EcorePackage.Literals.EDATA_TYPE);
//		ePackageOjectToReach.add(EcorePackage.Literals.EENUM);
//		ePackageOjectToReach.add(EcorePackage.Literals.EMODEL_ELEMENT);
//	}
	
	
	/**
	 * Creates a new instance.
	 */
	public FennecEcoreEditor() {
		super();
	}

	private static class MyReflectiveItemProviderAdapterFactory extends ReflectiveItemProviderAdapterFactory {
		public MyReflectiveItemProviderAdapterFactory() {
			reflectiveItemProviderAdapter = new ReflectiveItemProvider(this) {
				@Override
				public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
					// if (itemPropertyDescriptors == null)
					{
						itemPropertyDescriptors = new ArrayList<IItemPropertyDescriptor>();

						for (EStructuralFeature eFeature : ((EObject) object).eClass().getEAllStructuralFeatures()) {
							if (!(eFeature instanceof EReference) || !((EReference) eFeature).isContainment()) {
								String propertyEditorFactory = EcoreUtil.getAnnotation(eFeature,
										EcoreUtil.GEN_MODEL_ANNOTATION_URI, "propertyEditorFactory");
								if (propertyEditorFactory == null) {
									propertyEditorFactory = EcoreUtil.getAnnotation(eFeature.getEType(),
											EcoreUtil.GEN_MODEL_ANNOTATION_URI, "propertyEditorFactory");
								}
								if(eFeature instanceof EReference) {
									itemPropertyDescriptors.add(new ItemPropertyDescriptor(
											((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
											getResourceLocator(), getFeatureText(eFeature),
											getResourceLocator().getString("_UI_Property_description",
													new Object[] { getFeatureText(eFeature),
															eFeature.getEType().getName() }),
											eFeature, eFeature.isChangeable(), false, false,
											ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null,
											propertyEditorFactory == null ? null : URI.createURI(propertyEditorFactory)) {
										{
											this.itemDelegator = new ItemDelegator(adapterFactory, resourceLocator) {
												@Override
												public String getText(Object object) {
													System.out.println("Getting Text for " + object);
													if (object == null) {
														return "";
													}
													if (object instanceof EObject) {
														EObject eObject = (EObject) object;
														EClass eClass = eObject.eClass();
														if (EcorePackage.Literals.ENAMED_ELEMENT.isSuperTypeOf(eClass)) {
															return eObject.eGet(EcorePackage.Literals.ENAMED_ELEMENT__NAME)
																	+ " - " + EcoreUtil.getURI((EObject) object);
														}
													}
													// Can create labels here yourself.
													return super.getText(object);
												}
											};
										}
										
										@Override
										public Collection<?> getChoiceOfValues(Object object) {
											Collection<Object> result = new ArrayList<Object>(super.getChoiceOfValues(object));
											for (Iterator<Object> i = result.iterator(); i.hasNext();) {
												Object next = i.next();
												if (next instanceof EModelElement) {
													EModelElement element = (EModelElement) next;
													if(getEPackage(element) == EcorePackage.eINSTANCE) {
														i.remove();
													}
												}
											}
											return result;
										}
										
										EPackage getEPackage(EObject eObject) {
											if(eObject == null || eObject instanceof EPackage) {
												return (EPackage) eObject;
											} 
											return getEPackage(eObject.eContainer());
										}
									});
								} else {
									itemPropertyDescriptors.add(new ItemPropertyDescriptor(
											((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
											getResourceLocator(), getFeatureText(eFeature),
											getResourceLocator().getString("_UI_Property_description",
													new Object[] { getFeatureText(eFeature),
															eFeature.getEType().getName() }),
											eFeature, eFeature.isChangeable(), false, false,
											ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null,
											propertyEditorFactory == null ? null : URI.createURI(propertyEditorFactory)));
								}
							}
						}
					}
					return itemPropertyDescriptors;
				}
			};
		}
	}

	private static class MyEcoreItemProviderAdapterFactory extends EcoreItemProviderAdapterFactory {

		@Override
		public Adapter createEReferenceAdapter() {
			if (eReferenceItemProvider == null) {
				eReferenceItemProvider = new EReferenceItemProvider(this) {
					@Override
					public String getText(Object object) {
						// We can overwrite this, if we want to change the label EAttributes in the
						// eClass editor (Current is myAttribute : EString)
						System.out.println("I'm called for - " + object);
						return super.getText(object);
					}

					@Override
					protected void addETypePropertyDescriptor(Object object) {
						itemPropertyDescriptors.add(new ItemPropertyDescriptorWithUniqueChoiceOfValueLabels(
								((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
								getResourceLocator(), getString("_UI_ETypedElement_eType_feature"),
								getString("_UI_ETypedElement_eType_description"),
								EcorePackage.Literals.ETYPED_ELEMENT__ETYPE, true, false, true, null, null, null) {
							@Override
							public Collection<?> getChoiceOfValues(Object object) {
								// Filter out types that aren't permitted.
								//
								Collection<Object> result = new ArrayList<Object>(super.getChoiceOfValues(object));

								if (object instanceof EReference) {
									for (Object classifier : EcorePackage.eINSTANCE.getEClassifiers()) {
										if (!result.contains(classifier)) {
											result.add(classifier);
										}
									}
								}
								
								if (object instanceof EAttribute) {
									for (Iterator<Object> i = result.iterator(); i.hasNext();) {
										if (i.next() instanceof EClass) {
											i.remove();
										}
									}
								}

								// Let them choose type parameters as well.
								//
								for (EObject eObject = (EObject) object; eObject != null; eObject = eObject
										.eContainer()) {
									if (eObject instanceof EClassifier) {
										result.addAll(((EClassifier) eObject).getETypeParameters());
									} else if (eObject instanceof EOperation) {
										result.addAll(((EOperation) eObject).getETypeParameters());
									}
								}

								uniqueNameMap = computeUniqueLabels(object, result);

								return result;
							}

							@Override
							public void setPropertyValue(Object object, Object value) {
								EditingDomain editingDomain = getEditingDomain(object);
								if (editingDomain == null) {
									super.setPropertyValue(object, value);
								} else {

									EGenericType eGenericType = null;
									if (value instanceof EClassifier) {
										EClassifier eClassifier = (EClassifier) value;
										eGenericType = EcoreFactory.eINSTANCE.createEGenericType();
										eGenericType.setEClassifier(eClassifier);
										for (int i = 0, size = eClassifier.getETypeParameters().size(); i < size; ++i) {
											eGenericType.getETypeArguments()
													.add(EcoreFactory.eINSTANCE.createEGenericType());
										}
									} else if (value instanceof ETypeParameter) {
										eGenericType = EcoreFactory.eINSTANCE.createEGenericType();
										eGenericType.setETypeParameter((ETypeParameter) value);
									}
									editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, object,
											EcorePackage.Literals.ETYPED_ELEMENT__EGENERIC_TYPE, eGenericType));
								}
							}
						});
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
						// We can overwrite this, if we want to change the label References in the
						// eClass editor (Current is myERefName : MyTestClass)
						return super.getText(object);
					}
				};
			}

			return eAttributeItemProvider;
		}
	}

	@Override
	protected void initializeEditingDomain() {
		// Create an adapter factory that yields item providers.
		//
		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		ecoreItemProviderAdapterFactory = new MyEcoreItemProviderAdapterFactory();
		adapterFactory.addAdapterFactory(ecoreItemProviderAdapterFactory);
		adapterFactory.addAdapterFactory(new MyReflectiveItemProviderAdapterFactory());

		// Create the command stack that will notify this editor as commands are
		// executed.
		//
		BasicCommandStack commandStack = new BasicCommandStack() {
			@Override
			public void execute(Command command) {
				if (!(command instanceof AbstractCommand.NonDirtying)) {
					DiagnosticDecorator.cancel(editingDomain);
				}
				super.execute(command);
			}
		};

		// Add a listener to set the most recent command's affected objects to be the
		// selection of the viewer with focus.
		//
		commandStack.addCommandStackListener(new CommandStackListener() {
			public void commandStackChanged(final EventObject event) {
				getContainer().getDisplay().asyncExec(new Runnable() {
					public void run() {
						firePropertyChange(IEditorPart.PROP_DIRTY);

						// Try to select the affected objects.
						//
						Command mostRecentCommand = ((CommandStack) event.getSource()).getMostRecentCommand();
						if (mostRecentCommand != null) {
							setSelectionToViewer(mostRecentCommand.getAffectedObjects());
						}
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

		// Create the editing domain with a special command stack.
		//
		editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack) {
			{
				resourceToReadOnlyMap = new HashMap<Resource, Boolean>();
			}

			@Override
			public boolean isReadOnly(Resource resource) {
				if (super.isReadOnly(resource) || resource == null) {
					return true;
				} else {
					URI uri = resource.getURI();
					boolean result = "java".equals(uri.scheme()) || "xcore".equals(uri.fileExtension())
							|| "xcoreiq".equals(uri.fileExtension()) || "oclinecore".equals(uri.fileExtension())
							|| "genmodel".equals(uri.fileExtension())
							|| uri.isPlatformResource()
									&& !resourceSet.getURIConverter().normalize(uri).isPlatformResource()
							|| uri.isPlatformPlugin();
					if (resourceToReadOnlyMap != null) {
						resourceToReadOnlyMap.put(resource, result);
					}
					return result;
				}
			}
		};
	}

}
