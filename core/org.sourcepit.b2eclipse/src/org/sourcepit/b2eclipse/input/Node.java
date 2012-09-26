/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.input;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Class that represents the elements(Nodes) in a tree
 * 
 * @author WD
 */
public class Node
{
   private String name;
   private File file;
   private Node parent;
   private List<Node> children;
   private Type type;

   public enum Type
   {
      MODULE, PROJECT
   }

   /**
    * default Constructor
    */
   public Node()
   {
      children = new ArrayList<Node>();
   }

   public Node(Node _parent, Node _copy)
   {
      parent = _parent;
      file = _copy.getFile();
      name = file.getName();
      children = _copy.getChildren();
      type = _copy.getType();
   }

   /**
    * Creates a new Node under the <code>_parent</code> Node.
    * 
    * @param _parent the parent element
    * @param _file the given File
    * @param _type the given Type
    */
   public Node(Node _parent, File _file, Type _type)
   {
      children = new ArrayList<Node>();

      file = _file;
      name = _file.getName();
      parent = _parent;
      type = _type;
      _parent.addChild(this);
   }

   /**
    * delete the Node assigning the children to the parent.
    */
   public void deleteNode()
   {
      for (Node iter : children)
      {
         iter.setParent(parent);
      }
      parent.removeChild(this);
   }

   /**
    * delete the Node and its children, the same as <code> .remove(the Node) </code>.
    * 
    */
   public void deleteNodeAndChildren()
   {
      parent.removeChild(this);
   }

   public void setParent(Node _parent)
   {
      parent = _parent;
   }

   public void addChild(Node _child)
   {
      children.add(_child);
   }

   public void removeChild(Node _child)
   {
      children.remove(_child);
   }

   public List<Node> getChildren()
   {
      return children;
   }

   public Node getParent()
   {
      return parent;
   }

   public String getName()
   {
      return name;
   }

   public Type getType()
   {
      return type;
   }

   public File getFile()
   {
      return file;
   }

   /**
    * The init for the recursive search.
    * 
    * @return the list with "Project" Nodes
    */
   public List<Node> getProjectChildren()
   {
      List<Node> list = new ArrayList<Node>();
      listProjects(this, list);
      return list;
   }

   /**
    * Recursive search for "Project" Nodes, they are stored in <code>list</code>.
    * 
    * @param node the root Node
    * @param list the List for the "Project" Nodes
    */
   private void listProjects(Node node, List<Node> list)
   {
      for (Node iter : node.getChildren())
      {
         if (iter.getType() == Type.PROJECT)
         {
            list.add(iter);
         }
         if (iter.getType() == Type.MODULE)
         {
            listProjects(iter, list);
         }
      }
   }
   
   /**
    * Returns the Node which is equal to <code>equal</code>.
    *  
    * @param equal the to be searched node
    * @return the node
    */
   public Node getEqualNode(Node equal)
   {      
      return searchEqual(equal, this);
   }
   
   private Node searchEqual(Node equal, Node search)
   {    
      Node result = new Node();
      for(Node iter : search.getChildren()){
         if(iter.equals(equal))
            result =  iter;      
         else
         {
            result = searchEqual(equal, iter);
         }
      }
      return result;      
   }

}
