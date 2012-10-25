/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.provider;


import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.sourcepit.b2eclipse.Activator;
import org.sourcepit.b2eclipse.input.node.Node;
import org.sourcepit.b2eclipse.input.node.NodeFolder;
import org.sourcepit.b2eclipse.input.node.NodeModule;
import org.sourcepit.b2eclipse.input.node.NodeModuleProject;
import org.sourcepit.b2eclipse.input.node.NodeProject;
import org.sourcepit.b2eclipse.ui.Messages;

/**
 * @author WD
 */
public class LabelProviderForDir extends StyledCellLabelProvider
{
   private Shell shell;

   public LabelProviderForDir(Shell _shell)
   {
      shell = _shell;
   }

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

      if (node instanceof NodeModuleProject)
      {
         if (!node.hasConflict())
            label.append(Messages.msgModuleProject, StyledString.DECORATIONS_STYLER);
         else
            label.append(Messages.msgModuleProject);
         cell.setImage(PlatformUI.getWorkbench().getSharedImages()
            .getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT));
      }

      if (node instanceof NodeModule)
      {
         label.append(node.getName());
         String fix = ((NodeModule) node).getPrefix();
         if (fix != null)
            label.append("  (" + fix + ")", StyledString.DECORATIONS_STYLER);
         cell.setImage(Activator.getImageFromPath("org.eclipse.jdt.ui", "$nl$/icons/full/obj16/packagefolder_obj.gif"));
      }

      if (node instanceof NodeFolder)
      {
         label.append(node.getName());
         cell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER));
         // TODO maybe find a better icon
      }


      if (node.hasConflict())
         cell.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_GRAY));
      // TODO maybe gray out the Icons

      cell.setText(label.toString());
      cell.setStyleRanges(label.getStyleRanges());

      super.update(cell);
   }
}
