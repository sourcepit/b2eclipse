/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;

import java.io.File;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.sourcepit.b2eclipse.input.ViewerInput;
import org.sourcepit.b2eclipse.input.node.Node;
import org.sourcepit.b2eclipse.input.node.NodeFolder;
import org.sourcepit.b2eclipse.input.node.NodeModule;
import org.sourcepit.b2eclipse.input.node.NodeModuleProject;
import org.sourcepit.b2eclipse.input.node.NodeProject;
import org.sourcepit.b2eclipse.input.node.NodeWorkingSet;
import org.sourcepit.b2eclipse.input.node.NodeProject.ProjectType;


/**
 * Handles the most UI operations.
 * 
 * @author WD
 * 
 */
@SuppressWarnings("restriction")
public class Backend
{

   private ViewerInput input;
   private String prevBrowsedDirectory;
   private boolean simpleMode;


   public Backend()
   {
      simpleMode = false;
      prevBrowsedDirectory = "";
   }

   /**
    * Checks or unchecks all checkable Elements in the Tree. 
    * 
    * @param viewer
    * @param state check or not?
    */
   public void doCheck(CheckboxTreeViewer viewer, boolean state)
   {
      for (Node dad : ((Node) viewer.getInput()).getChildren())
      {
         if (!dad.hasConflict())
            viewer.setSubtreeChecked(dad, state);
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
    * Delete the <code>project</code> from the <code>previevTreeViever</code>.
    * 
    * @param previevTreeViever
    * @param node
    */
   public void deleteFromPrevievTree(TreeViewer previewTreeViewer, Node node)
   {
      if (node instanceof NodeProject || node instanceof NodeModuleProject) // Should always be true
      {
         Node imDead = ((Node) previewTreeViewer.getInput()).getEqualNode(node.getFile());

         if (imDead != null)
         {
            Node deadDad = imDead.getRootModule();
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
    * @param node
    */
   public void addToPrevievTree(TreeViewer previewTreeViewer, Node node)
   {

      Node root = (Node) previewTreeViewer.getInput();
      boolean created = false;

      Node parent = node.getParent();

      if (node instanceof NodeModule)
         parent = node;

      if (simpleMode)
      {
         if (parent instanceof NodeFolder)
            parent = node.getParent().getParent();
      }

      String wsName = getWSName(parent);

      for (Node iter : root.getChildren())
      {
         if (iter instanceof NodeWorkingSet)
         {
            if (iter.getName().equals(wsName))
            {
               if (root.getEqualNode(node.getFile()) == null)
               {
                  if (node instanceof NodeProject)
                  {
                     new NodeProject(iter, node.getFile(), ProjectType.PWS);
                     created = true;
                     break;
                  }
                  if (node instanceof NodeModuleProject)
                  {
                     new NodeModuleProject(iter, node.getFile(), node.getName());
                     created = true;
                     break;
                  }
               }
            }
         }
      }

      if (!created)
      {
         if (root.getEqualNode(node.getFile()) == null)
         {
            if (node instanceof NodeProject)
            {
               new NodeProject(new NodeWorkingSet(root, getWSName(parent)), node.getFile(), ProjectType.PWS);
            }
            if (node instanceof NodeModuleProject)
            {
               new NodeModuleProject(new NodeWorkingSet(root, getWSName(parent)), node.getFile(), node.getName());
            }
         }
      }

      previewTreeViewer.refresh();

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
   public void handleDirTreeViewer(CheckboxTreeViewer treeViewer, String txt)
   {
      input = new ViewerInput(new Node());

      treeViewer.setInput(input.createMainNodeSystem(new File(txt)));
      treeViewer.expandAll();

      checkProjectConflicts(treeViewer);

      treeViewer.refresh();
   }

   /**
    * Update the <code>PreviewViewer</code>, considering the checked elements in the <code>DirViewer</code>.
    * 
    * @param viewer the PreviewViewer
    * @param treeViewer the DirViewer
    */
   public void refreshPreviewViewer(CheckboxTreeViewer dirTreeViewer, TreeViewer previewTreeViewer)
   {
      Node root = (Node) dirTreeViewer.getInput();

      // TODO simple mode ...
      // TODO prefix ...

      // Create Preview Nodes
      for (Node top : root.getChildren())
      {
         for (Node iter : top.getAllSubNodes())
         {
            if (!iter.hasConflict())
               dirTreeViewer.setChecked(iter, true);
         }
         if (top instanceof NodeProject || top instanceof NodeModuleProject)
            if (!top.hasConflict())
               addToPrevievTree(previewTreeViewer, top);
         for (Node iter : top.getAllSubNodes())
         {
            if (iter instanceof NodeProject || iter instanceof NodeModuleProject)
               if (!iter.hasConflict())
                  addToPrevievTree(previewTreeViewer, iter);
         }
      }
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
    * Returns the Name for a Working Set.
    * 
    * @param node
    * @return
    */
   public String getWSName(Node node)
   {
      // If a Project Node was given
      if (node instanceof NodeProject || node instanceof NodeModuleProject)
         node = node.getParent();

      String name = "";

      while (node.getParent() != null)
      {
         if (node.getName() != null)
         {
            // Prefix check on Module Nodes
            if (node instanceof NodeModule)
            {
               String fix = ((NodeModule) node).getPrefix();
               if (fix != null)
                  name = "/" + fix + name;
               else
                  name = "/" + node.getName() + name;
            }
            else
               name = "/" + node.getName() + name;
         }
         node = node.getParent();
      }

      // Substring because the first "/"
      return name.substring(1);
   }

   public String showInputDialog(Shell dialogShell)
   {
      InputDialog dialog = new InputDialog(dialogShell, Messages.msgInDialogTitle, Messages.msgInDialogMessage, null,
         null);
      if (dialog.open() == Window.OK)
         return dialog.getValue();
      else
         return null;
   }

   public void checkProjectConflicts(CheckboxTreeViewer treeViewer)
   {
      for (Node node : ((Node) treeViewer.getInput()).getAllSubNodes())
      {
         if (node instanceof NodeProject || node instanceof NodeModuleProject)
         {
            for (IProject iter : ResourcesPlugin.getWorkspace().getRoot().getProjects())
            {
               // check before import new Projects
               if (iter.getName().equals(node.getName()))
               {
                  treeViewer.setChecked(node, false);
                  node.setConflict();
               }
            }
         }
      }
   }
}
