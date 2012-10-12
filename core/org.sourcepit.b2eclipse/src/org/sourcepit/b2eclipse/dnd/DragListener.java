/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.dnd;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.sourcepit.b2eclipse.input.node.Node;
import org.sourcepit.b2eclipse.input.node.NodeProject;


/**
 * 
 * @author WD
 * 
 */
public class DragListener implements DragSourceListener
{
   private TreeViewer viewer;
   private ArrayList<String> transferData;

   public DragListener(TreeViewer previewTreeViewer)
   {
      this.viewer = previewTreeViewer;
   }

   public void dragFinished(DragSourceEvent event)
   {
      /* do nothing */
   }

   public void dragSetData(DragSourceEvent event)
   {
      event.data = transferData.toArray(new String[0]);
   }

   public void dragStart(DragSourceEvent event)
   {
      transferData = new ArrayList<String>();
      for (Object iter : ((IStructuredSelection) viewer.getSelection()).toArray())
      {
         if (((Node) iter) instanceof NodeProject)
            transferData.add(((NodeProject) iter).getFile().toString());
         else
         {
            event.doit = false;
         }
      }
   }
}
