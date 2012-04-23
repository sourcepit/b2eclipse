/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.provider;


import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.sourcepit.b2eclipse.input.Category;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */
public class LabelProvider extends StyledCellLabelProvider
{
   /**
    * specifiy the settings of the TreeViewer
    */
   @Override
   public void update(ViewerCell cell)
   {
      final Object element = cell.getElement();
      final StyledString label = new StyledString();


      if (element instanceof Category)
      {
         final Category category = (Category) element;
         label.append(category.getName());
         cell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER));
         label.append(" ( " + category.getModules().size() + " ) ", StyledString.COUNTER_STYLER);
      }
      else
      {
         final File projectDir = (File) element;

         final IPath prijectDirPath = new Path(projectDir.getParent());

         final String projectName = prijectDirPath.lastSegment();
         label.append(projectName);

         final IPath pathToProject = prijectDirPath.removeLastSegments(1);
         label.append(" (", StyledString.DECORATIONS_STYLER);
         label.append(pathToProject.toOSString(), StyledString.DECORATIONS_STYLER);
         label.append(")", StyledString.DECORATIONS_STYLER);

         cell.setImage(PlatformUI.getWorkbench().getSharedImages()
            .getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT));

      }
      cell.setText(label.toString());
      cell.setStyleRanges(label.getStyleRanges());

      super.update(cell);
   }
}
