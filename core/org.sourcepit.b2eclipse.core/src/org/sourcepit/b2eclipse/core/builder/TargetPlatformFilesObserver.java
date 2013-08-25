/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.core.builder;

import static org.eclipse.core.resources.IResource.FILE;
import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.LoadTargetDefinitionJob;

public class TargetPlatformFilesObserver
{
   private final ITargetPlatformService tpService;

   public TargetPlatformFilesObserver(ITargetPlatformService targetPlatformService)
   {
      this.tpService = targetPlatformService;
   }

   public void resourceChanged(final IResourceDelta delta)
   {
      final Collection<IFile> changedTPFiles = getChangedTargetPlatformFiles(delta);
      if (!changedTPFiles.isEmpty())
      {
         final ITargetHandle activeTPHandle = getWorkspaceTargetPlatformHandle(tpService);
         if (activeTPHandle != null && containsTargetPlatform(changedTPFiles, activeTPHandle))
         {
            loadTargetPlatform(activeTPHandle);
         }
      }
   }

   private static void loadTargetPlatform(final ITargetHandle tpHandle)
   {
      try
      {
         LoadTargetDefinitionJob.load(tpHandle.getTargetDefinition());
      }
      catch (CoreException e)
      {
         // TODO log
         e.printStackTrace();
      }
   }

   private boolean containsTargetPlatform(final Collection<IFile> tpFiles, final ITargetHandle tpHandle)
   {
      for (IFile tpFile : tpFiles)
      {
         if (tpService.getTarget(tpFile).equals(tpHandle))
         {
            return true;
         }
      }
      return false;
   }

   private static Collection<IFile> getChangedTargetPlatformFiles(final IResourceDelta delta)
   {
      final Collection<IFile> files = new LinkedHashSet<IFile>();

      final IResourceDeltaVisitor visitor = new IResourceDeltaVisitor()
      {
         @Override
         public boolean visit(IResourceDelta delta) throws CoreException
         {
            final int kind = delta.getKind();
            switch (kind)
            {
               case IResourceDelta.ADDED :
               case IResourceDelta.CHANGED :
                  final IResource resource = delta.getResource();
                  if (isTargetPlatformFile(resource))
                  {
                     files.add((IFile) resource);
                  }
                  return true;
               default :
                  return false;
            }
         }
      };

      try
      {
         delta.accept(visitor);
      }
      catch (CoreException e)
      {
         throw pipe(e);
      }

      return files;
   }

   private static boolean isTargetPlatformFile(IResource resource)
   {
      final IContentType contentType = getContentType(resource);
      if (contentType != null)
      {
         return "org.eclipse.pde.targetFile".equals(contentType.getId());
      }
      return false;
   }

   private static IContentType getContentType(final IResource resource)
   {
      final IContentDescription contentDescription = getContentDescription(resource);
      if (contentDescription != null)
      {
         return contentDescription.getContentType();
      }
      return null;
   }

   private static IContentDescription getContentDescription(final IResource resource)
   {
      IContentDescription contentDescription = null;
      if (resource.getType() == FILE)
      {
         try
         {
            contentDescription = ((IFile) resource).getContentDescription();
         }
         catch (CoreException e)
         {
         }
      }
      return contentDescription;
   }

   private static ITargetHandle getWorkspaceTargetPlatformHandle(ITargetPlatformService tpService)
   {
      try
      {
         return tpService.getWorkspaceTargetHandle();
      }
      catch (CoreException e)
      {
         throw pipe(e);
      }
   }
}