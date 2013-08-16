/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.core.runtime;

import static java.util.Collections.singletonList;
import static org.sourcepit.common.utils.io.IO.buffIn;
import static org.sourcepit.common.utils.io.IO.fileIn;
import static org.sourcepit.common.utils.io.IO.read;
import static org.sourcepit.common.utils.io.IO.zipIn;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcepit.common.utils.io.IOHandle;
import org.sourcepit.common.utils.io.Read.FromStream;
import org.sourcepit.common.utils.lang.PipedException;

public class B2RuntimeUtils
{
   static final Logger LOG = LoggerFactory.getLogger(B2RuntimeUtils.class);

   public static void getB2Version(File mavenDir)
   {
      LOG.info("Hello :-");
      final List<B2Runtime> b2Runtimes = getB2Runtimes(mavenDir);
      
      b2Runtimes.toArray();
   }

   static List<B2Runtime> getB2Runtimes(File mavenDir)
   {
      final List<B2Runtime> b2Runtimes = new ArrayList<B2Runtime>();
      final File extDir = new File(mavenDir, "lib/ext");
      extDir.listFiles(new FileFilter()
      {
         @Override
         public boolean accept(File file)
         {
            final String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".jar"))
            {
               try
               {
                  final Properties pomProperties = readB2PomProperties(file);
                  b2Runtimes.add(new B2Runtime(singletonList(file), pomProperties));
               }
               catch (FileNotFoundException e)
               { // no b2 bootstrapper jar
               }
               catch (IOException e)
               {
                  LOG.error("Failed to investigate B2 bootstrapper JAR {0}", e, file.getPath());
               }
            }
            return false;
         }
      });
      return b2Runtimes;
   }

   static Properties readB2PomProperties(File file) throws IOException
   {
      final IOHandle<? extends InputStream> resource = zipIn(buffIn(fileIn(file)),
         "META-INF/maven/org.sourcepit.b2/b2-bootstrapper/pom.properties");

      final FromStream<Properties> fromStream = new FromStream<Properties>()
      {
         @Override
         public Properties read(InputStream inputStream) throws IOException
         {
            final Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
         }
      };

      try
      {
         return read(fromStream, resource);
      }
      catch (PipedException e)
      {
         e.adaptAndThrow(IOException.class);
         throw e;
      }
   }
}
