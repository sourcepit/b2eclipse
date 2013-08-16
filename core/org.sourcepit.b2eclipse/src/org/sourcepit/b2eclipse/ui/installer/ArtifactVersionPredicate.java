/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.installer;

import org.apache.maven.artifact.Artifact;
import org.sonatype.aether.util.version.GenericVersionScheme;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionConstraint;
import org.sonatype.aether.version.VersionScheme;

import com.google.common.base.Predicate;

public class ArtifactVersionPredicate implements Predicate<Artifact>
{
   private final VersionScheme versionScheme = new GenericVersionScheme();

   private final VersionConstraint versionConstraint;

   public ArtifactVersionPredicate(String versionConstraint)
   {
      this.versionConstraint = parseVersionConstraint(versionConstraint);
   }

   @Override
   public boolean apply(Artifact artifact)
   {
      final String version = artifact.getVersion();

      if (versionConstraint.containsVersion(parseVersion(version)))
      {
         return true;
      }

      return false;
   }

   private Version parseVersion(final String version)
   {
      try
      {
         return versionScheme.parseVersion(version);
      }
      catch (org.sonatype.aether.version.InvalidVersionSpecificationException e)
      {
         throw new IllegalArgumentException(e);
      }
   }

   private VersionConstraint parseVersionConstraint(String versionConstraint)
   {
      try
      {
         return versionScheme.parseVersionConstraint(versionConstraint);
      }
      catch (org.sonatype.aether.version.InvalidVersionSpecificationException e)
      {
         throw new IllegalArgumentException(e);
      }
   }
}