/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.installer;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.DefaultMaven;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.sourcepit.b2eclipse.core.DefaultLocatableArtifactsDetector;
import org.sourcepit.b2eclipse.core.DefaultMavenContext;

public class B2InstallationWizard extends Wizard
{

   public B2InstallationWizard()
   {
      setWindowTitle("New Wizard");
   }

   @Override
   public void addPages()
   {
      final DefaultMavenContext mavenContext = new DefaultMavenContext(MavenPlugin.getMaven());

      B2ChoserWizardPage b2Page = new B2ChoserWizardPage(new DefaultLocatableArtifactsDetector(mavenContext));
      try
      {
         b2Page.setArtifactRepository(getArtifactRepositories(MavenPlugin.getMaven()));
      }
      catch (CoreException e)
      {
         // TODO: imm0136 Auto-generated catch block
         e.printStackTrace();
      }

      addPage(b2Page);
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
      return false;
   }

}
