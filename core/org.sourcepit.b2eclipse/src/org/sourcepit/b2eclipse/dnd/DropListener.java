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
      this.viewer = viewer;
   }

   @Override
   public boolean performDrop(Object data)
   {
      Node target = (Node) this.getCurrentTarget();
      if (data != "" && target != null)
      {
         if (target.getType() == Node.Type.WORKINGSET)
         {
            new Node(target, new File((String) data), Node.Type.PROJECT);
            return true;
         }
         else if (target.getType() == Node.Type.PROJECT)
         {
            new Node(target.getParent(), new File((String) data), Node.Type.PROJECT);
            return true;
         }
      }
      if (target == null)
      {
         new Node((Node) viewer.getInput(), new File((String) data), Node.Type.PROJECT);
      }
      return false;
   }

   @Override
   public boolean validateDrop(Object _target, int operation, TransferData transferType)
   {
      return true;
   }

}
