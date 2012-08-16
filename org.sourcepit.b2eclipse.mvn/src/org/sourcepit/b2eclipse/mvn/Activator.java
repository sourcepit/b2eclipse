/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.mvn;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */

/**
 * The activator class controls the plug-in life cycle
 */

public class Activator extends AbstractUIPlugin
{

   // The plug-in ID
   public static final String PLUGIN_ID = "org.sourcepit.b2eclipse.mvn"; //$NON-NLS-1$

   // The shared instance
   private static Activator plugin;

   /**
    * The constructor
    */
   public Activator()
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
    */
   public void start(BundleContext context) throws Exception
   {
      super.start(context);
      plugin = this;

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
    */
   public void stop(BundleContext context) throws Exception
   {
      plugin = null;
      super.stop(context);
   }

   /**
    * Returns the shared instance
    * 
    * @return the shared instance
    */
   public static Activator getDefault()
   {
      return plugin;
   }

   public void extractPlugin()
   {
      String pluginPath = Platform.getInstallLocation().getURL().getPath() + "plugins/" + getBundle().getSymbolicName()
         + "_" + getBundle().getVersion() + ".jar";
      String destDirPath = Platform.getInstallLocation().getURL().getPath() + "plugins/b2-maven";
      File destDir = new File(destDirPath);
      File plugin = new File(pluginPath);

      if (plugin.exists() && !(destDir.exists()))
      {
         try
         {
            JarFile jarFile = new JarFile(plugin);
            Enumeration<?> files = jarFile.entries();
            byte[] buffer = new byte[20000];
            int len;
            while (files.hasMoreElements())
            {
               JarEntry entry = (JarEntry) files.nextElement();

               String entryFileName = entry.getName();

               File dir = buildDirectoryHierarchy(entryFileName, destDir);
               if (!dir.exists())
               {
                  dir.mkdirs();
               }

               if (!entry.isDirectory())
               {
                  BufferedInputStream buffInputStream = new BufferedInputStream(jarFile.getInputStream(entry));
                  BufferedOutputStream buffOutputStream = new BufferedOutputStream(new FileOutputStream(new File(
                     destDir, entryFileName)));

                  while ((len = buffInputStream.read(buffer)) > 0)
                  {
                     buffOutputStream.write(buffer, 0, len);
                  }

                  buffOutputStream.flush();
                  buffOutputStream.close();
                  buffInputStream.close();
               }
            }

         }
         catch (IOException e)
         {
            throw new IllegalStateException(e);
         }

      }
   }

   private File buildDirectoryHierarchy(String entryName, File destDir)
   {
      int lastIndex = entryName.lastIndexOf('/');
      String internalPathToEntry = entryName.substring(0, lastIndex + 1);
      return new File(destDir, internalPathToEntry);
   }


}
