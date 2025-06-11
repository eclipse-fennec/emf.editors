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
 *     IBM - Initial API and implementation
 *     Data In Motion - override to enable modifications
 */
package org.eclipse.fennec.emf.editor.provider.resource;


import java.util.Collection;
import java.util.MissingResourceException;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.EMFEditPlugin;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor.OverrideableCommandOwner;
import org.eclipse.emf.edit.provider.IItemPropertySource;


/**
 * This implementation of an item property descriptor supports delegating of the {@link IItemPropertySource} interface
 * to the {@link IItemPropertyDescriptor} interface.
 */
public class ResourcePropertyDescriptor implements IItemPropertyDescriptor, OverrideableCommandOwner
{
  /**
   * Returns the feature's default {@link #getId(Object) identifier}.
   * @param eStructuralFeature the feature to lookup.
   * @return the feature's default identifier.
   */
  public static String getDefaultId(EStructuralFeature eStructuralFeature)
  {
    return eStructuralFeature.getName();
  }

  public static final Object BOOLEAN_VALUE_IMAGE = EMFEditPlugin.INSTANCE.getImage("full/obj16/BooleanValue");
  public static final Object GENERIC_VALUE_IMAGE = EMFEditPlugin.INSTANCE.getImage("full/obj16/GenericValue");
  public static final Object INTEGRAL_VALUE_IMAGE = EMFEditPlugin.INSTANCE.getImage("full/obj16/IntegralValue");
  public static final Object REAL_VALUE_IMAGE = EMFEditPlugin.INSTANCE.getImage("full/obj16/RealValue");
  public static final Object TEXT_VALUE_IMAGE = EMFEditPlugin.INSTANCE.getImage("full/obj16/TextValue");

  /**
   * @since 2.14
   */
  public static final Object NO_VALUE_IMAGE = EMFEditPlugin.INSTANCE.getImage("full/obj16/NoValue");

  /**
   * For now we need to keep track of the adapter factory, because we need it to provide a correct label provider.
   */
  protected AdapterFactory adapterFactory;

  /**
   * This is used to locate resources for translated values like enumeration literals.
   */
  protected ResourceLocator resourceLocator;

  /**
   * This is a convenient wrapper of the {@link #adapterFactory}.
   */
  protected AdapterFactoryItemDelegator itemDelegator;

  /**
   * This is returned by {@link #canSetProperty}.
   */
  protected boolean isSettable;

  /**
   * This is the name that is displayed in the property sheet.
   */
  protected String displayName;

  /**
   * This is the description shown in the property sheet when this property is selected.
   */
  protected String description;

  /**
   * Whether the value of this property consists of multi-line text.
   * @since 2.2.0
   */
  protected boolean multiLine;

  /**
   * Whether the choices for this property should be sorted for display.
   * @since 2.2.0
   */
  protected boolean sortChoices;

  /**
   * This represents the group of properties into which this one should be placed.
   */
  protected String category;

  /**
   * These are the flags used as filters in the property sheet.
   */
  protected String [] filterFlags;

  /**
   * This is the label provider used to render property values.
   */
  // protected Object labelProvider;

  /**
   * This is the image that will be used with the value no matter what type of object it is.
   */
  protected Object staticImage;

  /**
   * If non-null, this object will be the owner of commands created to set the property's value. 
   */
  protected Object commandOwner;

  /**
   * @since 2.14
   */
  protected Object editorFactory;

  /**
   * This class uses a static image
   */
  protected class ItemDelegator extends AdapterFactoryItemDelegator
  {
    protected ResourceLocator resourceLocator;

    public ItemDelegator(AdapterFactory adapterFactory)
    {
      super(adapterFactory);
    }

    public ItemDelegator(AdapterFactory adapterFactory, ResourceLocator resourceLocator)
    {
      super(adapterFactory);
      this.resourceLocator = resourceLocator;
    }

    protected String convert(EDataType eDataType, Object value)
    {
      if (resourceLocator != null)
      {
        if (eDataType instanceof EEnum)
        {
          try
          {
            return 
              resourceLocator.getString
                ("_UI_" + eDataType.getName() + "_" + ((Enumerator)value).getName() + "_literal");
          }
          catch (MissingResourceException exception)
          {
            // Ignore
          }
        }
        else if (value instanceof Boolean)
        {
          try
          {
            return 
              resourceLocator.getString
                (Boolean.TRUE.equals(value) ? "_UI_Boolean_true_literal" : "_UI_Boolean_false_literal");
          }
          catch (MissingResourceException exception)
          {
            // Ignore
          }
        } 
      }
      return crop(EcoreUtil.convertToString(eDataType, value));
    }

    // This is copied from ItemProviderAdapterFactory.
    //
    protected String crop(String text)
    {
      if (text != null)
      {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
          if (Character.isISOControl(chars[i]))
          {
            return text.substring(0, i) + "...";
          }
        }
      }
      return text;
    }

    @Override
    public Object getImage(Object object)
    {
      return staticImage == null ? super.getImage(object) : staticImage;
    }
  }

  /**
   * This creates an instance that does not use a resource locator and determines the cell editor from the type of the
   * structural feature.  It assumed that the feature should be settable from this property.
   * 
   * <p>To reduce the number of constructors for this class, this one will soon be deprecated.  For new code, please
   * use {@link #ItemPropertyDescriptor(AdapterFactory, ResourceLocator, String, String, EStructuralFeature, boolean)
   * this} form, instead.
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      String displayName,
      String description)
  {
    this(adapterFactory, null, displayName, description, true, false, false, null, null, null);
  }

  /**
   * This creates an instance that uses a resource locator and determines the cell editor from the type of the
   * structural feature.  It assumed that the feature should be settable from this property.
   * 
   * <p>To reduce the number of constructors for this class, this one will soon be deprecated.  For new code, please
   * use {@link #ResourcePropertyDescriptor(AdapterFactory, ResourceLocator, String, String, EStructuralFeature, boolean)
   * this} form, instead.
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      ResourceLocator resourceLocator,
      String displayName,
      String description)
  {
    this(adapterFactory, resourceLocator, displayName, description, true, false, false, null, null, null);
  }

  /**
   * This creates an instance that does not use a resource locator and determines the cell editor from the type of the
   * structural feature. 
   * 
   * <p>To reduce the number of constructors for this class, this one may be deprecated in the future.  For new code, please
   * use {@link #ResourcePropertyDescriptor(AdapterFactory, ResourceLocator, String, String, EStructuralFeature, boolean)
   * this} form, instead.
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      String displayName,
      String description,
      boolean isSettable)
  {
    this(adapterFactory, null, displayName, description, isSettable, false, false, null, null, null);
  }

  /**
   * This creates an instance that uses a resource locator and determines the cell editor from the type of the
   * structural feature. 
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      ResourceLocator resourceLocator,
      String displayName,
      String description,
      boolean isSettable)
  {
    this(adapterFactory, resourceLocator, displayName, description, isSettable, false, false, null, null, null);
  }

  /**
   * This creates an instance that does not use a resource locator, specifies a static image, and determines the cell
   * editor from the type of the structural feature. 
   * 
   * <p>To reduce the number of constructors for this class, this one may be deprecated in the future.  For new code, please
   * use {@link #ResourcePropertyDescriptor(AdapterFactory, ResourceLocator, String, String, EStructuralFeature, boolean, Object)
   * this} form, instead.
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      String displayName,
      String description,
      boolean isSettable,
      Object staticImage)
  {
    this(adapterFactory, null, displayName, description, isSettable, false, false, staticImage, null, null);
  }

  /**
   * This creates an instance that uses a resource locator, specifies a static image, and determines the cell editor
   * from the type of the structural feature. 
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      ResourceLocator resourceLocator,
      String displayName,
      String description,
      boolean isSettable,
      Object staticImage)
  {
    this(adapterFactory, resourceLocator, displayName, description, isSettable, false, false, staticImage, null, null);
  }

  /**
   * This creates an instance that does not use a resource locator, specifies a category, and determines the cell
   * editor from the type of the structural feature. 
   * 
   * <p>To reduce the number of constructors for this class, this one will soon be deprecated.  For new code, please
   * use {@link #ResourcePropertyDescriptor(AdapterFactory, ResourceLocator, String, String, EStructuralFeature, boolean, String, String[])
   * this} form, instead.
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      String displayName,
      String description,
      boolean isSettable,
      String category)
  {
    this(adapterFactory, null, displayName, description, isSettable, false, false, null, category, null);
  }

  /**
   * This creates an instance that uses a resource locator, specifies a category, and determines the cell editor from
   * the type of the structural feature. 
   * 
   * <p>To reduce the number of constructors for this class, this one will soon be deprecated.  For new code, please
   * use {@link #ResourcePropertyDescriptor(AdapterFactory, ResourceLocator, String, String, EStructuralFeature, boolean, String, String[])
   * this} form, instead.
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      ResourceLocator resourceLocator,
      String displayName,
      String description,
      boolean isSettable,
      String category)
  {
    this(adapterFactory, resourceLocator, displayName, description, isSettable, false, false, null, category, null);
  }

  /**
   * This creates an instance that does not use a resource locator, specifies a static image and category, and
   * determines the cell editor from the type of the structural feature. 
   * 
   * <p>To reduce the number of constructors for this class, this one will soon be deprecated.  For new code, please
   * use {@link #ResourcePropertyDescriptor(AdapterFactory, ResourceLocator, String, String, EStructuralFeature, boolean, boolean, boolean, Object, String, String[])
   * this} form, instead.
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      String displayName,
      String description,
      boolean isSettable,
      Object staticImage,
      String category)
  {
    this(adapterFactory, null, displayName, description, isSettable, false, false, staticImage, category, null);
  }

  /**
   * This creates an instance that uses a resource locator, specifies a static image and category, and determines the
   * cell editor from the type of the structural feature. 
   * 
   * <p>To reduce the number of constructors for this class, this one will soon be deprecated.  For new code, please
   * use {@link #ResourcePropertyDescriptor(AdapterFactory, ResourceLocator, String, String, EStructuralFeature, boolean, boolean, boolean, Object, String, String[])
   * this} form, instead.
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      ResourceLocator resourceLocator,
      String displayName,
      String description,
      boolean isSettable,
      Object staticImage,
      String category)
  {
    this(adapterFactory, resourceLocator, displayName, description, isSettable, false, false, staticImage, category, null);
  }

  /**
   * This creates an instance that does not use a resource locator, specifies a category and filter flags, and
   * determines the cell editor from the type of the structural feature. 
   * 
   * <p>To reduce the number of constructors for this class, this one may be deprecated in the future.  For new code, please
   * use {@link #ResourcePropertyDescriptor(AdapterFactory, ResourceLocator, String, String, EStructuralFeature, boolean, String, String[])
   * this} form, instead.
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      String displayName,
      String description,
      boolean isSettable,
      String category,
      String [] filterFlags)
  {
    this(adapterFactory, null, displayName, description, isSettable, false, false, null, category, filterFlags);
  }

  /**
   * This creates an instance that uses a resource locator, specifies a category and filter flags, and determines the
   * cell editor from the type of the structural feature. 
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      ResourceLocator resourceLocator,
      String displayName,
      String description,
      boolean isSettable,
      String category,
      String [] filterFlags)
  {
    this(adapterFactory, resourceLocator, displayName, description, isSettable, false, false, null, category, filterFlags);
  }

  /**
   * This creates an instance that does not use a resource locator; specifies a static image, a category, and filter
   * flags; and determines the cell editor from the type of the structural feature. 
   * 
   * <p>To reduce the number of constructors for this class, this one may be deprecated in the future.  For new code, please
   * use {@link #ResourcePropertyDescriptor(AdapterFactory, ResourceLocator, String, String, EStructuralFeature, boolean, boolean, boolean, Object, String, String[])
   * this} form, instead.
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      String displayName,
      String description,
      boolean isSettable,
      Object staticImage,
      String category,
      String [] filterFlags)
  {
    this(adapterFactory, null, displayName, description, isSettable, false, false, staticImage, category, filterFlags);
  }

  /**
   * This creates an instance that uses a resource locator; specifies a static image, a category, and filter flags;
   * and determines the cell editor from the type of the structural feature. 
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      ResourceLocator resourceLocator,
      String displayName,
      String description,
      boolean isSettable,
      Object staticImage,
      String category,
      String [] filterFlags)
  {
    this(adapterFactory, resourceLocator, displayName, description, isSettable, false, false, staticImage, category, filterFlags);
    
  }

  /**
   * This creates an instance that uses a resource locator; indicates whether to be multi-line and to sort choices; specifies
   * a  static image, a category, and filter flags; and determines the cell editor from the type of the structural feature. 
   * @since 2.2.0
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      ResourceLocator resourceLocator,
      String displayName,
      String description,
      boolean isSettable,
      boolean multiLine,
      boolean sortChoices,
      Object staticImage,
      String category,
      String [] filterFlags)
  {
    this(adapterFactory, resourceLocator, displayName, description, isSettable, multiLine, sortChoices, staticImage, category, filterFlags, null);
  }

  /**
   * This creates an instance that uses a resource locator; indicates whether to be multi-line and to sort choices; specifies
   * a  static image, a category, and filter flags, and determines the cell editor from the editor factory.
   * @since 2.14
   */
  public ResourcePropertyDescriptor
     (AdapterFactory adapterFactory,
      ResourceLocator resourceLocator,
      String displayName,
      String description,
      boolean isSettable,
      boolean multiLine,
      boolean sortChoices,
      Object staticImage,
      String category,
      String [] filterFlags,
      Object editorFactory)
  {
    this.adapterFactory = adapterFactory;
    this.resourceLocator = resourceLocator;
    this.itemDelegator = new ItemDelegator(adapterFactory, resourceLocator);
    this.displayName = displayName;
    this.description = description;
    this.isSettable = isSettable;
    this.multiLine = multiLine;
    this.sortChoices = sortChoices;
    this.staticImage = staticImage;
    this.category = category;
    this.filterFlags = filterFlags;
    this.editorFactory = editorFactory;
  }

  /**
   * This returns the group of properties into which this one should be placed.
   */
  public String getCategory(Object object) 
  {
    return category;
  }

  /**
   * This returns the description to be displayed in the property sheet when this property is selected.
   */
  public String getDescription(Object object) 
  {
    return description;
  }

  /**
   * This returns the name of the property to be displayed in the property sheet.
   */
  public String getDisplayName(Object object) 
  {
    return displayName;
  }

  /**
   * This returns the flags used as filters in the property sheet.
   */
  public String[] getFilterFlags(Object object) 
  {
    return filterFlags;
  }

  public Object getHelpContextIds(Object object)
  {
    return null;
  }

  /**
   * This indicates whether these two property descriptors are equal.
   * It's not really clear to me how this is meant to be used, 
   * but it's a little bit like an equals test.
   */
  public boolean isCompatibleWith(Object object, Object anotherObject, IItemPropertyDescriptor anotherItemPropertyDescriptor) 
  {
/*
    if (propertyDescriptor == this)
    {
      return true;
    }
    else if (propertyDescriptor instanceof ItemPropertyDescriptor)
    {
      ItemPropertyDescriptor itemPropertyDescriptor = (ItemPropertyDescriptor)propertyDescriptor;
      if (adapterFactory == itemPropertyDescriptor.adapterFactory &&
            displayName.equals(itemPropertyDescriptor.displayName) &&
            (category == null && itemPropertyDescriptor.category == null || category.equals(itemPropertyDescriptor.category)))
      {
        return true;
      }
    }
*/

    return false;
  }

  /**
   * This does the delegated job of getting the property value from the given object; 
   * and it sets object, which is necessary if {@link #getComboBoxObjects getComboBoxObjects} is called.
   * It is implemented in a generic way using the structural feature or parent references.
   */
  public Object getPropertyValue(Object object)
  {
    return null;
  }

  /**
   * Sets the object to use as the owner of commands created to set the property's value.
   */
  public void setCommandOwner(Object commandOwner)
  {
    this.commandOwner = commandOwner;
  }

  /**
   * Returns the override command owner set via {@link #setCommandOwner setCommandOwner}.
   */
  public Object getCommandOwner()
  {
    return commandOwner;
  }

  /**
   * Returns either the override command owner set via {@link #setCommandOwner setCommandOwner} or, if that is null, the
   * fall-back object provided.
   */
  protected Object getCommandOwner(Object fallback)
  {
    return commandOwner != null ? commandOwner : fallback;
  }


  public EditingDomain getEditingDomain(Object object)
  {
    EObject eObject = (EObject)object;
    EditingDomain result = AdapterFactoryEditingDomain.getEditingDomainFor(eObject);
    if (result == null)
    {
      if (adapterFactory instanceof IEditingDomainProvider)
      {
        result = ((IEditingDomainProvider)adapterFactory).getEditingDomain();
      }

      if (result == null && adapterFactory instanceof ComposeableAdapterFactory)
      {
        AdapterFactory rootAdapterFactory = ((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory();
        if (rootAdapterFactory instanceof IEditingDomainProvider)
        {
          result = ((IEditingDomainProvider)rootAdapterFactory).getEditingDomain();
        }
      }
    }
    return result;
  }

  
  public boolean isMultiLine(Object object)
  {
    return multiLine;
  }

  public boolean isSortChoices(Object object)
  {
    return sortChoices;
  }

  /**
   * @since 2.14
   */
  public Object getEditorFactory(Object object)
  {
    return editorFactory;
  }

  /**
   * @since 2.14
   */
  public boolean isChoiceArbitrary(Object object)
  {
    return false;
  }

  @Override
  public boolean isPropertySet(Object object) {
    return false;
  }

  @Override
  public boolean canSetProperty(Object object) {
    return false;
  }

  @Override
  public void resetPropertyValue(Object object) {
  }

  @Override
  public void setPropertyValue(Object object, Object value) {
  }

  @Override
  public String getId(Object object) {
    return object.toString();
  }

  @Override
  public Object getFeature(Object object) {
    return null;
  }

  @Override
  public boolean isMany(Object object) {
    return true;
  }

  @Override
  public Collection<?> getChoiceOfValues(Object object) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IItemLabelProvider getLabelProvider(Object object) {
    return new IItemLabelProvider() {
      
      @Override
      public String getText(Object object) {
        return displayName;
      }
      
      @Override
      public Object getImage(Object object) {
        return staticImage;
      }
    };
  }
}
