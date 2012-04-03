/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.comparator;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File>
{

   @Override
   public int compare(File o1, File o2)
   {
      if(o1.getParent().endsWith(".module") && o2.getParent().endsWith(".doc")){
         return -1;
      }
      
      if(o1.getParent().endsWith(".module") && o2.getParent().endsWith(".tests")){
         return -1;
      }
      
      if(o1.getParent().endsWith(".module") && o2.getParent().endsWith(".module")){
         return 0;
      }
      
      if(o1.getParent().endsWith(".tests") && o2.getParent().endsWith(".module")){
         return 1;
      }
      
      if(o1.getParent().endsWith(".tests") && o2.getParent().endsWith(".doc")){
         return -1;
      }
      
      if(o1.getParent().endsWith(".tests") && o2.getParent().endsWith(".tests")){
         return 0;
      }
      
      if(o1.getParent().endsWith(".doc") && o2.getParent().endsWith(".tests")){
         return 1;
      }
      
      if(o1.getParent().endsWith(".doc") && o2.getParent().endsWith(".module")){
         return 1;
      }
      
      if(o1.getParent().endsWith(".doc") && o2.getParent().endsWith(".doc")){
         return 0;
      }
      
      return 0;
   }
   
   

}
