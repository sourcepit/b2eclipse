/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.debug;

import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.m2e.actions.MavenLaunchConstants;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenRuntime;
import org.eclipse.m2e.core.embedder.MavenRuntimeManager;
import org.eclipse.m2e.ui.internal.launch.MavenLaunchMainTab;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.sourcepit.b2eclipse.core.runtime.B2RuntimeUtils;
import org.sourcepit.b2eclipse.ui.installer.B2InstallerWizard;
import org.sourcepit.b2eclipse.ui.installer.Selections;

public class LaunchShortcut1 implements ILaunchShortcut
{
   @Override
   public void launch(ISelection selection, String mode)
   {
      final IProject project = getProject(selection);

      final MavenRuntime runtime = getB2Runtime(project);

      final ILaunchConfiguration launchConfiguration = createLaunchConfiguration(project, runtime,
         "target-platform:localize");

      boolean openDialog = true;
      if (!openDialog)
      {
         try
         {
            // if no goals specified
            String goals = launchConfiguration.getAttribute(MavenLaunchConstants.ATTR_GOALS, (String) null);
            openDialog = goals == null || goals.trim().length() == 0;
         }
         catch (CoreException e)
         {
            throw pipe(e);
         }
      }

      if (openDialog)
      {
         ILaunchGroup group = DebugUITools.getLaunchGroup(launchConfiguration, mode);
         String groupId = group != null ? group.getIdentifier() : MavenLaunchMainTab.ID_EXTERNAL_TOOLS_LAUNCH_GROUP;
         DebugUITools.openLaunchConfigurationDialog(getShell(), launchConfiguration, groupId, null);
      }
      else
      {
         DebugUITools.launch(launchConfiguration, mode);
      }
   }

   private ILaunchConfiguration createLaunchConfiguration(IContainer basedir, MavenRuntime runtime, String goal)
   {
      ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
      ILaunchConfigurationType launchConfigurationType = launchManager
         .getLaunchConfigurationType(MavenLaunchConstants.LAUNCH_CONFIGURATION_TYPE_ID);

      String launchSafeGoalName = goal.replace(':', '-');

      final ILaunchConfigurationWorkingCopy workingCopy;
      try
      {
         workingCopy = launchConfigurationType.newInstance(null, //
            launchSafeGoalName);
      }
      catch (CoreException e)
      {
         throw pipe(e);
      }

      workingCopy.setAttribute(MavenLaunchConstants.ATTR_POM_DIR, basedir.getLocation().toOSString());
      workingCopy.setAttribute(MavenLaunchConstants.ATTR_GOALS, goal);
      workingCopy.setAttribute(MavenLaunchConstants.ATTR_RUNTIME, runtime.getLocation());
      workingCopy.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
      workingCopy.setAttribute(RefreshTab.ATTR_REFRESH_SCOPE, "${project}"); //$NON-NLS-1$
      workingCopy.setAttribute(RefreshTab.ATTR_REFRESH_RECURSIVE, true);

      IPath path = getJREContainerPath(basedir);
      if (path != null)
      {
         workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, path.toPortableString());
      }

      // TODO when launching Maven with debugger consider to add the following property
      // -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent -Djava.compiler=NONE"
      try
      {
         workingCopy.doSave();
      }
      catch (CoreException e)
      {
         throw pipe(e);
      }

      return workingCopy;
   }

   // TODO ideally it should use MavenProject, but it is faster to scan IJavaProjects
   private IPath getJREContainerPath(IContainer basedir)
   {
      try
      {
         IProject project = basedir.getProject();
         if (project != null && project.hasNature(JavaCore.NATURE_ID))
         {
            IJavaProject javaProject = JavaCore.create(project);
            IClasspathEntry[] entries = javaProject.getRawClasspath();
            for (int i = 0; i < entries.length; i++)
            {
               IClasspathEntry entry = entries[i];
               if (JavaRuntime.JRE_CONTAINER.equals(entry.getPath().segment(0)))
               {
                  return entry.getPath();
               }
            }
         }
         return null;
      }
      catch (CoreException e)
      {
         throw pipe(e);
      }
   }

   private MavenRuntime getB2Runtime(IProject project)
   {
      MavenRuntimeManager mavenRuntimeManager = MavenPlugin.getMavenRuntimeManager();

      String b2Version = getB2Version(project);

      for (MavenRuntime mavenRuntime : mavenRuntimeManager.getMavenRuntimes())
      {
         if (!B2RuntimeUtils.getB2Runtimes(new File(mavenRuntime.getLocation())).isEmpty())
         {
            return mavenRuntime;
         }
      }

      Shell shell = getShell();

      B2InstallerWizard installerWizard = new B2InstallerWizard();
      WizardDialog dlg = new WizardDialog(shell, installerWizard);

      return dlg.open() == Window.OK ? installerWizard.getMavenRuntime() : null;
   }

   Shell getShell()
   {
      return PlatformUI.getWorkbench().getDisplay().getActiveShell();
   }

   private String getB2Version(IProject project)
   {
      return "";
   }

   private IProject getProject(ISelection selection)
   {
      final Object element = Selections.getFirstElement(selection);
      if (element instanceof IAdaptable)
      {
         return (IProject) ((IAdaptable) element).getAdapter(IProject.class);
      }
      return null;
   }

   @Override
   public void launch(IEditorPart editor, String mode)
   {
   }

}
