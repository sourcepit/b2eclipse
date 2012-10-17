/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.sourcepit.b2eclipse.Activator;
import org.sourcepit.b2eclipse.input.node.Node;
import org.sourcepit.b2eclipse.input.node.NodeModule;
import org.sourcepit.b2eclipse.input.node.NodeProject;
import org.sourcepit.b2eclipse.input.node.NodeWorkingSet;

/**
 * @author WD
 */
public class B2Wizard extends Wizard implements IImportWizard
{
   private B2WizardPage page;

   public B2Wizard()
   {
      super();
   }

   public void init(IWorkbench workbench, IStructuredSelection selection)
   {
      setWindowTitle(Messages.msgImportTitle);
      Image projectFolder = Activator.getImageFromPath("icons/ProjectFolder.gif");
      setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(projectFolder));

      setHelpAvailable(false);
      setNeedsProgressMonitor(true);

      page = new B2WizardPage(Messages.msgImportHeader, this, selection);
      addPage(page);
   }

   /**
    * Performs the finish of this Wizard.
    * 
    * <br>
    * <br>
    * {@inheritDoc}
    */
   @Override
   public boolean performFinish()
   {
      IRunnableWithProgress dialogRunnable = new IRunnableWithProgress()
      {
         public void run(IProgressMonitor monitor) throws InvocationTargetException
         {
            try
            {
               ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable()
               {
                  public void run(IProgressMonitor monitor) throws CoreException
                  {
                     doFinish(monitor);
                  }
               }, monitor);
            }
            catch (OperationCanceledException e)
            {
               // TODO implement
            }
            catch (CoreException e)
            {
               throw new InvocationTargetException(e);
            }
         }
      };

      try
      {
         getContainer().run(true, true, dialogRunnable);
      }
      catch (InvocationTargetException e)
      {
         throw new IllegalStateException(e.getTargetException());
      }
      catch (InterruptedException e)
      {
         throw new IllegalStateException(e);
      }
      return true;
   }

   /**
    * Creates WorkingSets and Projects in the Workspace.
    * 
    * @param monitor
    * @throws CoreException
    */
   private void doFinish(IProgressMonitor monitor) throws CoreException
   {
      final IWorkingSetManager wSmanager = PlatformUI.getWorkbench().getWorkingSetManager();
      final IWorkspace workspace = ResourcesPlugin.getWorkspace();
      final Node root = page.getPreviewRootNode();

      final int length = root.getProjectChildren().size();

      try
      {
         monitor.beginTask(Messages.msgTask, length);
         for (Node currentElement : root.getChildren())
         {
            if (currentElement instanceof NodeWorkingSet)
            {
               String wsName = currentElement.getName();
               monitor.subTask(wsName);
               IWorkingSet workingSet = wSmanager.getWorkingSet(wsName);
               if (workingSet == null)
               {
                  // info:
                  // org.eclipse.ui.resourceWorkingSetPage = Resource WorkingSet
                  // org.eclipse.jdt.ui.JavaWorkingSetPage = Java WorkingSet

                  workingSet = wSmanager.createWorkingSet(wsName, new IAdaptable[] {});
                  workingSet.setId("org.eclipse.jdt.ui.JavaWorkingSetPage");
                  wSmanager.addWorkingSet(workingSet);
               }

               for (Node currentSubElement : currentElement.getChildren())
               {
                  monitor.subTask(currentSubElement.getName());
                  if (currentSubElement instanceof NodeProject || currentSubElement instanceof NodeModule)
                  {
                     IProject project = createOrOpenProject(currentSubElement.getFile().toString(),
                        currentSubElement.getName(), workspace);
                     wSmanager.addToWorkingSets(project, new IWorkingSet[] { workingSet });
                     monitor.worked(1);
                  }
               }
            }
            if (currentElement instanceof NodeProject || currentElement instanceof NodeModule)
            {
               monitor.subTask(currentElement.getName());
               createOrOpenProject(currentElement.getFile().toString(), currentElement.getName(), workspace);
               monitor.worked(1);
            }
         }
      }
      finally
      {
         monitor.done();
      }
   }

   private IProject createOrOpenProject(String path, String name, IWorkspace workspace) throws CoreException
   {
      IProject project;
      if (new File(path + "/.project").exists())
      {
         IProjectDescription projectDescription = workspace.loadProjectDescription(new Path(path + "/.project"));
         project = workspace.getRoot().getProject(projectDescription.getName());
         JavaCapabilityConfigurationPage.createProject(project, projectDescription.getLocationURI(), null);
      }
      else
      {
         IProjectDescription projectDescription = workspace.newProjectDescription(name);
         projectDescription.setLocation(new Path(path));
         project = workspace.getRoot().getProject(projectDescription.getName());
         JavaCapabilityConfigurationPage.createProject(project, projectDescription.getLocationURI(), null);
      }
      return project;
   }
}
