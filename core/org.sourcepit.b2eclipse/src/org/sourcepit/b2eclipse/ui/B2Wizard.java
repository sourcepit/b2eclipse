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
import org.eclipse.core.resources.IResource;
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
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.sourcepit.b2eclipse.Activator;
import org.sourcepit.b2eclipse.input.Node;
import org.sourcepit.b2eclipse.input.ViewerInput;

/**
 * @author WD
 */
@SuppressWarnings("restriction")
public class B2Wizard extends Wizard implements IImportWizard
{
   private B2WizardPage page;
   private ViewerInput input;
   private String prevBrowsedDirectory;
   private boolean simpleMode;

   public B2Wizard()
   {
      super();
   }

   public void init(IWorkbench workbench, IStructuredSelection selection)
   {
      simpleMode = false;
      prevBrowsedDirectory = "";

      setWindowTitle(Messages.msgImportTitle);
      Image projectFolder = Activator.getImageFromPath("icons/ProjectFolder.gif");
      setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(projectFolder));

      setHelpAvailable(false);
      setNeedsProgressMonitor(true);

      page = new B2WizardPage(Messages.msgImportHeader, this, selection);
      addPage(page);
   }

   /**
    * Checks or unchecks all Elements in the Tree.
    * 
    * @param viewer
    * @param state check or not?
    */
   public void doCheck(CheckboxTreeViewer viewer, boolean state)
   {
      for (Node dad : ((Node) viewer.getInput()).getChildren())
      {
         viewer.setSubtreeChecked(dad, state);
      }
   }


   /**
    * Delete the <code>project</code> from the <code>previevTreeViever</code>.
    * 
    * @param previevTreeViever
    * @param project
    */
   public void deleteProjectFromPrevievTree(TreeViewer previewTreeViewer, Node project)
   {
      if (project.getType() == Node.Type.PROJECT) // Should always be true
      {
         Node imDead = ((Node) previewTreeViewer.getInput()).getEqualNode(project.getFile());

         if (imDead != null)
         {
            Node deadDad = imDead.getRootModel();
            imDead.deleteNode();

            if (deadDad.getChildren().size() == 0)
            {
               deadDad.deleteNode();
            }
         }
         previewTreeViewer.refresh();
      }
   }

   /**
    * Add the <code>project</code> to the <code>previevTreeViever</code>. Also it checks if there is a corresponding
    * Working Set for this Project, if not it also creates it.
    * 
    * @param previevTreeViever
    * @param project
    */
   public void addProjectToPrevievTree(TreeViewer previewTreeViewer, Node project)
   {
      if (project.getType() == Node.Type.PROJECT) // Should always be true
      {
         Node root = (Node) previewTreeViewer.getInput();
         boolean created = false;

         Node parent = project.getParent();
         if (simpleMode)
         {
            if (parent.getType() == Node.Type.FOLDER)
               parent = project.getParent().getParent();
         }

         String wsName = project.getWSName(parent);

         for (Node iter : root.getChildren())
         {
            if (iter.getType() == Node.Type.WORKINGSET)
            {
               if (iter.getName().equals(wsName))
               {
                  new Node(iter, project.getFile(), project.getType());
                  created = true;
                  break;
               }
            }
         }

         if (!created)
         {
            new Node(new Node(root, parent.getFile(), Node.Type.WORKINGSET, wsName), project.getFile(),
               project.getType());
         }

         previewTreeViewer.refresh();
      }
   }

   /**
    * Checks if the parent file is null.
    * 
    * @param selectedProject
    * @return true if the parent file is not null, else false.
    */
   public boolean testOnLocalDrive(String selectedProject)
   {
      if (selectedProject != null)
         if (new File(selectedProject).getParentFile() != null)
            return true;

      return false;
   }

   /**
    * Shows a directory select dialog.
    * 
    * @param directoryName
    * @param dialogShell
    * @return the chosen Directory or ""
    */
   public String showDirectorySelectDialog(String directoryName, Shell parent)
   {

      DirectoryDialog directoryDialog = new DirectoryDialog(parent, SWT.OPEN);

      directoryDialog.setText(Messages.msgSelectDirTitle);

      directoryName = directoryName.trim();
      if (directoryName.length() == 0)
      {

         if (prevBrowsedDirectory.length() == 0)
         {
            directoryName = IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getLocation().toOSString();
         }
         else
         {
            directoryName = prevBrowsedDirectory;
         }
      }
      if (!new File(directoryName).exists())
      {
         directoryName = IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getLocation().toOSString();
      }

      directoryDialog.setFilterPath(new Path(directoryName).toOSString());

      String selectedDirectory = directoryDialog.open();
      if (selectedDirectory != null)
      {
         if (testOnLocalDrive(selectedDirectory))
         {
            prevBrowsedDirectory = selectedDirectory;
            return selectedDirectory;
         }
      }
      return "";
   }

   /**
    * Shows a workspace select dialog.
    * 
    * @param dialogShell
    * @return the chosen Directory or ""
    */
   public String showWorkspaceSelectDialog(Shell parent)
   {
      ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(parent, new WorkbenchLabelProvider(),
         new BaseWorkbenchContentProvider());
      dialog.setTitle(Messages.msgSelectProjectTitle);
      dialog.setMessage(Messages.msgSelectProject);
      dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
      dialog.open();
      if (dialog.getFirstResult() != null)
      {
         String selectedProject = String.valueOf(((IResource) dialog.getFirstResult()).getLocation());

         if (testOnLocalDrive(selectedProject))
         {
            return selectedProject;
         }
      }
      return "";
   }

   /**
    * Handles a change in the directory Field, actual it sets a new input to the treeViewer's.
    * 
    * @param treeViewer
    * @param previewTreeViewer
    * @param txt
    */
   public void handleDirTreeViewer(CheckboxTreeViewer treeViewer, TreeViewer previewTreeViewer, String txt)
   {
      input = new ViewerInput(new Node());

      treeViewer.setInput(input.createMainNodeSystem(new File(txt)));
      treeViewer.expandToLevel(2);
      doCheck(treeViewer, true);

      previewTreeViewer.setInput(input.createNodeSystemForPreview(simpleMode, treeViewer));

      treeViewer.refresh();
      previewTreeViewer.refresh();
   }

   /**
    * Sets the <code>simpleMode</code> flag, if in simple mode, ordinary folders will not be added as a WS entry in the
    * <code>PreviewViewer</code>.
    * 
    * @param _simpleMode
    */
   public void setPreviewMode(boolean _simpleMode)
   {
      simpleMode = _simpleMode;
   }

   /**
    * Update the <code>PreviewViewer</code>, considering the checked elements in the <code>DirViewer</code>.
    * 
    * @param viewer the PreviewViewer
    * @param treeViewer the DirViewer
    */
   public void refreshPreviewViewer(TreeViewer viewer, CheckboxTreeViewer treeViewer)
   {
      if (input != null)
         viewer.setInput(input.createNodeSystemForPreview(simpleMode, treeViewer));

      viewer.refresh();
   }

   /**
    * Performs the finish of this Wizard. 
    *  
    * <br><br>{@inheritDoc}
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
            if (currentElement.getType() == Node.Type.WORKINGSET)
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
                  if (currentSubElement.getType() == Node.Type.PROJECT) // Should always be true
                  {
                     IProjectDescription projectDescription = workspace.loadProjectDescription(new Path(
                        currentSubElement.getFile().toString() + "/.project"));
                     IProject project = workspace.getRoot().getProject(projectDescription.getName());
                     JavaCapabilityConfigurationPage.createProject(project, projectDescription.getLocationURI(), null);

                     wSmanager.addToWorkingSets(project, new IWorkingSet[] { workingSet });
                     monitor.worked(1);
                  }
               }
            }
            if (currentElement.getType() == Node.Type.PROJECT)
            {
               monitor.subTask(currentElement.getName());
               IProjectDescription projectDescription = workspace.loadProjectDescription(new Path(currentElement
                  .getFile().toString() + "/.project"));
               IProject project = workspace.getRoot().getProject(projectDescription.getName());
               JavaCapabilityConfigurationPage.createProject(project, projectDescription.getLocationURI(), null);
               monitor.worked(1);
            }
         }
      }
      finally
      {
         monitor.done();
      }
   }
}
