/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.core.builder;

import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.LoadTargetDefinitionJob;
import org.sourcepit.b2eclipse.core.B2CorePlugin;

public class B2Builder extends IncrementalProjectBuilder
{
   public static final String BUILDER_ID = "org.sourcepit.b2eclipse.core.B2Builder";

   class TargetPlatformFileDeltaVisitor implements IResourceDeltaVisitor
   {
      @Override
      public boolean visit(IResourceDelta delta) throws CoreException
      {
         IResource resource = delta.getResource();
         if (resource.getType() != IResource.FILE)
         {
            return true;
         }

         switch (delta.getKind())
         {
            case IResourceDelta.ADDED :
            case IResourceDelta.CHANGED :
               final IFile file = (IFile) resource;
               if (isTargetPlatformFile(file))
               {
                  checkTargetPlatformFile(file);
               }
               break;
         }
         return true;
      }
   }

   protected static boolean isTargetPlatformFile(IFile file)
   {
      IContentDescription desc;
      try
      {
         desc = file.getContentDescription();
      }
      catch (CoreException e)
      {
         throw pipe(e);
      }
      final IContentType contentType = desc.getContentType();
      return "org.eclipse.pde.targetFile".equals(contentType.getId());
   }

   protected void checkTargetPlatformFile(IFile file)
   {
      final ITargetPlatformService tpService = getTargetPlatformService();
      final ITargetHandle tpHandle = tpService.getTarget(file);

      if (tpHandle.equals(getWorkspaceTargetHandle(tpService)))
      {
         try
         {
            LoadTargetDefinitionJob.load(tpHandle.getTargetDefinition());
         }
         catch (CoreException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }

   private static ITargetPlatformService getTargetPlatformService()
   {
      return B2CorePlugin.getDefault().acquireService(ITargetPlatformService.class);
   }

   private static ITargetHandle getWorkspaceTargetHandle(ITargetPlatformService tpService)
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

   @Override
   protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException
   {
      if (kind == FULL_BUILD)
      {
         fullBuild(monitor);
      }
      else
      {

         IResourceDelta delta = getDelta(getProject());
         if (delta == null)
         {
            fullBuild(monitor);
         }
         else
         {
            incrementalBuild(delta, monitor);
         }
      }
      return null;
   }

   @Override
   protected void clean(IProgressMonitor monitor) throws CoreException
   {
   }

   protected void fullBuild(final IProgressMonitor monitor) throws CoreException
   {
   }

   protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException
   {
      final ITargetPlatformService targetPlatformService = B2CorePlugin.getDefault().acquireService(
         ITargetPlatformService.class);

      new TargetPlatformFilesObserver(targetPlatformService).resourceChanged(delta);
   }
}
