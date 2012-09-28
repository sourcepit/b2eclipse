/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.provider;


import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.sourcepit.b2eclipse.input.Node;

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

      switch (node.getType())
      {
         case PROJECT :

            label.append(node.getName());

            label.append("  (" + node.getFile().getAbsolutePath() + ")", StyledString.DECORATIONS_STYLER);


            cell.setImage(PlatformUI.getWorkbench().getSharedImages()
               .getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT));
            break;

         case MODULE :
            label.append(node.getName());
            cell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER));
            break;
            
         case WORKINGSET :
            label.append(node.getName());
            cell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT));
            break;

         default :
            label.append("unknown File, run for youre Life!");
            cell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));
            break;
      }

      cell.setText(label.toString());
      cell.setStyleRanges(label.getStyleRanges());

      super.update(cell);
   }
}
