
package org.sourcepit.b2eclipse.ui;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.sourcepit.b2eclipse.provider.TreeContentProvider;


public class b2Wizard extends Wizard implements IImportWizard, ISelectionListener
{


   private static IPath projectPath;
   private WizardPageOne modulePage;


   public b2Wizard()
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

      if (part != b2Wizard.this)
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
