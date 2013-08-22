/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.installer;

import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.MavenRuntime;
import org.sourcepit.b2eclipse.core.ArtifactResolver;
import org.sourcepit.b2eclipse.core.B2CorePlugin;
import org.sourcepit.b2eclipse.core.DefaultArtifactResolver;
import org.sourcepit.b2eclipse.core.DefaultLocatableArtifactsDetector;
import org.sourcepit.b2eclipse.core.DefaultMavenContext;
import org.sourcepit.b2eclipse.core.LocatableArtifactsDetector;
import org.sourcepit.b2eclipse.core.runtime.B2Runtime;
import org.sourcepit.b2eclipse.core.runtime.B2RuntimeUtils;

public class B2InstallerWizard extends Wizard
{
   Artifact b2Artifact;

   MavenRuntime mavenRuntime;

   ArtifactResolver artifactResolver;

   public B2InstallerWizard()
   {
      setWindowTitle("New Wizard");
   }


   @Override
   public void addPages()
   {
      final File stateLocationDir = B2CorePlugin.getDefault().getStateLocation().toFile();
      final File installationsDir = new File(stateLocationDir, "runtimes");

      final DefaultMavenContext mavenContext = new DefaultMavenContext(MavenPlugin.getMaven());

      final List<ArtifactRepository> repositories;
      try
      {
         repositories = getArtifactRepositories(MavenPlugin.getMaven());
      }
      catch (CoreException e)
      {
         throw pipe(e);
      }

      final LocatableArtifactsDetector artifactsDetector = new DefaultLocatableArtifactsDetector(mavenContext,
         repositories);

      artifactResolver = new DefaultArtifactResolver(mavenContext, repositories);

      B2ChoserWizardPage b2Page = new B2ChoserWizardPage(artifactsDetector)
      {
         @Override
         protected void selectionChanged(Artifact artifact)
         {
            super.selectionChanged(artifact);
            b2Artifact = artifact;
         }
      };
      addPage(b2Page);

      MavenChoserWizardPage mavenPage = new MavenChoserWizardPage(installationsDir,
         MavenPlugin.getMavenRuntimeManager(), artifactsDetector, artifactResolver)
      {
         @Override
         protected void selectionChanged(MavenRuntime mavenRuntime)
         {
            super.selectionChanged(mavenRuntime);
            B2InstallerWizard.this.mavenRuntime = mavenRuntime;
         }
      };

      addPage(mavenPage);
   }

   List<ArtifactRepository> getArtifactRepositories(final IMaven maven) throws CoreException
   {
      List<ArtifactRepository> repositories = new ArrayList<ArtifactRepository>();
      repositories.addAll(maven.getArtifactRepositories());
      addSrcpitRepos(maven, repositories);
      return repositories;
   }

   void addSrcpitRepos(final IMaven maven, List<ArtifactRepository> repositories) throws CoreException
   {
      repositories.add(maven.createArtifactRepository("srcpit-public",
         "http://nexus.sourcepit.org/content/groups/public"));
   }

   @Override
   public boolean performFinish()
   {
      RunnableContexts.run(getContainer(), true, true, new IRunnableWithProgress()
      {
         @Override
         public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
         {
            performFinish(monitor);
         }
      });
      return true;
   }

   protected void performFinish(IProgressMonitor monitor)
   {
      final Artifact resolvedArtifact = artifactResolver.resolve(b2Artifact, monitor);
      final File b2File = resolvedArtifact.getFile();

      final File mavenDir = new File(mavenRuntime.getLocation());

      final List<B2Runtime> b2Runtimes = B2RuntimeUtils.getB2Runtimes(mavenDir);
      for (B2Runtime b2Runtime : b2Runtimes)
      {
         for (File file : b2Runtime.getFiles())
         {
            try
            {
               FileUtils.forceDelete(file);
            }
            catch (IOException e)
            {
               throw pipe(e);
            }
         }
      }

      try
      {
         FileUtils.copyFile(b2File, new File(mavenDir, "lib/ext/" + b2File.getName()));
      }
      catch (IOException e)
      {
         throw pipe(e);
      }

   }

   public MavenRuntime getMavenRuntime()
   {
      return mavenRuntime;
   }

}
