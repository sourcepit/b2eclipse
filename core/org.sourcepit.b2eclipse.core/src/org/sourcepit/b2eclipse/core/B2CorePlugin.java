/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class B2CorePlugin extends Plugin implements BundleActivator
{
   private static B2CorePlugin plugin;
   private BundleContext bundleContext;

   public B2CorePlugin()
   {
      plugin = this;
   }

   public static B2CorePlugin getDefault()
   {
      return plugin;
   }

   @Override
   public void start(BundleContext context) throws Exception
   {
      super.start(context);
      this.bundleContext = context;
   }

   @Override
   public void stop(BundleContext context) throws Exception
   {
      super.stop(context);
      bundleContext = null;
      plugin = null;
   }

   /**
    * Returns a service with the specified name or <code>null</code> if none.
    * 
    * @param serviceName name of service
    * @return service object or <code>null</code> if none
    */
   public <T> T acquireService(Class<T> serviceType)
   {
      final ServiceReference<T> reference = bundleContext.getServiceReference(serviceType);
      if (reference == null)
         return null;
      T service = bundleContext.getService(reference);
      if (service != null)
      {
         bundleContext.ungetService(reference);
      }
      return service;
   }

}
