/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;


import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.sourcepit.b2eclipse.Activator;
import org.sourcepit.b2eclipse.provider.ContentProvider;
import org.sourcepit.b2eclipse.structure.TreeviewerInput;

/**
 * @author Marco Grupe
 */

public class B2Wizard extends Wizard implements IImportWizard, ISelectionListener
{


   private static IPath projectPath;
   private B2WizardPage modulePage;
   private List<File> projects;
   private final IWorkspace workspace = ResourcesPlugin.getWorkspace();
   private final IWorkbench workbench = PlatformUI.getWorkbench();
   private IPath projectDotProjectFile;
   private IProjectDescription projectDescription;
   private IProject project;
   private Object firstElement;
   private IResource selectedProject;


   public B2Wizard()
   {
      super();
      setWindowTitle("Import b2 Projects");
      modulePage = B2WizardPage.getInstance();
      addPage(modulePage);


   }


   /**
    * dafür zuständig um Finish Button zu aktivieren
    */
   @Override
   public boolean performFinish()
   {


      Runnable runnable = new Runnable()
      {
         public void run()
         {
            try
            {
               projects = modulePage.getSelectedProjects();

               for (int i = 0; i < projects.size(); i++)
               {


                  projectDotProjectFile = new Path(String.valueOf(projects.get(i)));
                  projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
                  project = workspace.getRoot().getProject(projectDescription.getName());
                  JavaCapabilityConfigurationPage.createProject(project, projectDescription.getLocationURI(), null);

                  if (modulePage.isCheckButtonSelected() && modulePage.getWorkingSet() != null)
                  {
                     modulePage.getWorkingSetManager().addToWorkingSets(project, modulePage.getWorkingSet());
                  }
               }
            }
            catch (CoreException e)
            {
               Activator.error("Fehler", e);
            }
         }
      };


      workbench.getDisplay().syncExec(runnable);


      return true;
   }


   /**
    * bei einem Klick auf ein Project im Package Explorer wird dessen Pfad ausgelesen
    */
   @Override
   public void init(IWorkbench workbench, IStructuredSelection selection)
   {
      workbench.getActiveWorkbenchWindow().getSelectionService().addSelectionListener(this);

      if (selection instanceof IStructuredSelection)
      {


         firstElement = selection.getFirstElement();


         if (firstElement instanceof IAdaptable)
         {
            selectedProject = (IResource) ((IAdaptable) firstElement).getAdapter(IResource.class);
            if (selectedProject != null)
            {
               projectPath = selectedProject.getProject().getLocation();
            }
         }
      }

   }


   @Override
   public void selectionChanged(IWorkbenchPart part, ISelection selection)
   {

      if (part != B2Wizard.this)
      {
         init(PlatformUI.getWorkbench(), (IStructuredSelection) selection);
      }

   }


   /**
    * deaktiviert den SelectionListener
    */
   public void dispose()
   {
      TreeviewerInput.clearArrayList();
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().removeSelectionListener(this);
      super.dispose();
   }


   public static IPath getPath()
   {
      return projectPath;
   }


}
