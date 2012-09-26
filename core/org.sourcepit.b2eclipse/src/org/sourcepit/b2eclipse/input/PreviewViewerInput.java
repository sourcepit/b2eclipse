package org.sourcepit.b2eclipse.input;

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
      createNodeSystemForPreview();
   }
   
   /**
    * Returns a initial Node System that is representing the Preview
    * @return 
    * 
    * 
    * @return the Node (system)
    */
   public void createNodeSystemForPreview()
   {
      for (Node i : abstractRoot.getChildren())
      {
         Node kid = new Node(abstractRoot, i.getFile(), i.getType());
         for (Node j : i.getProjectChildren())
         {
            new Node(kid, j.getFile(), j.getType());
         }
      }
   }
}
