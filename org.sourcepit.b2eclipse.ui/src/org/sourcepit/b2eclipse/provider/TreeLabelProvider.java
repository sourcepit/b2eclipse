
package org.sourcepit.b2eclipse.provider;


import java.io.File;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


public class TreeLabelProvider extends LabelProvider
{

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
     
         File file = (File) element;
         return file.getParent();
          
   }


}
