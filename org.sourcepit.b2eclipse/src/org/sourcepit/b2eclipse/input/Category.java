/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.input;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Category
{
   private String name;
   private List<File> plugins = new ArrayList<File>();
   
   public String getName() {
       return name;
   }
   public void setName(String name) {
       this.name = name;
   }
   
   public List<File> getPlugins (){
       return plugins;
   }

}
