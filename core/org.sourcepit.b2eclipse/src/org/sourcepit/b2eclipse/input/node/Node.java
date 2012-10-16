/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.input.node;

import java.io.File;
import java.util.ArrayList;


/**
 * Class that represents the elements(Nodes).
 * 
 * @author WD
 */
public class Node
{
   protected String name;
   protected File file;
   protected Node parent;
   protected ArrayList<Node> children;
   
   
//   protected Type type;
//
//   public static enum Type
//   {
//      MODULE, PROJECT, WORKINGSET, FOLDER
//   }

   public Node()
   {
      children = new ArrayList<Node>();
   }

   /**
    * Creates a new Node under the <code>_parent</code> Node.
    * 
    * @param _parent
    * @param _file
    * @param _type
    */
//   public Node(Node _parent, File _file, Type _type)
//   {
//      children = new ArrayList<Node>();
//      file = _file;
//      name = _file.getName();
//      parent = _parent;
//      type = _type;
//      _parent.addChild(this);
//   }

   /**
    * Creates a new Node under the <code>_parent</code> Node. Is mainly used for WS and Modules. Where the Name is
    * different to File.
    * 
    * @param _parent
    * @param _file
    * @param _type
    * @param _name
    */
//   public Node(Node _parent, File _file, Type _type, String _name)
//   {
//      children = new ArrayList<Node>();
//      file = _file;
//      name = _name;
//      parent = _parent;
//      type = _type;
//      _parent.addChild(this);
//   }

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
    * Deletes the Node and its children.
    */
   public void deleteNode()
   {
      if (parent != null)
      {
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

   public ArrayList<Node> getChildren()
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

//   public Type getType()
//   {
//      return type;
//   }

   public File getFile()
   {
      return file;
   }

   /**
    * The init for the recursive search.
    * 
    * @return the list with "Project" Nodes
    */
   public ArrayList<Node> getProjectChildren()
   {
      ArrayList<Node> list = new ArrayList<Node>();
      listProjects(this, list);
      return list;
   }

   /**
    * Recursive search for "Project" Nodes, they are stored in <code>list</code>.
    * 
    * @param node the root Node
    * @param list the List for the "Project" Nodes
    */
   protected void listProjects(Node node, ArrayList<Node> list)
   {
      for (Node iter : node.getChildren())
      {
         if (iter instanceof NodeProject)
         {
            list.add(iter);
         }
         else
         {
            listProjects(iter, list);
         }
      }
   }

   /**
    * Returns the Node which is equal to <code>_file</code>. Checks the file field. Searches recursive through the
    * children.
    * 
    * @param _file
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
   protected Node searchEqual(File equal, Node search)
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
   public Node getRootModule()
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
         result = parent.getRootModule();
         if (result != null)
            return result;
      }
      return result;
   }
   
   public ArrayList<Node> getAllSubNodes()
   {
      ArrayList<Node> tree = new ArrayList<Node>();
      search(tree, this);      
      return tree;
   }
   
   protected void search(ArrayList<Node> tree, Node node)
   {
      for (Node iter : node.getChildren())
      {
         tree.add(iter);
         search(tree, iter);
      }
   }
}
