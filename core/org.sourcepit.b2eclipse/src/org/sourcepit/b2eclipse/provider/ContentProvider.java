
package org.sourcepit.b2eclipse.provider;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.sourcepit.b2eclipse.input.Node;


public class ContentProvider implements ITreeContentProvider
{

   public void dispose()
   {
      // TODO Auto-generated method stub
   }

   public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
   {
      // TODO Auto-generated method stub
      //viewer.setInput(((Node) newInput));
   }

   public Object[] getElements(Object inputElement)
   {
      //Da oberstes root und nichts drin ist 
      return ((Node) inputElement).getChildren().toArray();
   }

   public Object[] getChildren(Object parentElement)
   {
      return ((Node) parentElement).getChildren().toArray();
   }

   public Object getParent(Object element)
   {
      return ((Node) element).getParent();
   }

   public boolean hasChildren(Object element)
   {
      return ((Node) element).getChildren().size() > 0;
   }

}
