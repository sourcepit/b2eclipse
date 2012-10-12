/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.provider;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.sourcepit.b2eclipse.input.node.Node;

/**
 * 
 * @author WD
 *
 */
public class ContentProvider implements ITreeContentProvider
{

   public void dispose()
   {
      // TODO Auto-generated method stub
   }

   public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
   {
      // TODO Auto-generated method stub
   }

   public Object[] getElements(Object inputElement)
   {
      //Da oberstes root und nichts drin ist 
      return ((Node) inputElement).getChildren().toArray();
   }

   public Object[] getChildren(Object parentElement)
   {
      return ((Node) parentElement).getChildren().toArray();
   }

   public Object getParent(Object element)
   {
      return ((Node) element).getParent();
   }

   public boolean hasChildren(Object element)
   {
      return ((Node) element).getChildren().size() > 0;
   }

}
