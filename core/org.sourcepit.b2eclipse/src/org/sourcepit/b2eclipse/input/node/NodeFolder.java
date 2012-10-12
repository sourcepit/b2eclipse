package org.sourcepit.b2eclipse.input.node;

import java.io.File;
import java.util.ArrayList;

public class NodeFolder extends Node
{
   public NodeFolder(Node _parent, File _file)
   {
      super();
      children = new ArrayList<Node>();
      file = _file;
      name = file.getName();
      parent = _parent;
      _parent.addChild(this);
   }
}
