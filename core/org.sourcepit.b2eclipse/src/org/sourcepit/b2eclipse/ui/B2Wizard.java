/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;

import java.io.File;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.sourcepit.b2eclipse.Activator;
import org.sourcepit.b2eclipse.input.Node;
import org.sourcepit.b2eclipse.input.PreviewViewerInput;
import org.sourcepit.b2eclipse.input.TreeViewerInput;

/**
 * @author WD
 */
@SuppressWarnings("restriction")
public class B2Wizard extends Wizard implements IImportWizard, ISelectionListener
{
   private static String previouslyBrowsedDirectory = "";
   
   public B2Wizard()
   {
      super();
      addPage(new B2WizardPage(Messages.msgImportHeader, this));
   }

   public void init(IWorkbench workbench, IStructuredSelection selection)
   {
      //TODO diese Methode wird nach dem Konstruktor aufgerufen und vor allen anderen methoden
      setWindowTitle(Messages.msgImportTitle);
      Image projectFolder = Activator.getImageFromPath("icons/ProjectFolder.gif");
      setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(projectFolder));
      
      //TODO das ist warscheinlich um dann die Projekte in workbench zu adden
      //deswegen auch der ISelectionListener + die selectionChanged methode
      workbench.getActiveWorkbenchWindow().getSelectionService().addSelectionListener(this);
   }

   @Override
   public boolean performFinish()
   {
      //TODO do smth.
      return false;
   }

   public void doCheck(CheckboxTreeViewer viewer, boolean state)
   {
      for (Node dad : ((Node) viewer.getInput()).getChildren())
      {
         viewer.setSubtreeChecked(dad, state);
      }
      // Idee: nur wenn im Modul Projecte vorhanden sind, markieren
   }

   public void showElementsInTree(TreeViewer previevTreeViever, Node node)
   {
      ((Node) previevTreeViever.getInput()).getEqualNode(node);
      
      
      
      // TODO zeigt Die Elemente die von der Direc. des Textfeldes markiert sind im Treeviever links
   }

   public void addNewWorkingSetNode()
   {
      // TODO legt ein neues Working Set Node an
   }

   public void removeSelectedWorkingSetNode()
   {
      // TODO löscht markiertes Working Set Node
   }

   // Cheken ob das parent null is
   public boolean testOnLocalDrive(String selectedProject)
   {
      return !new File(selectedProject).getParentFile().equals(null);
   }

   public String showDirectorySelectDialog(String directoryName, Shell dialogShell)
   {

      DirectoryDialog directoryDialog = new DirectoryDialog(dialogShell, SWT.OPEN);

      directoryDialog.setText(Messages.msgSelectDirTitle);

      directoryName = directoryName.trim();
      if (directoryName.length() == 0)
      {

         if (previouslyBrowsedDirectory.length() == 0)
         {
            directoryName = IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getLocation().toOSString();
         }
         else
         {
            directoryName = previouslyBrowsedDirectory;
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
            previouslyBrowsedDirectory = selectedDirectory;
            return selectedDirectory;
         }
      }
      return "";
   }

   public String showWorkspaceSelectDialog(Shell dialogShell)
   {
      ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(dialogShell, new WorkbenchLabelProvider(),
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

   public void handleDirTreeViever(CheckboxTreeViewer treeViewer, TreeViewer previevTreeViever, String txt)
   {
      Node root = new Node();
      
      treeViewer.setInput(new TreeViewerInput(root, new File(txt)));
      treeViewer.expandToLevel(2);
      doCheck(treeViewer, true);

      previevTreeViever.setInput(new PreviewViewerInput(root));

   }

   public void selectionChanged(IWorkbenchPart arg0, ISelection arg1)
   {
      // TODO Auto-generated method stub
      
   }

}
