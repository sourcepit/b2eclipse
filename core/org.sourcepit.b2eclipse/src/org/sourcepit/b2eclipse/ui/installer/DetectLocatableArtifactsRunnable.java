/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.installer;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.sourcepit.b2eclipse.core.LocatableArtifactsDetector;

import com.google.common.base.Predicate;

public abstract class DetectLocatableArtifactsRunnable implements IRunnableWithProgress
{
   final public LocatableArtifactsDetector artifactsDetector;
   final public String groupId;
   final public String artifactId;
   final public String type;
   final public String classifier;
   final public Predicate<Artifact> artifactFilter;

   public DetectLocatableArtifactsRunnable(LocatableArtifactsDetector artifactsDetector, String groupId,
      String artifactId, String type, String classifier, Predicate<Artifact> artifactFilter)
   {
      this.artifactsDetector = artifactsDetector;
      this.groupId = groupId;
      this.artifactId = artifactId;
      this.type = type;
      this.classifier = classifier;
      this.artifactFilter = artifactFilter;
   }

   @Override
   public void run(IProgressMonitor monitor)
   {
      final List<Artifact> artifacts = artifactsDetector.detectLocatableArtifacts(groupId, artifactId, type,
         classifier, artifactFilter, monitor);
      final Artifact recommended = determineRecommended(artifacts);
      detectedLocatableArtifacts(artifacts, recommended);
   }

   protected abstract void detectedLocatableArtifacts(List<Artifact> artifacts, Artifact recommended);

   protected Artifact determineRecommended(List<Artifact> artifacts)
   {
      for (Artifact artifact : artifacts)
      {
         if (!ArtifactUtils.isSnapshot(artifact.getVersion()))
         {
            return artifact;
         }
      }
      return null;
   }
}