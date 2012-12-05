/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;

import java.io.File;
import java.util.List;

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
import org.sourcepit.b2eclipse.input.node.WSNameValidator;

/**
 * Handles the most UI operations.
 * 
 * @author WD
 * 
 */
@SuppressWarnings("restriction")
public class Backend
{
   private WSNameValidator wsVal;

   private ViewerInput input;
   private String prevBrowsedDirectory;
   private Mode mode;
   private Mode previouseMode;
   private boolean highestMP;
   private boolean toggleNameState;

   public static enum Mode
   {
      onlyModule, moduleAndFolder, onlyFolder
   }

   public Backend(WSNameValidator wsVal)
   {
      this.wsVal = wsVal;
      highestMP = true;
      toggleNameState = true;
      mode = Mode.onlyModule;
      previouseMode = mode;
      prevBrowsedDirectory = "";
   }

   /**
    * Checks or unchecks all checkable Elements in the Tree.
    * 
    * @param viewer
    * @param state check or not?
    */
   public void doCheck(CheckboxTreeViewer viewer, Node root, boolean state)
   {
      if (root == null)
      {
         root = (Node) viewer.getInput();
      }
      for (Node aNode : root.getAllSubNodes())
      {
         if (!aNode.hasConflict())
         {
            viewer.setChecked(aNode, state);
         }


         if (aNode instanceof NodeModuleProject && highestMP)
         {
            viewer.setChecked(aNode, false);
         }
      }

      // Do it only while marking all, not while unmarking
      if (highestMP && state)
      {
         // Checks the highest ModuleProject Node
         List<Node> prnts = ((Node) viewer.getInput()).getChildren();
         if (prnts.size() > 0)
         {
            for (Node aNode : prnts.get(0).getChildren())
            {
               if (aNode instanceof NodeModuleProject && !aNode.hasConflict())
               {
                  viewer.setChecked(aNode, true);
               }
            }
         }
      }
   }

   public void doCheck(CheckboxTreeViewer viewer, boolean state, boolean highestMP)
   {
      this.highestMP = highestMP;
      doCheck(viewer, null, state);
   }


   // ------------------------------------------------------------------------
   public void checkModuleProjects(CheckboxTreeViewer viewer, boolean all)
   {
      Node root = (Node) viewer.getInput();
      for (Node aNode : root.getAllSubNodes())
      {
         if (!aNode.hasConflict())
         {
            viewer.setChecked(aNode, all);
         }

         if (aNode instanceof NodeModuleProject)
         {
            viewer.setChecked(aNode, false);
         }
      }

      // Checks the highest ModuleProject Node
      for (Node aNode : root.getChildren().get(0).getChildren())
      {
         if (aNode instanceof NodeModuleProject)
         {
            viewer.setChecked(aNode, true);
         }
      }
   }

   // ------------------------------------------------------------------------


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
    * Deletes the <code>node</code> from the <code>viewer</code>.
    * 
    * @param viewer
    * @param node
    */
   public void deleteFromPrevievTree(TreeViewer viewer, Node node)
   {
      if (node instanceof NodeProject || node instanceof NodeModuleProject) // Should
      // always
      // be
      // true
      {
         Node imDead = ((Node) viewer.getInput()).getEqualNode(node.getFile());

         if (imDead != null)
         {
            Node deadDad = imDead.getRootModule();
            imDead.deleteNode();

            // checks if there are nodes under the corresponding working set
            // node, if no deletes it
            if (deadDad.getChildren().size() == 0)
            {
               if (deadDad instanceof NodeWorkingSet)
               {
                  wsVal.removeFromlist(((NodeWorkingSet) deadDad).getLongName());
               }
               deadDad.deleteNode();

            }
         }
         viewer.refresh();
      }
   }

   /**
    * Add the <code>node</code> to the <code>viewer</code>. Also it checks if there is a corresponding Working Set for
    * this Project, if not it also creates it. In addition it handles the appearance of the preview, corresponding to
    * the <code>mode</code> field.
    * 
    * @param viewer
    * @param node from dirTreeViewer
    */
   public void addToPrevievTree(TreeViewer viewer, Node node)
   {
      Node root = (Node) viewer.getInput();
      Node parent = node.getParent();

      String wsName = getWSName(parent);

      String lastModuleName;
      if (parent instanceof NodeFolder)
      {
         lastModuleName = new String(parent.getParent().getName());
      }
      else
      {
         lastModuleName = parent.getName();
      }

      Node wsRoot = root;
      Boolean wsFind = false;

      switch (mode)
      {
         case moduleAndFolder :
            /* actually it does nothing */
            break;

         case onlyModule :
            if (parent instanceof NodeFolder)
            {
               parent = parent.getParent();
            }
            wsName = getWSName(parent);
            break;

         case onlyFolder :
            if (parent instanceof NodeFolder)
               wsName = parent.getName();
            else
               wsName = null;

            break;

         default :
            break;
      }

      // Add to root and search for existing Working Sets
      if (wsName != null)
      {
         for (Node iter : root.getChildren())
         {
            if (iter instanceof NodeWorkingSet)
            {
               if (((NodeWorkingSet) iter).getLongName().equals(wsName))
               {
                  wsRoot = iter;
                  wsFind = true;
                  break;
               }
            }
         }

         if (!wsFind)
            wsRoot = new NodeWorkingSet(root, wsVal.validate(wsName), lastModuleName);
      }

      if (root.getEqualNode(node.getFile()) == null)
      {
         if (node instanceof NodeProject)
            new NodeProject(wsRoot, node.getFile(), ProjectType.PWS);

         if (node instanceof NodeModuleProject)
            new NodeModuleProject(wsRoot, node.getFile(), node.getName());
      }

      if (toggleNameState)
      {
         ((NodeWorkingSet) wsRoot).setShortName();
      }

      viewer.refresh();
   }

   /**
    * Shows a directory select dialog.
    * 
    * @param directoryName
    * @param parent
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
    * @param parent
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
    * Handles a change in the directory Field, actual it sets a new input to the <code>viewer</code>.
    * 
    * @param viewer
    * @param txt
    */
   public void handleDirTreeViewer(CheckboxTreeViewer viewer, String txt)
   {
      input = new ViewerInput(new Node());

      Node inputNode = input.createMainNodeSystem(new File(txt));
      viewer.setInput(inputNode);

      // TODO maybe find out which to expand
      viewer.expandToLevel(2);

      checkProjectConflicts(viewer);

      viewer.refresh();
   }

   /**
    * Update the <code>previewTreeViewer</code>, considering the checked elements in the <code>dirTreeViewer</code>.
    * 
    * @param dirTreeViewer
    * @param previewTreeViewer
    */
   public void refreshPreviewViewer(CheckboxTreeViewer dirTreeViewer, TreeViewer previewTreeViewer, boolean refresh)
   {
      // if mode has changed, refresh preview
      if (previouseMode != mode || refresh)
      {
         previewTreeViewer.setInput(new Node());
         wsVal.clear();
         previouseMode = mode;
      }

      Node root = (Node) dirTreeViewer.getInput();

      // Create Preview Nodes
      for (Node iter : root.getAllSubNodes())
      {
         if (iter instanceof NodeProject || iter instanceof NodeModuleProject)
         {
            if (!iter.hasConflict() && dirTreeViewer.getChecked(iter))
            {
               addToPrevievTree(previewTreeViewer, iter);
            }
         }
      }
      previewTreeViewer.refresh();
   }

   public void refreshPreviewViewer(CheckboxTreeViewer dirTreeViewer, TreeViewer previewTreeViewer)
   {
      refreshPreviewViewer(dirTreeViewer, previewTreeViewer, false);
   }

   /**
    * Sets the <code>mode</code>.
    * 
    * @param _mode the new Mode
    */
   public void setPreviewMode(Mode _mode)
   {
      mode = _mode;
   }

   /**
    * Returns the Name for a Working Set.
    * 
    * @param node
    * @return the Name or "".
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

   /**
    * Shows a dialog where user can put in a Name.
    * 
    * @param parent
    * @return the Name or, null.
    */
   public String showInputDialog(Shell parent)
   {
      InputDialog dialog = new InputDialog(parent, Messages.msgInDialogTitle, Messages.msgInDialogMessage, null, null);
      if (dialog.open() == Window.OK)
         return dialog.getValue();
      else
         return null;
   }

   /**
    * Checks if there are conflicts. If there are it sets the conflict field in the corresponding node. A conflict
    * occur, if there is already a project in the workspace, with the same name as, the to be imported projects.
    * 
    * @param viewer
    */
   public void checkProjectConflicts(CheckboxTreeViewer viewer)
   {
      for (Node node : ((Node) viewer.getInput()).getAllSubNodes())
      {
         if (node instanceof NodeProject || node instanceof NodeModuleProject)
         {
            for (IProject iter : ResourcesPlugin.getWorkspace().getRoot().getProjects())
            {
               if (iter.getName().equals(node.getName()))
               {
                  viewer.setChecked(node, false);
                  node.setConflict();
               }
            }
         }
      }
   }

   public void toggleNaming(TreeViewer previewTreeViewer, boolean state)
   {
      toggleNameState = state;
      for (Node aNode : ((Node) previewTreeViewer.getInput()).getAllSubNodes())
      {
         if (aNode instanceof NodeWorkingSet)
         {
            if (state)
            {
               ((NodeWorkingSet) aNode).setShortName();
            }
            else
            {
               ((NodeWorkingSet) aNode).setLongName();

            }

         }

      }
   }
}
