
package org.sourcepit.b2eclipse.input;

import java.util.ArrayList;
import java.util.List;

public class PreviewViewerInput extends ViewerInput
{
   Node root;

   /**
    * Initialize the NodeSystem for the Preview, saving it in <code>abstractRoot</code>.
    * 
    * @param _root reference to the NodeSystem of the DirectoryViewer
    */
   public PreviewViewerInput(Node _root)
   {
      root = _root;
      abstractRoot = new Node();
   }

   /**
    * Returns a initial Node System that is representing the Preview
    * 
    * @return the Node (system)
    */
   public Node createNodeSystemForPreviev()
   {
      List<Node> parentList = new ArrayList<Node>();
      for (Node i : root.getChildren()) //vom Linken Viewer die kiddis des roots
      {
         Node ws = abstractRoot;
         for (Node j : i.getProjectChildren()) //Die projekt kiddies eines root kiddi
         {
            if (!parentList.contains(j.getParent())) //Trifft beim ersten Mal immer zu
            {
               parentList.add(j.getParent());
               ws = new Node(abstractRoot, j.getParent().getFile(), Node.Type.WORKINGSET, j.getWSName(j.getParent()));
            }
            new Node(ws, j.getFile(), j.getType());
         }
      }
      return abstractRoot;
   }

   
}
