/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.input;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 * @author WD
 */
public class ViewerInput
{
   public enum Mode
   {
      STRUCTURED, SIMPLE
   }

   Node dirViewerRoot;

   public ViewerInput(Node _root)
   {
      dirViewerRoot = _root;
   }

   /**
    * Creates the Node system that represents the Modules and Projects.
    * 
    * @param path the Path that should be searched
    */
   public Node createMainNodeSystem(File path)
   {
      localizeFiles(path, dirViewerRoot);
      return dirViewerRoot;
   }

   /**
    * Recursive search for the the Modules and Projects.
    * 
    * @param path the path that should be searched
    * @param parent the root Node
    */
   private void localizeFiles(File path, Node parent)
   {
      List<File> pathList = new ArrayList<File>();
      List<String> fileList = new ArrayList<String>();

      Node me = parent;

      if (path.listFiles() != null)
      {
         for (File iter : path.listFiles())
         {
            if (checkDir(iter))
            {
               pathList.add(iter);
            }
            if (iter.isFile())
            {
               fileList.add(iter.getName());
            }
         }

         if (fileList.contains("module.xml"))
         {
            String name = loadModuleXml(path);

            me = new Node(parent, path, Node.Type.MODULE, name);

            for (File iter : pathList)
            {
               searchForProjects(me, iter);
            }
            if (me.getChildren().size() == 0)
            {
               me.deleteNode();
            }

            return;
         }
         else
         {
            for (File iter : pathList)
            {
               localizeFiles(iter, me);
            }
         }
      }
   }

   private String loadModuleXml(File xmlPath)
   {
      String name = xmlPath.getName();

      File xmlFile = new File(xmlPath.getPath() + "/module.xml");
      if (xmlFile.exists() && xmlFile.canRead())
      {
         try
         {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            NodeList nodes = doc.getElementsByTagName("artifactId");

            for (int i = 0; i < nodes.getLength(); i++)
            {
               if (nodes.item(i).getParentNode().equals(doc.getElementsByTagName("project").item(0)))
                  name = nodes.item(i).getTextContent();
            }

            if (nodes.getLength() > 0)
            {
               // TODO was wenn mehrere?
               // -->mommentan wird immer die erste genommen
            }
         }
         catch (Exception e)
         {
            // ignore
         }
      }
      return name;
   }

   /**
    * Returns false if Projects were found, at allowed positions. The return statement is only useful for recursion.
    * 
    * @param root the model root Node
    * @param path the path where should be searched (should be direct under the Model)
    * 
    * @return empty or not?
    */
   private boolean searchForProjects(Node root, File path)
   {
      List<File> pathList = new ArrayList<File>();
      List<String> fileList = new ArrayList<String>();

      Boolean empty = true;

      for (File iter : path.listFiles())
      {
         if (checkDir(iter))
         {
            pathList.add(iter);
         }
         if (iter.isFile())
         {
            fileList.add(iter.getName());
         }
      }

      if (fileList.contains("module.xml"))
      {
         String name = loadModuleXml(path);
         Node me = new Node(root, path, Node.Type.MODULE, name);
         for (File iter : pathList)
         {
            empty = searchForProjects(me, iter);
         }
         if (empty)
         {
            me.deleteNode();
         }
      }
      else
      {
         // Check ob Projekte direkt drunter
         for (File iter : pathList)
         {
            if (iter.getName().equals("META-INF"))
            {
               new Node(root, iter.getParentFile(), Node.Type.PROJECT);
               empty = false;
            }
         }
         if (empty)
         {
            // Check ob "normale" Ordner ein Projekt drunter haben
            Node parent = new Node();
            for (File currentPath : pathList)
            {
               Boolean exist = false;
               // Check ob es den Node bereits gibt
               for (Node currentChild : root.getChildren())
               {
                  if (currentChild.getFile().equals(currentPath.getParentFile()))
                  {
                     parent = currentChild;
                     exist = true;
                  }
               }
               if (!exist)
                  parent = new Node(root, currentPath.getParentFile(), Node.Type.MODULE);

               for (File content : currentPath.listFiles())
               {
                  if (content.getName().equals("META-INF"))
                  {
                     // heist darunter ist ein Projekt
                     new Node(parent, currentPath, Node.Type.PROJECT);
                     empty = false;
                  }
               }
            }
            // Check ob Projekte gefunden wurden, ansonsten wird der Node gelöscht.
            if (parent.getChildren().size() == 0)
            {
               parent.deleteNode();
            }
         }
      }
      return empty;
   }


   /**
    * Checks which directories are forbidden or uninteresting.
    * 
    * @param dir the checked directory
    * @return true if directory is interesting, else false.
    */
   private boolean checkDir(File dir)
   {
      // Feel free to add more restrictions
      if (dir.isDirectory() && !dir.getName().equals(".metadata") && !dir.getName().equals("target")
         && !dir.getName().equals(".git") && !dir.getName().equals(".b2"))
         return true;
      else
         return false;
   }

   /**
    * Returns a initial Node System that is representing the Preview
    * 
    * @return the Node (system)
    */
   public Node createNodeSystemForPreview(Mode mode)
   {
      Node preViewerRoot = new Node();

      List<Node> parentList = new ArrayList<Node>();
      for (Node i : dirViewerRoot.getChildren())
      {
         Node ws = preViewerRoot;
         if (mode == Mode.SIMPLE)
            ws = new Node(preViewerRoot, i.getFile(), Node.Type.WORKINGSET, i.getName());

         for (Node j : i.getProjectChildren())
         {
            if (mode == Mode.SIMPLE)
               new Node(ws, j.getFile(), j.getType());
            if (mode == Mode.STRUCTURED)
            {
               if (!parentList.contains(j.getParent()))
               {
                  parentList.add(j.getParent());
                  ws = new Node(preViewerRoot, j.getParent().getFile(), Node.Type.WORKINGSET,
                     j.getWSName(j.getParent()));
               }
               new Node(ws, j.getFile(), j.getType());
            }
         }
      }
      return preViewerRoot;
   }


}
