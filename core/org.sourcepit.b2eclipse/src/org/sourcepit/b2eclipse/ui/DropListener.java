package org.sourcepit.b2eclipse.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

public class DropListener extends  ViewerDropAdapter 
{

   protected DropListener(Viewer viewer)
   {
      super(viewer);
      // TODO Auto-generated constructor stub
   }

   @Override
   public boolean performDrop(Object data)
   {
      System.out.println(data.getClass().toString());
      
      
      
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean validateDrop(Object arg0, int arg1, TransferData arg2)
   {
      // TODO Auto-generated method stub
      return false;
   }

}
