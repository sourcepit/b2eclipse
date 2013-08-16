/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.core;

import static org.apache.maven.RepositoryUtils.toRepo;
import static org.sourcepit.common.utils.io.IO.buffIn;
import static org.sourcepit.common.utils.io.IO.fileIn;
import static org.sourcepit.common.utils.io.IO.read;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.embedder.ICallable;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenExecutionContext;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.metadata.Metadata.Nature;
import org.sonatype.aether.resolution.MetadataRequest;
import org.sonatype.aether.resolution.MetadataResult;
import org.sonatype.aether.util.metadata.DefaultMetadata;
import org.sourcepit.common.utils.io.Read.FromStream;

import com.google.common.base.Predicate;

public class DefaultLocatableArtifactsDetector implements LocatableArtifactsDetector
{
   final MavenContext mavenContext;

   final List<ArtifactRepository> repositories;

   public DefaultLocatableArtifactsDetector(MavenContext mavenContext, List<ArtifactRepository> repositories)
   {
      this.mavenContext = mavenContext;
      this.repositories = repositories;
   }

   @Override
   public List<Artifact> detectLocatableArtifacts(String groupId, String artifactId, String type, String classifier,
      Predicate<Artifact> artifactFilter, IProgressMonitor monitor)
   {
      final List<Artifact> locateableArtifacts = new ArrayList<Artifact>();
      final org.apache.maven.repository.RepositorySystem mavenRepoSession = mavenContext
         .lookup(org.apache.maven.repository.RepositorySystem.class);
      final List<String> versions = detectLocatableArtifactVersions(groupId, artifactId, monitor);
      for (String version : versions)
      {
         final Artifact artifact = mavenRepoSession.createArtifactWithClassifier(groupId, artifactId, version, type,
            classifier);
         if (artifactFilter == null || artifactFilter.apply(artifact))
         {
            locateableArtifacts.add(artifact);
         }
      }
      Collections.reverse(locateableArtifacts);
      return locateableArtifacts;
   }

   List<String> detectLocatableArtifactVersions(final String groupId, final String artifactId, IProgressMonitor monitor)
   {
      final ICallable<List<String>> callable = new ICallable<List<String>>()
      {
         @Override
         public List<String> call(IMavenExecutionContext context, IProgressMonitor monitor)
         {
            final Metadata metadata = new DefaultMetadata(groupId, artifactId, "maven-metadata.xml",
               Nature.RELEASE_OR_SNAPSHOT);

            final List<MetadataRequest> metadataRequests = new ArrayList<MetadataRequest>(repositories.size());
            metadataRequests.add(new MetadataRequest(metadata, null, null));
            for (ArtifactRepository repository : repositories)
            {
               final MetadataRequest metadataRequest = new MetadataRequest();
               metadataRequest.setMetadata(metadata);
               metadataRequest.setRepository(toRepo(repository));
               metadataRequest.setDeleteLocalCopyIfMissing(true);
               metadataRequest.setFavorLocalRepository(false);
               metadataRequests.add(metadataRequest);
            }

            final List<MetadataResult> metadataResults = mavenContext.lookup(RepositorySystem.class).resolveMetadata(
               context.getRepositorySession(), metadataRequests);

            final Set<String> versions = new LinkedHashSet<String>(metadataResults.size());
            for (MetadataResult metadataResult : metadataResults)
            {
               final File metadataFile = metadataResult.getMetadata() == null ? null : metadataResult.getMetadata()
                  .getFile();
               if (metadataFile != null && metadataFile.exists())
               {
                  final Versioning versioning = readVersioning(metadataFile);
                  versions.addAll(versioning.getVersions());
               }
            }

            return new ArrayList<String>(versions);
         }
      };

      return mavenContext.execute(callable, monitor);
   }

   static Versioning readVersioning(File metadataFile)
   {
      final FromStream<Versioning> fromStream = new FromStream<Versioning>()
      {
         @Override
         public Versioning read(InputStream inputStream) throws Exception
         {
            org.apache.maven.artifact.repository.metadata.Metadata m = new MetadataXpp3Reader()
               .read(inputStream, false);
            return m.getVersioning();
         }
      };
      return read(fromStream, buffIn(fileIn(metadataFile)));
   }

   <T> T lookup(final PlexusContainer plexus, Class<T> clazz)
   {
      try
      {
         return plexus.lookup(clazz);
      }
      catch (ComponentLookupException e)
      {
         throw new IllegalStateException(e);
      }
   }

   <T> T lookup(final PlexusContainer plexus, Class<T> clazz, String hint)
   {
      try
      {
         return plexus.lookup(clazz, hint);
      }
      catch (ComponentLookupException e)
      {
         throw new IllegalStateException(e);
      }
   }

   static PlexusContainer getPlexusContainer(final IMaven maven)
   {
      try
      {
         return (PlexusContainer) maven.getClass().getMethod("getPlexusContainer").invoke(maven);
      }
      catch (InvocationTargetException e)
      {
         throw new IllegalStateException(e.getCause());
      }
      catch (Exception e)
      {
         throw new IllegalStateException(e);
      }
   }

}
