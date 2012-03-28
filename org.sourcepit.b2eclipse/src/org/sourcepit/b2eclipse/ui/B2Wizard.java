
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
import org.sourcepit.b2eclipse.provider.TreeContentProvider;


public class B2Wizard extends Wizard implements IImportWizard, ISelectionListener
{


   private static IPath projectPath;
   private WizardPageOne modulePage;
   IProject project;

   public B2Wizard()
   {
      super();
      setWindowTitle("Import b2 Projects");
      modulePage = new WizardPageOne("Module");
      

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
               List<File> projects = modulePage.getSelectedProjects();

               for (int i = 0; i < projects.size(); i++)
               {

                  final IWorkspace workspace = ResourcesPlugin.getWorkspace();
                  IPath projectDotProjectFile = new Path(String.valueOf(projects.get(i)));
                  IProjectDescription projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
                  IProject project = workspace.getRoot().getProject(projectDescription.getName());
                  JavaCapabilityConfigurationPage.createProject(project, projectDescription.getLocationURI(), null);

                  if (modulePage.checkBtn.getSelection())
                  {
                     modulePage.workingSetManager.addToWorkingSets(project, modulePage.workingSet);
                  }


               }
            }
            catch (CoreException e)
            {
               e.printStackTrace();
            }
         }
      };


      final IWorkbench workbench = PlatformUI.getWorkbench();
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


         Object firstElement = selection.getFirstElement();


         if (firstElement instanceof IAdaptable)
         {
            IResource selectedProject = (IResource) ((IAdaptable) firstElement).getAdapter(IResource.class);
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
      TreeContentProvider.clearArrayList();
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().removeSelectionListener(this);
      super.dispose();
   }


   public static IPath getPath()
   {
      return projectPath;
   }


}
