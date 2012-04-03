/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.provider;


import java.io.File;
import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.sourcepit.b2eclipse.structure.Category;
import org.sourcepit.b2eclipse.structure.TreeviewerInput;

public class ContentProvider implements ITreeContentProvider
{

   private static ArrayList<File> projectFileList = new ArrayList<File>();

   private TreeviewerInput input;

   @Override
   public Object[] getElements(Object inputElement)
   {
      return input.getData().toArray();
   }


   @Override
   public Object[] getChildren(Object parentElement)
   {
      if (parentElement instanceof Category)
      {
         return ((Category)parentElement).getPlugins().toArray();
      }
      return null;
   }

   @Override
   public Object getParent(Object element)
   {
      return null;
   }

   @Override
   public boolean hasChildren(Object element)
   {
      if (element instanceof Category)
      {
         return true;
      }
      return false;
   }


   @Override
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
   {
      this.input = (TreeviewerInput) newInput;
   }

   @Override
   public void dispose()
   {
   }

   public ArrayList<File> getProjects()
   {
      return projectFileList;
   }


}
