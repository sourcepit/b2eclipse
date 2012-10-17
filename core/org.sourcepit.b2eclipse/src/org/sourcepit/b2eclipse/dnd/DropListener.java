/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.dnd;

import java.io.File;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.sourcepit.b2eclipse.input.node.Node;
import org.sourcepit.b2eclipse.input.node.NodeModule;
import org.sourcepit.b2eclipse.input.node.NodeProject;
import org.sourcepit.b2eclipse.input.node.NodeWorkingSet;

/**
 * 
 * @author WD
 * 
 */
public class DropListener extends ViewerDropAdapter
{
   private TreeViewer viewer;

   public DropListener(TreeViewer viewer)
   {
      super(viewer);
      this.viewer = viewer;
   }

   @Override
   public boolean performDrop(Object data)
   {
      Node target = (Node) this.getCurrentTarget();      
      int loc = this.getCurrentLocation();

      for (String iter : (String[]) data)
      {
         if (iter != "")
         {
            Node selected = ((Node) viewer.getInput()).getEqualNode(new File(iter));
            if (selected != null && (selected instanceof NodeProject || selected instanceof NodeModule))
            {
               if (target != null)
               {
                  if (target instanceof NodeWorkingSet)
                  {
                     selected.getParent().removeChild(selected);
                     selected.setParent(target);
                     target.addChild((Node)selected);
                  }
                  if (target instanceof NodeProject || target instanceof NodeModule)
                  {
                     selected.getParent().removeChild(selected);
                     selected.setParent(target.getParent());
                     target.getParent().addChild((Node)selected);
                  }
               }
               if (loc == LOCATION_NONE || (target instanceof NodeWorkingSet && (loc == LOCATION_AFTER || loc == LOCATION_BEFORE)))
               {
                  selected.getParent().removeChild(selected);
                  selected.setParent((Node) viewer.getInput());
                  ((Node) viewer.getInput()).addChild((Node)selected);
               }
            }
         }
      }
      viewer.refresh();
      return true;
   }

   @Override
   public boolean validateDrop(Object _target, int operation, TransferData transferType)
   {
      // if (_target instanceof Node)
      return true;
      // return false;
   }

}
