/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.input.node;

import java.io.File;
import java.util.ArrayList;

/**
 * 
 * @author WD
 * 
 */
public class NodeModule extends Node
{
   private String prefix;

   public NodeModule(Node parent, File file, String name)
   {
      super();
      children = new ArrayList<Node>();
      this.file = null;// _file;
      this.name = name;
      this.parent = parent;
      parent.addChild(this);
      prefix = null;
   }

   public void setPrefix(String _prefix)
   {
      if (_prefix != null)
         prefix = new String(_prefix);
      else
         prefix = null;
   }

   public String getPrefix()
   {
      return prefix;
   }
}
