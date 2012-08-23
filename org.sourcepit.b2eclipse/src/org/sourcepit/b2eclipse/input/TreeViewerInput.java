/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.input;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */

public class TreeViewerInput
{


   private List<File> projectList = new ArrayList<File>();
   private List<String> dirContentList = new ArrayList<String>();
   private List<ParentCategory> parentCategories = new ArrayList<ParentCategory>();
   private List<String> fileList = new ArrayList<String>();
   private Map<String, ArrayList<String>> moduleMap = new HashMap<String, ArrayList<String>>();
   boolean result = false;

   public TreeViewerInput()
   {
   };

   public TreeViewerInput(Object inputElement)
   {
      File[] elementList = ((File) inputElement).listFiles();

      if (elementList != null)
      {
         getProjects(inputElement);
      }

   }

   public List<ParentCategory> getData()
   {
      if (projectList != null)
      {
         moduleRelatedProjects();
         Iterator<String> it = getModuleMap().keySet().iterator();
         while (it.hasNext())
         {
            SubCategory categoryPlugins = new SubCategory();
            categoryPlugins.setName("Plugins");

            SubCategory categoryTests = new SubCategory();
            categoryTests.setName("Tests");

            SubCategory categoryDocs = new SubCategory();
            categoryDocs.setName("Docs");

            final String parentName = it.next();
            ParentCategory parentCategory = new ParentCategory();
            parentCategory.setName(parentName);


            final List<String> projectPaths = getModuleMap().get(parentName);
            for (String projectPath : projectPaths)
            {
               File file = new File(projectPath);
               if (!file.getParent().endsWith(".tests") && !new File(file.getParent()).getParent().endsWith("tests")
                  && !file.getParent().endsWith(".doc") && !new File(file.getParent()).getParent().endsWith("doc"))
               {
                  categoryPlugins.getFileEntries().add(file);
               }
               if (file.getParent().endsWith(".tests") || new File(file.getParent()).getParent().endsWith("tests"))
               {
                  categoryTests.getFileEntries().add(file);
               }
               if (file.getParent().endsWith(".doc") || new File(file.getParent()).getParent().endsWith("doc"))
               {
                  categoryDocs.getFileEntries().add(file);
               }
            }
            parentCategory.getCategoryEntries().add(categoryPlugins);
            parentCategory.getCategoryEntries().add(categoryTests);
            parentCategory.getCategoryEntries().add(categoryDocs);
            parentCategories.add(parentCategory);

         }
      }

      return parentCategories;
   }

   private List<File> getProjects(Object inputElement)
   {

      File[] elementList = ((File) inputElement).listFiles();
      dirContentList.clear();
      if (elementList != null)
      {
         for (File file : elementList)
         {
            setDirList(file.getName());
         }

         if ((!(dirContentList.contains("module.xml")) && !(dirContentList.contains(".project"))))
         {
            doDirectorySearch(elementList);
         }
         else if (!(dirContentList.contains("module.xml")) && dirContentList.contains(".project") && result)
         {
            result = false;
            doProjectSearch(elementList);
            result = true;
         }
         else if (!(dirContentList.contains("module.xml")) && dirContentList.contains(".project"))
         {
            String name = null;
            for (File file : elementList)
            {
               if (file.getName().equals(".project"))
               {
                  name = doParentSearch(file);
                  if (name != null)
                  {
                     doProjectSearch(elementList);
                  }
               }
            }
         }
         else if ((dirContentList.contains("module.xml") && !(dirContentList.contains(".project")))
            || (dirContentList.contains("module.xml") && dirContentList.contains(".project")))
         {
            result = true;
            doDirectorySearch(elementList);
            result = false;
         }
         return projectList;
      }

      return null;

   }

   public void clearArrayList()
   {
      projectList.clear();
   }


   public List<ParentCategory> getCategories()
   {
      return parentCategories;
   }

   private void setDirList(String file)
   {
      dirContentList.add(file);
   }

   private void doDirectorySearch(File[] elementList)
   {

      for (File file : elementList)
      {
         if (file.isDirectory() && !(file.getName().startsWith(".")) && !(file.getName().equals("target")))
         {
            getProjects(file);
         }
      }
   }

   private void doProjectSearch(File[] elementList)
   {
      for (File file : elementList)
      {
         if (file.getName().equals(".project"))
         {
            projectList.add(file.getAbsoluteFile());
         }
      }

   }

   private String doParentSearch(File file)
   {
      if (file.getParentFile() != null)
      {
         File[] elementList = file.getParentFile().listFiles();
         fileList.clear();

         for (File fileElement : elementList)
         {
            addFiletoFilelist(fileElement.getName());
         }

         if (fileList.contains("module.xml"))
         {
            for (File element : elementList)
            {
               if (element.getName().equals("module.xml"))
               {
                  return element.getParentFile().getName();
               }
            }
         }

         return doParentSearch(file.getParentFile());
      }
      return null;


   }

   private void moduleRelatedProjects()
   {
      for (File file : projectList)
      {
         String result = doParentSearch(file);
         if (result != null)
         {
            if (moduleMap.containsKey(result))
            {

               ArrayList<String> b = moduleMap.get(result);
               b.add(file.getAbsolutePath());
               moduleMap.put(result, b);

            }
            else
            {
               ArrayList<String> dummyList = new ArrayList<String>();
               dummyList.add(file.getAbsolutePath());
               moduleMap.put(result, dummyList);
            }
         }

      }
   }

   private Map<String, ArrayList<String>> getModuleMap()
   {
      return moduleMap;
   }

   private void addFiletoFilelist(String file)
   {
      fileList.add(file);
   }

}
