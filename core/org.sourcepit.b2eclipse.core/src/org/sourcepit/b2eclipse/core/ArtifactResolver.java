/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.core;

import org.apache.maven.artifact.Artifact;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ArtifactResolver
{
   Artifact resolve(Artifact artifact, IProgressMonitor monitor);
}
