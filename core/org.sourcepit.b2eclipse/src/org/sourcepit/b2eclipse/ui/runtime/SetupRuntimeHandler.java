/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.runtime;

import static java.util.Collections.sort;
import static org.apache.maven.RepositoryUtils.toRepo;
import static org.sourcepit.common.utils.io.IO.buffIn;
import static org.sourcepit.common.utils.io.IO.fileIn;
import static org.sourcepit.common.utils.io.IO.read;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.repository.Proxy;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ICallable;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenExecutionContext;
import org.eclipse.m2e.core.embedder.MavenRuntime;
import org.eclipse.m2e.core.embedder.MavenRuntimeManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.impl.MetadataGenerator;
import org.sonatype.aether.impl.MetadataGeneratorFactory;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.metadata.Metadata.Nature;
import org.sonatype.aether.resolution.MetadataRequest;
import org.sonatype.aether.resolution.MetadataResult;
import org.sonatype.aether.resolution.VersionResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.metadata.DefaultMetadata;
import org.sonatype.aether.util.version.GenericVersionScheme;
import org.sonatype.aether.version.VersionConstraint;
import org.sonatype.aether.version.VersionScheme;
import org.sourcepit.b2eclipse.core.runtime.B2RuntimeUtils;
import org.sourcepit.b2eclipse.ui.installer.B2InstallerWizard;
import org.sourcepit.common.utils.io.Read.FromStream;

public class SetupRuntimeHandler extends AbstractHandler implements IHandler
{
   static class Dependency
   {
      String groupId;
      String artifactId;
      String version;
      String type = "jar";
      String classifier;
   }

   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException
   {
      IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

      WizardDialog dialog = new WizardDialog(window.getShell(), new B2InstallerWizard());

      dialog.open();
      
      // try
      // {
      // execute();
      // }
      // catch (CoreException e)
      // {
      // e.printStackTrace();
      // }

      return null;
   }

   void execute() throws CoreException
   {
      final IMaven maven = MavenPlugin.getMaven();

      final Dependency dep = new Dependency();
      dep.groupId = "org.apache.maven";
      dep.artifactId = "apache-maven";
      dep.version = "3.0.5";
      dep.type = "zip";
      dep.classifier = "bin";

      final List<ArtifactRepository> repositories = getArtifactRepositories(maven);

      final List<String> mavenVersions = resolveVersions("org.apache.maven", "apache-maven", maven, repositories);
      System.out.println(mavenVersions);

      VersionScheme versionScheme = new GenericVersionScheme();

      VersionConstraint versionConstraint;
      try
      {
         versionConstraint = versionScheme.parseVersionConstraint("[3.0.2,3.1-alpha)");
      }
      catch (org.sonatype.aether.version.InvalidVersionSpecificationException e)
      {
         throw new IllegalArgumentException(e);
      }

      // final VersionRange versionRange = createVersionRange("[3.0.2,3.1)");
      for (String mavenVersion : mavenVersions)
      {
         try
         {
            if (versionConstraint.containsVersion(versionScheme.parseVersion(mavenVersion)))
            {
               System.out.println(mavenVersion);
            }
         }
         catch (org.sonatype.aether.version.InvalidVersionSpecificationException e)
         {
            throw new IllegalArgumentException(e);
         }
      }

      List<String> b2Versions = resolveVersions("org.sourcepit.b2", "b2-bootstrapper", maven, repositories);
      System.out.println(b2Versions);

      // resolve(dep);

      final List<MavenRuntime> externalRuntimes = getExternalMavenRuntimes();
      for (MavenRuntime externalRuntime : externalRuntimes)
      {
         String mavenHome = externalRuntime.getLocation();
         B2RuntimeUtils.getB2Version(new File(mavenHome));
         System.out.println(mavenHome);
      }
   }

   static VersionRange createVersionRange(String versionSpec)
   {
      try
      {
         return VersionRange.createFromVersionSpec(versionSpec);
      }
      catch (InvalidVersionSpecificationException e)
      {
         throw new IllegalArgumentException(e);
      }
   }

   List<String> resolveVersions(final String groupId, final String artifactId, final IMaven maven,
      final List<ArtifactRepository> repositories)
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

            final PlexusContainer plexus = getPlexusContainer(maven);
            final RepositorySystem repoSystem = lookup(plexus, RepositorySystem.class);

            final List<MetadataResult> metadataResults = repoSystem.resolveMetadata(context.getRepositorySession(),
               metadataRequests);

            final Set<String> versions = new LinkedHashSet<String>(metadataResults.size());
            for (MetadataResult metadataResult : metadataResults)
            {
               final File metadataFile = metadataResult.getMetadata() == null ? null : metadataResult.getMetadata()
                  .getFile();
               if (metadataFile != null && metadataFile.exists())
               {
                  final Versioning versioning = readVersions(metadataFile);
                  versions.addAll(versioning.getVersions());
               }
            }

            return new ArrayList<String>(versions);
         }
      };

      try
      {
         return maven.execute(callable, null);
      }
      catch (CoreException e)
      {
         throw new IllegalStateException(e);
      }
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

   static Versioning readVersions(File metadataFile)
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

   // URLConnection openConnection(ArtifactRepository repository, String path) throws IOException
   // {
   // final URL url;
   // try
   // {
   // url = new URL(repository.getUrl() + path);
   // }
   // catch (MalformedURLException e)
   // {
   // throw new IllegalStateException(e);
   // }
   //
   // java.net.Proxy jProxy = toJNetProxy(repository.getProxy());
   //
   // return url.openConnection(toJNetProxy(proxy));
   // }

   static java.net.Proxy toJNetProxy(Proxy proxy)
   {
      if (proxy == null)
      {
         return null;
      }
      return new java.net.Proxy(Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort()));
   }

   void resolve(final Dependency dep) throws CoreException
   {
      final IMaven maven = MavenPlugin.getMaven();
      List<ArtifactRepository> repositories = getArtifactRepositories(maven);
      maven.resolve(dep.groupId, dep.artifactId, dep.version, dep.type, dep.classifier, repositories, null);
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

   private List<MavenRuntime> getExternalMavenRuntimes()
   {
      final List<MavenRuntime> externalRuntimes = new ArrayList<MavenRuntime>();
      for (MavenRuntime mavenRuntime : MavenPlugin.getMavenRuntimeManager().getMavenRuntimes())
      {
         if (!MavenRuntimeManager.EMBEDDED.equals(mavenRuntime.getLocation()))
         {
            externalRuntimes.add(mavenRuntime);
         }
      }
      return externalRuntimes;
   }
}
