package org.sourcepit.b2eclipse.input.node;

import java.io.File;
import java.util.ArrayList;

public class NodeWorkingSet extends Node
{   
   public NodeWorkingSet(Node _parent, Node _accordingNode)
   {
      super();
      children = new ArrayList<Node>();
      parent = _parent;
      _parent.addChild(this);
      if(_accordingNode != null)
      {
         file = _accordingNode.getFile();
         name = _accordingNode.getWSName(_accordingNode);
      }
      else
      {
         name = "";
         file = new File("");
      }
   }
   
   public NodeWorkingSet(Node _parent, String _name)
   {
      super();
      children = new ArrayList<Node>();
      parent = _parent;
      _parent.addChild(this);
      
      name = _name;
      file = new File("");
      
   }
}
