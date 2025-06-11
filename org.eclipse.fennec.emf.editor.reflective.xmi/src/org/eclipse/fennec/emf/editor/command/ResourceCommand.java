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
package org.eclipse.fennec.emf.editor.command;


import java.util.Collection;
import java.util.Collections;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.emf.edit.EMFEditPlugin;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.command.AbstractOverrideableCommand;
import org.eclipse.emf.edit.command.CommandActionDelegate;
import org.eclipse.emf.edit.command.CommandParameter;


/**
 * The add command logically acts upon an owner object that has a collection-based feature to which other objects can be added.
 * The static create methods delegate command creation to {@link EditingDomain#createCommand EditingDomain.createCommand},
 * which may or may not result in the actual creation of an instance of this class.
 * 
 * <p>
 * The implementation of this class is low-level and EMF specific;
 * it allows one or more objects to be added to a many-valued feature of an owner,
 * i.e., it is equivalent of the call
 * <pre>
 *   ((EList)((EObject)owner).eGet((EStructuralFeature)feature)).addAll(index, (Collection)collection);
 * </pre>
 *
 * <p>
 * It can also be used as an equivalent to the call
 * <pre>
 *   ((EList)extent).addAll(index, (Collection)collection);
 * </pre>
 * which is how root objects are added into the contents of a resource.
 * Like all the low-level commands in this package, the add command is undoable.
 *
 * <p>
 * An add command is an {@link OverrideableCommand}.
 */
public class ResourceCommand extends AbstractOverrideableCommand implements CommandActionDelegate
{
  /**
   * This is the list to which the command will add the collection.
   */
  protected EList<EObject> ownerList;

  /**
   * This is the collection of objects being added to the owner list.
   */
  protected Collection<EObject> collection;

  /**
   * This is the position at which the objects will be inserted.
   */
  protected int index;

  /**
   * This is the value returned by {@link Command#getAffectedObjects}. 
   * The affected objects are different after an execute than after an undo, so we record it.
   */
  protected Collection<?> affectedObjects;

  private Resource owner;

  /**
   * This constructs a primitive command to add a particular value to the specified many-valued feature of the owner.
   */
  public ResourceCommand(EditingDomain domain, Resource owner, EObject value)
  {
    this(domain, value.eClass().getName(), "Adds a new " + value.eClass().getName(),  owner, Collections.singleton(value), CommandParameter.NO_INDEX);
  }

  /**
   * This constructs a primitive command to insert particular value into the specified many-valued feature of the owner.
   */
  public ResourceCommand(EditingDomain domain, Resource owner, EObject value, int index)
  {
    this(domain, value.eClass().getName(), "Adds a new " + value.eClass().getName(),  owner, Collections.singleton(value), index);
  }

  /**
   * This constructs a primitive command to add a collection of values to the specified many-valued feature of the owner.
   */
  public ResourceCommand(EditingDomain domain, String label, String description, Resource owner, Collection<EObject> collection)
  {
    this(domain, label, description, owner, collection, CommandParameter.NO_INDEX);
  }

  /**
   * This constructs a primitive command to insert a collection of values into the specified many-valued feature of the owner.
   */
  public ResourceCommand(EditingDomain domain, String label, String description, Resource owner, Collection<EObject> collection, int index)
  {
    super(domain, label, description);
    this.owner = owner;

    this.collection = collection;
    this.index = index;

    ownerList = owner.getContents();
  }

  /**
   * This returns the list to which the command will add.
   */
  public EList<?> getOwnerList()
  {
    return ownerList;
  }

  /**
   * This returns the collection of objects being added.
   */
  public Collection<?> getCollection()
  {
    return collection;
  }

  /**
   * This returns the position at which the objects will be added.
   */
  public int getIndex()
  {
    return index;
  }

  protected boolean isUserElement(EStructuralFeature entryFeature)
  {
    return
      entryFeature != XMLTypePackage.Literals.XML_TYPE_DOCUMENT_ROOT__TEXT &&
      entryFeature != XMLTypePackage.Literals.XML_TYPE_DOCUMENT_ROOT__CDATA &&
      entryFeature != XMLTypePackage.Literals.XML_TYPE_DOCUMENT_ROOT__COMMENT &&
      entryFeature != XMLTypePackage.Literals.XML_TYPE_DOCUMENT_ROOT__PROCESSING_INSTRUCTION;
  }

  @Override
  protected boolean prepare()
  {
    // If there is no list to add to, no collection or an empty collection from which to add, or the index is out of range...
    //
    if (ownerList == null || 
          collection == null ||
          collection.size() == 0 ||
          index != CommandParameter.NO_INDEX && (index < 0 || index > ownerList.size()))
    {
      return false;
    }
    return true;
  }

  @Override
  public void doExecute() 
  {
    // Simply add the collection to the list.
    //
    if (index == CommandParameter.NO_INDEX)
    {
      ownerList.addAll(collection);
    }
    else
    {
      ownerList.addAll(index, collection);
    }

    // We'd like the collection of things added to be selected after this command completes.
    //
    affectedObjects = collection;
  }

  @Override
  public void doUndo() 
  {
    // Remove the collection from the list by index.
    //
    int i = index != CommandParameter.NO_INDEX ? index : ownerList.size() - collection.size();
    ownerList.subList(i, i + collection.size()).clear();    
  
    // We'd like the owner selected after this undo completes.
    //
    affectedObjects = owner == null ? Collections.EMPTY_SET : Collections.singleton(owner);
  }

  @Override
  public void doRedo()
  {
    // Simply add the collection to the list.
    //
    if (index == CommandParameter.NO_INDEX)
    {
      ownerList.addAll(collection);
    }
    else
    {
      ownerList.addAll(index, collection);
    }

    // We'd like the collection of things added to be selected after this command completes.
    //
    affectedObjects = collection;
  }

  @Override
  public Collection<?> doGetResult()
  {
    return collection;
  }

  @Override
  public Collection<?> doGetAffectedObjects()
  {
    return affectedObjects;
  }

  /**
   * This gives an abbreviated name using this object's own class' name, without package qualification,
   * followed by a space separated list of <tt>field:value</tt> pairs.
   */
  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder(super.toString());
    result.append(" (owner: " + owner + ")");
    result.append(" (ownerList: " + ownerList + ")");
    result.append(" (collection: " + collection + ")");
    result.append(" (index: " + index + ")");
    result.append(" (affectedObjects:" + affectedObjects + ")");

    return result.toString();
  }

  @Override
  public Object getImage() {
    
    return EMFEditPlugin.INSTANCE.getImage("full/ctool16/CreateChild");
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
