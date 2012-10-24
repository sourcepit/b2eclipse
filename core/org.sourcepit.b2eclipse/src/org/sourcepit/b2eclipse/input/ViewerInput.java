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

import org.sourcepit.b2eclipse.input.node.Node;
import org.sourcepit.b2eclipse.input.node.NodeFolder;
import org.sourcepit.b2eclipse.input.node.NodeModule;
import org.sourcepit.b2eclipse.input.node.NodeModuleProject;
import org.sourcepit.b2eclipse.input.node.NodeProject;
import org.sourcepit.b2eclipse.input.node.NodeProject.ProjectType;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 * The Input for the two Viewers in B2WizardPage.
 * 
 * @author WD
 */
public class ViewerInput
{
   private Node dirViewerRoot;

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

            if (name != null)
            {
               me = new NodeModule(parent, path, name);

               new NodeModuleProject(me, path, name);

               for (File iter : pathList)
               {
                  searchForProjects(me, iter);
               }
               if (me.getChildren().size() == 0)
               {
                  me.deleteNode();
               }
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


   /**
    * Returns false if Projects were found, at allowed positions. The return statement is only used for recursion.
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
         if (name != null)
         {
            Node me = new NodeModule(root, path, name);
            new NodeModuleProject(me, path, name);
            for (File iter : pathList)
            {
               empty = searchForProjects(me, iter);
            }
            if (empty)
            {
               me.deleteNode();
            }
         }
      }
      else
      {
         // Checks if projects are directly under
         for (File iter : pathList)
         {
            if (iter.getName().equals("META-INF"))
            {
               new NodeProject(root, iter.getParentFile(), ProjectType.PDIR);
               empty = false;
            }
         }
         if (empty)
         {
            // Checks ordinary folders for containing projects
            Node parent = new Node();
            for (File currentPath : pathList)
            {
               Boolean exist = false;
               // Checks for the existence of the node
               for (Node currentChild : root.getChildren())
               {
                  if (currentChild.getFile().equals(currentPath.getParentFile()))
                  {
                     parent = currentChild;
                     exist = true;
                  }
               }
               if (!exist)
                  parent = new NodeFolder(root, currentPath.getParentFile());

               for (File content : currentPath.listFiles())
               {
                  if (content.getName().equals("META-INF"))
                  {
                     // denotes the existence of a project under the module
                     new NodeProject(parent, currentPath, ProjectType.PDIR);
                     empty = false;
                  }
               }
            }
            // Checks if projects was found, if not the node will be erased
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
    * Is searching for the <code>artifactId</code> Tag in the module.xml file.
    * 
    * @param xmlPath path to module.xml
    * @return the content of the <code>artifactId</code> tag.
    */
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

         }
         catch (Exception e)
         {
            // ignore
            System.err.println("ERROR XML FILE");
            return null;
         }

      }
      return name;
   }
}
