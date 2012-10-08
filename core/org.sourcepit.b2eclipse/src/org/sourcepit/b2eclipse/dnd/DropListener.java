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
import org.sourcepit.b2eclipse.input.Node;

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
      setFeedbackEnabled(false);
      this.viewer = viewer;
   }

   @Override
   public boolean performDrop(Object data)
   {
      Node target = (Node) this.getCurrentTarget();

      for (String iter : (String[]) data)
      {
         if (iter != "" && target != null)
         {
            Node selected = ((Node) viewer.getInput()).getEqualNode(new File(iter));
            if (selected != null && selected.getType() == Node.Type.PROJECT)
            {
               if (target.getType() == Node.Type.WORKINGSET)
               {
                  selected.getParent().removeChild(selected);
                  selected.setParent(target);
                  target.addChild(selected);
               }
               else if (target.getType() == Node.Type.PROJECT)
               {
                  selected.getParent().removeChild(selected);
                  selected.setParent(target.getParent());
                  target.getParent().addChild(selected);
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
      if (_target instanceof Node)
         return true;
      return false;
   }

}
