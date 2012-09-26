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
 * @author WD
 */

public class TreeViewerInput extends ViewerInput
{ 
   public TreeViewerInput(Node _root, File path)
   {
      abstractRoot = _root; 
      createMainNodeSystem(path);
   }
   
   /**
    * Creates the Node system that represents the Modules and Projects.
    * 
    * @param path the Path that should be searched
    */
   public void createMainNodeSystem(File path)
   { 
      localizeFiles(path, abstractRoot);
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
            me = new Node(parent, path, Node.Type.MODULE);
         }

         if (fileList.contains("MANIFEST.MF"))
         {            
            new Node(parent, path.getParentFile(), Node.Type.PROJECT);
         }

         for (File iter : pathList)
         {
            localizeFiles(iter, me);
         }
      }
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
         && !dir.getName().equals(".git"))
         return true;
      else
         return false;
   }
   
}
