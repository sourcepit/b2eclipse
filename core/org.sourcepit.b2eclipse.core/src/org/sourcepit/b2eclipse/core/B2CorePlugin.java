/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class B2CorePlugin extends Plugin implements BundleActivator
{
   private static B2CorePlugin plugin;

   public B2CorePlugin()
   {
      plugin = this;
   }

   public static B2CorePlugin getDefault()
   {
      return plugin;
   }

   @Override
   public void stop(BundleContext context) throws Exception
   {
      super.stop(context);
      plugin = null;
   }

}
