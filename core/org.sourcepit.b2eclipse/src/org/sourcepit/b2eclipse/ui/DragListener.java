package org.sourcepit.b2eclipse.ui;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.sourcepit.b2eclipse.input.Node;

public class DragListener implements DragSourceListener
{
   private TreeViewer viewer;

   public DragListener (TreeViewer previewTreeViewer){
      this.viewer = previewTreeViewer;
   }
   
   public void dragFinished(DragSourceEvent event)
   {
      // TODO Auto-generated method stub
      System.out.println("Finshed Drag");
   }
   

   public void dragSetData(DragSourceEvent event)
   {
      // TODO Auto-generated method stub
      System.out.println(event.getClass().toString());
      IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
      System.out.println(selection.getClass().toString());
      
      
         
   }

   public void dragStart(DragSourceEvent event)
   {
      // TODO Auto-generated method stub
      System.out.println("Start Drag");
      System.out.println(event.getClass().toString());
      IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
      System.out.println(((Node) selection.getFirstElement()).getName()); //GEHT!! Nun weiter!
   }

}
