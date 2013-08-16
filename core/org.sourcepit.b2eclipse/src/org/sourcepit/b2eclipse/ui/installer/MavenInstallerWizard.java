/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.installer;

import static org.sourcepit.common.utils.lang.Exceptions.pipe;
import static org.sourcepit.common.utils.zip.ZipProcessingRequest.newUnzipRequest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.sourcepit.b2eclipse.core.ArtifactResolver;
import org.sourcepit.b2eclipse.core.LocatableArtifactsDetector;
import org.sourcepit.common.utils.zip.ZipProcessor;

public class MavenInstallerWizard extends Wizard
{
   private final File parentDir;
   private final LocatableArtifactsDetector artifactsDetector;
   private final ArtifactResolver artifactResolver;

   private MavenInstallerWizardPage installerPage;
   private File installationDir;

   public MavenInstallerWizard(File parentDir, LocatableArtifactsDetector artifactsDetector,
      ArtifactResolver artifactResolver)
   {
      setWindowTitle("New Wizard");
      this.artifactsDetector = artifactsDetector;
      this.artifactResolver = artifactResolver;
      this.parentDir = parentDir;
   }

   @Override
   public void addPages()
   {
      installerPage = new MavenInstallerWizardPage(parentDir, artifactsDetector);
      addPage(installerPage);
   }
   
   public File getInstallationDirectory()
   {
      return installationDir;
   }

   @Override
   public boolean performFinish()
   {
      installationDir = installerPage.getInstallationDirectory();
      final Artifact artifact = installerPage.getArtifact();
      RunnableContexts.run(getContainer(), true, true, new IRunnableWithProgress()
      {
         @Override
         public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
         {
            performFinish(installationDir, artifact, monitor);
         }
      });
      return true;
   }

   private void performFinish(File installationDir, Artifact artifact, IProgressMonitor monitor)
   {
      final Artifact resolvedArtifact = artifactResolver.resolve(artifact, monitor);
      final File zipFile = resolvedArtifact.getFile();
      try
      {
         new ZipProcessor().process(newUnzipRequest(zipFile, installationDir));
      }
      catch (IOException e)
      {
         throw pipe(e);
      }

      File[] files = installationDir.listFiles();

      if (files.length == 1 && files[0].isDirectory())
      {
         try
         {
            FileUtils.copyDirectory(files[0], installationDir, true);
            FileUtils.deleteDirectory(files[0]);
         }
         catch (IOException e)
         {
            throw pipe(e);
         }
      }
   }

}
