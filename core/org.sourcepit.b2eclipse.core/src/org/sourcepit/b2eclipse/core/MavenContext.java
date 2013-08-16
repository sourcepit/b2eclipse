/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.core;

import org.codehaus.plexus.PlexusContainer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.embedder.ICallable;

public interface MavenContext
{
   PlexusContainer getPlexusContainer();

   <T> T lookup(Class<T> clazz);

   <T> T lookup(Class<T> clazz, String hint);

   <V> V execute(boolean offline, boolean forceDependencyUpdate, ICallable<V> callable, IProgressMonitor monitor);

   <V> V execute(ICallable<V> callable, IProgressMonitor monitor);
}
