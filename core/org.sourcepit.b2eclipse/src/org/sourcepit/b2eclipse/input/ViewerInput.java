package org.sourcepit.b2eclipse.input;

public abstract class ViewerInput //eventuell sinnlos
{
   Node abstractRoot;   
   
   /**
    * Returns the root Node/s which are under the empty <code>abstractRoot</code> Node.
    * 
    * @return the root Node/s
    */
   public Object[] getRoot(){
      return abstractRoot.getChildren().toArray();
   }
   
   /**
    * Returns the children of the <code>searchedNode</code>.
    * 
    * @param searchedNode
    * @return the children
    */
   public Object[] getChildren(Node searchedNode){
      return abstractRoot.getEqualNode(searchedNode).getChildren().toArray();      
   }
   
   /**
    * Returns the parent of the <code>searchedNode</code>.
    * 
    * @param searchedNode
    * @return the parent
    */
   public Object getParent(Node searchedNode){
      return abstractRoot.getEqualNode(searchedNode).getParent();
   }
   
}
