/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.core;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.base.Predicate;

public interface LocatableArtifactsDetector
{
   List<Artifact> detectLocatableArtifacts(String groupId, String artifactId, String type, String classifier,
      Predicate<Artifact> artifactFilter, IProgressMonitor monitor);
}
