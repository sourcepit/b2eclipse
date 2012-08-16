/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */

public class Activator extends AbstractUIPlugin
{
   // The plug-in ID
   private static final String MODULE_ID = "org.sourcepit.b2eclipse"; //$NON-NLS-1$

   // The shared instance
   private static Activator module;

   /**
    * The constructor
    */
   public Activator()
   {
   }

   public void start(BundleContext context) throws Exception
   {
      super.start(context);
      module = this;

   }

   public void stop(BundleContext context) throws Exception
   {
      module = null;
      super.stop(context);
   }

   /**
    * Returns the shared instance
    * 
    * @return the shared instance
    */
   public static Activator getDefault()
   {
      return module;
   }

   public static void error(Throwable t)
   {
      error(t.getLocalizedMessage(), t);
   }

   public static void error(String msg, Throwable t)
   {
      if (t instanceof CoreException)
      {
         log(((CoreException) t).getStatus());
      }
      else
      {
         log(new Status(IStatus.ERROR, MODULE_ID, msg, t));
      }
   }

   public static void log(IStatus status)
   {
      getDefault().getLog().log(status);
   }

   /**
    * @param path image path
    * @return {@link Image}
    */
   public static Image getImageFromPath(String path)
   {
      Image image = module.getImageRegistry().get(path);
      if (image == null)
      {
         ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(module.getBundle().getSymbolicName(),
            path);
         if (descriptor != null)
         {
            module.getImageRegistry().put(path, descriptor);
            image = module.getImageRegistry().get(path);
         }
      }
      return image;
   }


}
