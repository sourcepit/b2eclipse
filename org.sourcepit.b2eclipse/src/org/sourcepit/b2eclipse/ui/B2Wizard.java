/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.sourcepit.b2eclipse.Activator;
import org.sourcepit.b2eclipse.input.TreeViewerInput;

/**
 * @author Marco Grupe
 */

public class B2Wizard extends Wizard implements IImportWizard, ISelectionListener
{


   private static IPath projectPath;
   private B2WizardPage modulePage;
   private List<File> projectList;
   private final IWorkspace workspace = ResourcesPlugin.getWorkspace();
   private IPath projectFile;
   private IProjectDescription projectDescription;
   private IProject project;
   private Object firstElement;
   private IResource selectedProject;
   private ProgressMonitorDialog progressMonitorDialog;
   private IRunnableWithProgress runnableWithProgress;
   static final String DIALOG_SETTING_FILE = "workingSets.xml";
   private DialogSettings dialogSettings;


   public B2Wizard()
   {
      super();
      setWindowTitle("Import");
      // setDefaultPageImageDescriptor()); Header �ndern
      modulePage = B2WizardPage.getInstance();

      dialogSettings = new DialogSettings("workingSets");
      
      File file = new File(DIALOG_SETTING_FILE);

      if (!file.exists())
      {
         try
         {
           
            file.createNewFile();
            dialogSettings.save(DIALOG_SETTING_FILE);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }

      try
      {
         dialogSettings.load(DIALOG_SETTING_FILE);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      setDialogSettings(dialogSettings);

      addPage(modulePage);


   }

   /**
    * While closing the wizard a message dialog opens to confirm {@inheritDoc}
    */
   public boolean performCancel()
   {
      boolean message = MessageDialog.openConfirm(getShell(), "Confirm Close", "Are you sure you want to close?");
      if (message)
         return true;
      return false;
   }


   /**
    * After pressing the finish button the selected projects will be create {@inheritDoc}
    */
   @Override
   public boolean performFinish()
   {

      try
      {
         // Saves the dialog settings into the specified file.
         getDialogSettings().save(DIALOG_SETTING_FILE);
      }
      catch (IOException e1)
      {
         e1.printStackTrace();
      }

      projectList = modulePage.getSelectedProjects();

      runnableWithProgress = new IRunnableWithProgress()
      {

         @Override
         public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
         {

            monitor.beginTask("Creating projects", projectList.size());

            for (int i = 0; i < projectList.size(); i++)
            {
               if (monitor.isCanceled())
                  return;
               Thread.sleep(250);
               monitor.subTask("Working on " + projectList.get(i).getParent());
               createProjects(i);
               monitor.worked(1);
            }
            monitor.done();
         }
      };


      progressMonitorDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
      try
      {
         progressMonitorDialog.run(true, true, runnableWithProgress);
      }
      catch (InvocationTargetException e)
      {
         Activator.error(e);
      }
      catch (InterruptedException e)
      {
         Activator.error(e);
      }

      return true;
   }


   /**
    * By clicking project in the package explorer firstElement gets the absolute path of the selected project
    * {@inheritDoc}
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
    * disposes the SelectionListener
    */
   public void dispose()
   {
      TreeViewerInput.clearArrayList();
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().removeSelectionListener(this);
      super.dispose();
   }


   public static IPath getPath()
   {
      return projectPath;
   }

   private void createProjects(int projectsListPosition)
   {

      try
      {
         projectFile = new Path(String.valueOf(projectList.get(projectsListPosition)));
         projectDescription = workspace.loadProjectDescription(projectFile);
         project = workspace.getRoot().getProject(projectDescription.getName());
         JavaCapabilityConfigurationPage.createProject(project, projectDescription.getLocationURI(), null);

         if (modulePage.getCheckButtonSelection() && modulePage.getWorkingSet() != null)
         {
            modulePage.getWorkingSetManager().addToWorkingSets(project, modulePage.getWorkingSet());
         }
      }
      catch (CoreException e)
      {
         Activator.error(e);
      }

   }


}
