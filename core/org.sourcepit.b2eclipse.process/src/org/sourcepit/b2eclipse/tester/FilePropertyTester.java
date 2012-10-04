/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.tester;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */

public class FilePropertyTester extends PropertyTester
{
   private static final String PROPERTY_CONTAINS_FILE = "containsFile";
   private List<String> fileList = new ArrayList<String>();
   private boolean result = false;

   public FilePropertyTester()
   {
   }

   @Override
   public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
   {

      if (PROPERTY_CONTAINS_FILE.equals(property))
      {
         File selectedProject = new File(((IProject) receiver).getLocation().toOSString());
         if (expectedValue instanceof String)
         {
            result = doModuleSearch(selectedProject, (String) expectedValue);
         }


      }
      return result;
   }

   private boolean doModuleSearch(File file, String expectedValue)
   {
      try
      {
         File[] elementList = file.getParentFile().listFiles();
         fileList.clear();

         for (File fileElement : elementList)
         {
            fileList.add((fileElement.getName()));
         }

         if (fileList.contains(expectedValue))
         {
            for (File element : elementList)
            {
               if (element.getName().equals(expectedValue))
               {
                  return true;

               }
            }
         }
         else
         {
            return doModuleSearch(file.getParentFile(), expectedValue);
         }
      }
      catch (NullPointerException e)
      {
         return false;
      }
      return false;

   }

}
