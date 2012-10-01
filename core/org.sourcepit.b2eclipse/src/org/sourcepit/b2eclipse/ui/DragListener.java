/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.sourcepit.b2eclipse.input.Node;

/**
 * 
 * @author WD
 *
 */
public class DragListener implements DragSourceListener
{
   private TreeViewer viewer;

   private Node node;

   public DragListener(TreeViewer previewTreeViewer)
   {
      this.viewer = previewTreeViewer;
   }

   public void dragFinished(DragSourceEvent event)
   {
      if (event.doit)
      {
         Node parent = node.getParent();
         node.deleteNode();
         if (parent.getChildren().size() == 0)
            parent.deleteNode();

         viewer.refresh();
      }
   }


   public void dragSetData(DragSourceEvent event)
   {
      if (node.getType() == Node.Type.PROJECT)
         event.data = node.getFile().toString();
      else
         event.data = "";
   }

   public void dragStart(DragSourceEvent event)
   {
      node = (Node) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
      if (node.getType() != Node.Type.PROJECT)
         event.doit = false;

   }

}
