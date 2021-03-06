/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.provider;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.sourcepit.b2eclipse.Activator;
import org.sourcepit.b2eclipse.input.node.Node;
import org.sourcepit.b2eclipse.input.node.NodeModuleProject;
import org.sourcepit.b2eclipse.input.node.NodeProject;
import org.sourcepit.b2eclipse.input.node.NodeWorkingSet;
import org.sourcepit.b2eclipse.ui.Messages;

/**
 * 
 * @author WD
 * 
 */
public class LabelProviderForPreview extends StyledCellLabelProvider
{
   @Override
   public void update(ViewerCell cell)
   {
      final StyledString label = new StyledString();
      Node node = ((Node) cell.getElement());

      if (node instanceof NodeProject)
      {
         label.append(node.getName());
         cell.setImage(PlatformUI.getWorkbench().getSharedImages()
            .getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT));
      }

      if (node instanceof NodeModuleProject)
      {
         label.append(node.getName() + " ");
         label.append(Messages.msgModuleProject, StyledString.DECORATIONS_STYLER);
         cell.setImage(PlatformUI.getWorkbench().getSharedImages()
            .getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT));
      }

      if (node instanceof NodeWorkingSet)
      {
         ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
         String key = "foo";
         Image i = imageRegistry.get(key);
         if (i == null)
         {
            DecorationOverlayIcon icon = new DecorationOverlayIcon(Activator.getImageFromPath("org.eclipse.ui",
               "$nl$/icons/full/obj16/fldr_obj.gif"), AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.jdt.ui",
               "$nl$/icons/full/ovr16/java_ovr.gif"), IDecoration.TOP_LEFT);
            imageRegistry.put(key, icon.createImage());
         }
         label.append(node.getName());
         cell.setImage(imageRegistry.get(key));
         // TODO maybe find a better icon
      }

      cell.setText(label.toString());
      cell.setStyleRanges(label.getStyleRanges());

      super.update(cell);
   }
}
