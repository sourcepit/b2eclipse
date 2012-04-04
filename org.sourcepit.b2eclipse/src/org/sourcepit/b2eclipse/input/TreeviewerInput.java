/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.input;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class TreeviewerInput
{

   private File[] elementList;
   private File[] projects;
   private static ArrayList<File> projectFileList = new ArrayList<File>();
   private static List<Category> categories;
   private  Category categoryModules,categoryTests,categoryDocs;

   public TreeviewerInput(){};
   
   public TreeviewerInput(Object inputElement)
   {

      getProjects(inputElement);

      projects = new File[projectFileList.size()];
      for (int y = 0; y < projects.length; y++)
      {
         projects[y] = projectFileList.get(y);


      }
   }
   

   public List<Category> getData()
   {
      categories = new ArrayList<Category>();
      categoryModules = new Category();
      categoryModules.setName("Plugins");
      categories.add(categoryModules);

      categoryTests = new Category();
      categoryTests.setName("Tests");
      categories.add(categoryTests);

      categoryDocs = new Category();
      categoryDocs.setName("Docs");
      categories.add(categoryDocs);

      for (int i = 0; i < projects.length; i++)
      {
         if (projects[i].getParent().endsWith(".module"))
         {
            categoryModules.getPlugins().add(projects[i]);
         }
         else if (projects[i].getParent().endsWith(".tests"))
         {
            categoryTests.getPlugins().add(projects[i]);
         }
         else if (projects[i].getParent().endsWith(".doc"))
         {
            categoryDocs.getPlugins().add(projects[i]);
         }
      }


      return categories;
   }

   private ArrayList<File> getProjects(Object inputElement)
   {


      elementList = ((File) inputElement).listFiles();

      if (elementList != null)
      {

         for (File file : elementList)
         {
            if (file.isDirectory())
            {
               getProjects(file);
            }
            else if (file.getName().endsWith(".project"))
            {
               projectFileList.add(file.getAbsoluteFile());
            }

         }
      }
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
   
   public ArrayList<File> getProjects()
   {
      return projectFileList;
   }
   
   public static List<Category> getCategories(){
      
      return categories;
   }

}
