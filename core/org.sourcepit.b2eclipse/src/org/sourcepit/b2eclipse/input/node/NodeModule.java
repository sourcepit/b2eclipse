package org.sourcepit.b2eclipse.input.node;

import java.io.File;
import java.util.ArrayList;

public class NodeModule extends Node
{
   private String prefix;
   
   public NodeModule(Node _parent, File _file, String _name){
      super();
      children = new ArrayList<Node>();
      file = _file;
      name = _name;
      parent = _parent;
      _parent.addChild(this);
      
      prefix = "";
   }
   
   public void setPrefix(String _prefix)
   {
      prefix = _prefix;
   }
   
   public String setPrefix()
   {
      return prefix;
   }  
}
