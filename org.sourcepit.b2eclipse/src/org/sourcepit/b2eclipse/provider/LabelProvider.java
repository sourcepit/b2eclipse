/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.provider;


import java.io.File;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.sourcepit.b2eclipse.structure.Category;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */
public class LabelProvider extends StyledCellLabelProvider
{
   private File file;
   private String cutString;

   /**
    * Ordner icons im Treeviewer
    */
   
   @Override
   public void update(ViewerCell cell) {
       Object element = cell.getElement();
       StyledString text = new StyledString();

       if (element instanceof Category) {
           Category category = (Category) element;
           text.append(category.getName());
           cell.setImage(PlatformUI.getWorkbench().getSharedImages()
                   .getImage(ISharedImages.IMG_OBJ_FOLDER));
           text.append(" ( " +category.getPlugins().size() + " ) ", StyledString.COUNTER_STYLER);
       } else {
          file = (File) element;
          cutString = file.getParent();
          text.append(cutString.substring(cutString.lastIndexOf("\\")).replace("\\", "").concat("  (" + cutString + ")"));
           cell.setImage(PlatformUI.getWorkbench().getSharedImages()
                   .getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT ));
       }
       cell.setText(text.toString());
       cell.setStyleRanges(text.getStyleRanges());
       super.update(cell);
   }



}
