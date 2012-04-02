/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.provider;


import java.io.File;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */
public class TreeLabelProvider extends LabelProvider
{
   private File file;
   private String cutString;

   /**
    * Ordner icons im Treeviewer
    */
   @Override
   public Image getImage(Object element)
   {

      return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
   }

   @Override
   public String getText(Object element)
   {

      file = (File) element;
      cutString = file.getParent();
      return cutString.substring(cutString.lastIndexOf("\\")).replace("\\", "").concat("  (" + cutString + ")");

   }


}
