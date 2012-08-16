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
   private int height = 0;
   private boolean result = false;

   public FilePropertyTester()
   {
   }

   @Override
   public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
   {

      if (PROPERTY_CONTAINS_FILE.equals(property))
      {
         File folder = new File(((IProject) receiver).getLocation().toOSString());
         doModuleSearch(folder);

      }
      return result;
   }

   public void doModuleSearch(File file)
   {

      if (height != 2)
      {
         File[] elementList = file.getParentFile().listFiles();
         fileList.clear();

         for (File i : elementList)
         {
            fileList.add((i.getName()));
         }

         if (fileList.contains("module.xml"))
         {
            for (File element : elementList)
            {
               if (element.getName().equals("module.xml"))
               {
                  result = true;

               }
            }
         }
         height++;
         doModuleSearch(file.getParentFile());
      }


   }


}
