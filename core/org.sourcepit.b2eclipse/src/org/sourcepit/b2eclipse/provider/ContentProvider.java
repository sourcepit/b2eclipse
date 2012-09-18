/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.provider;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.sourcepit.b2eclipse.input.SubCategory;
import org.sourcepit.b2eclipse.input.ParentCategory;
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
      List<Object> result = new ArrayList<Object>();
      if (parentElement instanceof ParentCategory)
      {
         ParentCategory parent = (ParentCategory) parentElement;
         result.addAll(parent.getCategoryEntries());

      }
      else if (parentElement instanceof SubCategory)
      {
         SubCategory sub = (SubCategory) parentElement;
         result.addAll(sub.getFileEntries());
      }
      return result.toArray();
   }

   public Object getParent(Object element)
   {
      return null;
   }

   public boolean hasChildren(Object element)
   {
      return getChildren(element).length > 0;
   }

   public void inputChanged(Viewer viewer, Object oldInputData, Object newInputData)
   {
   }

   public void dispose()
   {
   }

}
