/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.core;

import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.lang.reflect.InvocationTargetException;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.embedder.ICallable;
import org.eclipse.m2e.core.embedder.IMaven;

public class DefaultMavenContext implements MavenContext
{
   private final PlexusContainer plexusContainer;
   private final IMaven maven;

   public DefaultMavenContext(IMaven maven)
   {
      this.plexusContainer = getPlexusContainer(maven);
      this.maven = maven;
   }

   static PlexusContainer getPlexusContainer(final IMaven maven)
   {
      try
      {
         try
         {
            return (PlexusContainer) maven.getClass().getMethod("getPlexusContainer").invoke(maven);
         }
         catch (InvocationTargetException e)
         {
            throw e.getCause();
         }
      }
      catch (Exception e)
      {
         throw pipe(e);
      }
      catch (Error e)
      {
         throw pipe(e);
      }
      catch (Throwable e)
      {
         throw pipe(new IllegalStateException(e));
      }
   }

   @Override
   public PlexusContainer getPlexusContainer()
   {
      return plexusContainer;
   }

   @Override
   public <T> T lookup(Class<T> clazz)
   {
      try
      {
         return plexusContainer.lookup(clazz);
      }
      catch (ComponentLookupException e)
      {
         throw pipe(e);
      }
   }

   @Override
   public <T> T lookup(Class<T> clazz, String hint)
   {
      try
      {
         return plexusContainer.lookup(clazz, hint);
      }
      catch (ComponentLookupException e)
      {
         throw pipe(e);
      }
   }

   @Override
   public <V> V execute(boolean offline, boolean forceDependencyUpdate, ICallable<V> callable, IProgressMonitor monitor)
   {
      try
      {
         return maven.execute(offline, forceDependencyUpdate, callable, monitor);
      }
      catch (CoreException e)
      {
         throw pipe(e);
      }
   }

   @Override
   public <V> V execute(ICallable<V> callable, IProgressMonitor monitor)
   {
      try
      {
         return maven.execute(callable, monitor);
      }
      catch (CoreException e)
      {
         throw pipe(e);
      }
   }

}
