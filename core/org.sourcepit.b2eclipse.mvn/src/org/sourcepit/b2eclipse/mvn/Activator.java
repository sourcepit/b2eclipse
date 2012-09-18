/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.mvn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.FileLocator;
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
   private String mvnPath;

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

   public void copy()
   {
      try
      {
         final File bundleDir = FileLocator.getBundleFile(getDefault().getBundle());
         final File newBundleDir = Activator.getDefault().getStateLocation().toFile();
         if (bundleDir.exists())
         {
            copyFolder(bundleDir, newBundleDir);
         }
         setMvnPath(newBundleDir.getPath());
      }
      catch (IOException e)
      {
         throw new IllegalStateException(e);
      }
   }

   public void copyFolder(File src, File dest)
   {
      if (src.isDirectory())
      {
         if (!dest.exists())
         {
            dest.mkdir();
         }

         String[] files = src.list();

         for (int i = 0; i < files.length; i++)
         {

            copyFolder(new File(src, files[i]), new File(dest, files[i]));
         }
      }
      else
      {
         try
         {
            InputStream iStream = new FileInputStream(src);
            OutputStream oStream = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = iStream.read(buffer)) > 0)
            {
               oStream.write(buffer, 0, length);
            }

            iStream.close();
            oStream.close();
         }
         catch (IOException e)
         {
            throw new IllegalStateException(e);
         }


      }
   }

   private void setMvnPath(String mvnPath)
   {
      this.mvnPath = mvnPath;
   }

   public String getMvnPath()
   {
      return mvnPath;
   }

}
