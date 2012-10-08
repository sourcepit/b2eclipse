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
      MODULE, PROJECT, WORKINGSET
   }

   /**
    * default Constructor
    */
   public Node()
   {
      children = new ArrayList<Node>();
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
    * Creates a new Node under the <code>_parent</code> Node. Only for WORKINGSET Nodes.
    * 
    * @param _parent the parent element
    * @param _file the given File
    * @param _type the given Type
    */
   public Node(Node _parent, File _file, Type _type, String _name)
   {
      children = new ArrayList<Node>();
      file = _file;
      name = _name;
      parent = _parent;
      type = _type;
      _parent.addChild(this);
   }

   /**
    * delete the Node assigning the children to the parent.
    */
   public void deleteNodeAssigningChildrenToParent()
   {
      for (Node iter : children)
      {
         iter.setParent(parent);
         parent.addChild(iter);
      }
      parent.removeChild(this);
   }

   /**
    * delete the Node and its children, the same as <code> .remove(the Node) </code>.
    * 
    */
   public void deleteNode()
   {
      if(parent != null){
         parent.removeChild(this);
      }
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
   
   public void setName(String _name)
   {
      name = _name;
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
    * Returns the Node which is equal to <code>_file</code>.
    * Checks the file field. Searches recursive through the children. 
    * 
    * @param file 
    * @return the node
    */
   public Node getEqualNode(File _file)
   {
      return searchEqual(_file, this);
   }

   /**
    * Checks only the equality of the <code>file</code> field.
    * 
    * @param equal
    * @param search
    * @return
    */
   private Node searchEqual(File equal, Node search)
   {
      Node result = null;

      for (Node iter : search.getChildren())
      {

         if (iter.getFile().equals(equal))

            return iter;

         else
         {
            result = searchEqual(equal, iter);
            if (result != null)
            {
               return result;
            }
         }
      }
      return result;
   }

   /**
    * The existing Parent, which is a representation of a Model, not the "abstract" root. If its already the rootModel
    * returns null.
    * 
    * @return the root Model
    */
   public Node getRootModel()
   {
      Node result = null;
      if (parent == null)
      {
         return null;
      }
      if (parent.getParent() == null)
         result = this;
      else
      {
         result = parent.getRootModel();
         if (result != null)
            return result;
      }
      return result;
   }
   
   /**
    * Returns the Name for a Working Set.
    * @param node
    * @return
    */
   public String getWSName(Node node)
   {
      String name = "";
      Node mod = node.getRootModel();
      while (node != mod)
      {
         name = "/" + node.getName() + name;
         node = node.getParent();
      }
      name = node.getRootModel().getName() + name;
      return name;
   }
}
