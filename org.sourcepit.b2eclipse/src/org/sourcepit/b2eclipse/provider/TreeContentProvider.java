/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.provider;


import java.io.File;
import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TreeContentProvider implements ITreeContentProvider
{

   private static ArrayList<File> projectFileList = new ArrayList<File>();;
   private File[] projects;
   private File[] elementList;


   @Override
   public void dispose()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
   {

   }

   /**
    * setInput() Methode welche rekursiv nach .project Dateien sucht
    * 
    * @return und diese Projekte dann zurückgibt
    */
   @Override
   public File[] getElements(Object inputElement)
   {
      elementList = ((File) inputElement).listFiles();
      if (elementList != null)
      {

         for (File file : elementList)
         {
            if (file.isDirectory())
            {
               getElements(file);
            }
            else if (file.getName().endsWith(".project"))
            {
               projectFileList.add(file.getAbsoluteFile());
            }

         }

         projects = new File[projectFileList.size()];
         for (int y = 0; y < projects.length; y++)
         {
            projects[y] = projectFileList.get(y);

         }

      }
      return projects;
   }


   @Override
   public Object[] getChildren(Object parentElement)
   {
      return null;

   }


   @Override
   public Object getParent(Object element)
   {
      return null;

   }

   @Override
   public boolean hasChildren(Object element)

   {

      return false;

   }

   public ArrayList<File> getProjects()
   {
      return projectFileList;
   }


   /**
    * falls andere Projekte ausgewählt werden, so werden die alten Einträge in der ArrayList verworfen
    * 
    * @return gibt leere Projektliste zurück
    */
   public static ArrayList<File> clearArrayList()
   {
      projectFileList.clear();

      return projectFileList;
   }


}
