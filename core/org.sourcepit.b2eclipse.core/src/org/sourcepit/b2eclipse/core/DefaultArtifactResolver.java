/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.core;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.runtime.IProgressMonitor;

public class DefaultArtifactResolver implements ArtifactResolver
{
   final MavenContext mavenContext;

   final List<ArtifactRepository> repositories;

   public DefaultArtifactResolver(MavenContext mavenContext, List<ArtifactRepository> repositories)
   {
      this.mavenContext = mavenContext;
      this.repositories = repositories;
   }

   @Override
   public Artifact resolve(Artifact artifact, IProgressMonitor monitor)
   {
      return mavenContext.resolve(artifact, repositories, monitor);
   }

}
