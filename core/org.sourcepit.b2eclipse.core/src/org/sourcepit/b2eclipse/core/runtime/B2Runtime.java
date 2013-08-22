/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.core.runtime;

import java.io.File;
import java.util.List;
import java.util.Properties;

public class B2Runtime
{
   private final List<File> files;
   private final Properties pomProperties;

   public B2Runtime(List<File> files, Properties pomProperties)
   {
      this.files = files;
      this.pomProperties = pomProperties;
   }

   public List<File> getFiles()
   {
      return files;
   }

   public String getVersion()
   {
      return pomProperties.getProperty("version");
   }
}
