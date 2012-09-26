/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.input;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */

public class ParentCategory implements Category
{
   private String categoryName;
   private List<SubCategory> categoryEntriesList = new ArrayList<SubCategory>();

   public String getName()
   {
      return categoryName;
   }

   public void setName(String name)
   {
      if ("".equals(name))
      {
         throw new IllegalArgumentException();
      }
      else
      {
         this.categoryName = name;
      }

   }

   public List<SubCategory> getCategoryEntries()
   {
      return categoryEntriesList;
   }
}