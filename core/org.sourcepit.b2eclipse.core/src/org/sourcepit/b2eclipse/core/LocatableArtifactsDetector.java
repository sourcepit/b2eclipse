/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.core;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.base.Predicate;

public interface LocatableArtifactsDetector
{
   List<Artifact> detectLocateableArtifacts(String groupId, String artifactId, String type, String classifier,
      List<ArtifactRepository> repositories, Predicate<Artifact> artifactFilter, IProgressMonitor monitor);
}
