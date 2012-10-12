/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.provider;


import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.sourcepit.b2eclipse.input.node.Node;
import org.sourcepit.b2eclipse.input.node.NodeFolder;
import org.sourcepit.b2eclipse.input.node.NodeModule;
import org.sourcepit.b2eclipse.input.node.NodeProject;
import org.sourcepit.b2eclipse.input.node.NodeWorkingSet;

/**
 * @author WD
 */
public class LabelProvider extends StyledCellLabelProvider
{
   /**
    * Specify the settings of the TreeViewer.
    */
   @Override
   public void update(ViewerCell cell)
   {
      final StyledString label = new StyledString();
      Node node = ((Node) cell.getElement());

      if (node instanceof NodeProject)
      {
         label.append(node.getName());
         label.append("  (" + node.getFile().getAbsolutePath() + ")", StyledString.DECORATIONS_STYLER);
         cell.setImage(PlatformUI.getWorkbench().getSharedImages()
            .getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT));
      }

      if (node instanceof NodeModule)
      {
         label.append(node.getName());
         cell.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.jdt.ui",
            "$nl$/icons/full/obj16/packagefolder_obj.gif").createImage());
      }

      if (node instanceof NodeFolder)
      {
         label.append(node.getName());
         cell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER));
      }

      if (node instanceof NodeWorkingSet)
      {
         DecorationOverlayIcon icon = new DecorationOverlayIcon(AbstractUIPlugin.imageDescriptorFromPlugin(
            "org.eclipse.ui", "$nl$/icons/full/obj16/fldr_obj.gif").createImage(),
            AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.jdt.ui", "$nl$/icons/full/ovr16/java_ovr.gif"),
            IDecoration.TOP_LEFT);
         label.append(node.getName());
         cell.setImage(icon.createImage());
         // TODO anderes Icon finden
      }

//      else // Should never happen!
//      {
//         label.append("unknown File, run for youre Life!");
//         cell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));
//      }

      cell.setText(label.toString());
      cell.setStyleRanges(label.getStyleRanges());

      super.update(cell);
   }
}
