/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.provider;


import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.sourcepit.b2eclipse.input.Category;
import org.sourcepit.b2eclipse.input.TreeViewerInput;
/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */
public class ContentProvider implements ITreeContentProvider
{
   public Object[] getElements(Object inputElement)
   {
      return ((TreeViewerInput) inputElement).getData().toArray();
   }

   public Object[] getChildren(Object parentElement)
   {
      if (parentElement instanceof Category)
      {
         return ((Category) parentElement).getModules().toArray();
      }
      return null;
   }

   public Object getParent(Object element)
   {
      return null;
   }

   public boolean hasChildren(Object element)
   {
      if (element instanceof Category)
      {
         return true;
      }
      return false;
   }

   public void inputChanged(Viewer viewer, Object oldInputData, Object newInputData)
   {
   }

   public void dispose()
   {
   }

}
