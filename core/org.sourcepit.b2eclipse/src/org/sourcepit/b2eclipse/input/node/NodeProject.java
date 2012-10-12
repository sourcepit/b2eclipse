package org.sourcepit.b2eclipse.input.node;

import java.io.File;
import java.util.ArrayList;

public class NodeProject extends Node
{
   protected ProjectType type;
   
   public static enum ProjectType
   {
      PWS, PDIR
   }
   
   public NodeProject(Node _parent, File _file, ProjectType _type){
      super();
      children = new ArrayList<Node>();
      file = _file;
      name = file.getName();
      type = _type;
      parent = _parent;
      _parent.addChild(this);
   }
}
