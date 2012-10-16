/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.sourcepit.b2eclipse.dnd.DragListener;
import org.sourcepit.b2eclipse.dnd.DropListener;
import org.sourcepit.b2eclipse.input.node.Node;
import org.sourcepit.b2eclipse.input.node.NodeModule;
import org.sourcepit.b2eclipse.input.node.NodeProject;
import org.sourcepit.b2eclipse.input.node.NodeWorkingSet;
import org.sourcepit.b2eclipse.provider.LabelProvider;
import org.sourcepit.b2eclipse.provider.ContentProvider;

/**
 * @author WD
 */
public class B2WizardPage extends WizardPage
{
   private Button dirRadioBtn;
   private Text dirTxt;
   private Button dirBtn;

   private Button workspaceRadioBtn;
   private Text workspaceTxt;
   private Button workspaceBtn;

   private CheckboxTreeViewer dirTreeViewer;
   private TreeViewer previewTreeViewer;

   private ToolItem refresh;
   private ToolItem selAll;
   private ToolItem addPrefix;
   private ToolItem add;
   private ToolItem delete;
   private ToolItem toggleMode;
   private ToolItem expandAll;

   private Backend bckend;
   private Shell dialogShell;
   private IStructuredSelection preSelect;
   private String currentDirectory;


   protected B2WizardPage(String pageName, B2Wizard parent, IStructuredSelection selection)
   {
      super(pageName);
      setPageComplete(false);
      setTitle(Messages.msgImportHeader);
      setDescription(Messages.msgImportSuperscription);
      bckend = new Backend();
      preSelect = selection;
   }

   public void createControl(Composite parent)
   {

      initializeDialogUnits(parent);

      dialogShell = parent.getShell();

      Composite widgetContainer = new Composite(parent, SWT.NONE);
      widgetContainer.setLayout(new GridLayout());

      createFileChooserArea(widgetContainer);
      createViewArea(widgetContainer);

      addListeners();

      setControl(widgetContainer);

   }

   private void createFileChooserArea(Composite area)
   {
      Composite container = new Composite(area, SWT.NONE);
      container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      container.setLayout(new GridLayout(3, false));

      dirRadioBtn = new Button(container, SWT.RADIO);
      dirRadioBtn.setText(Messages.msgSelectRootRbtn);

      dirTxt = new Text(container, SWT.BORDER);
      dirTxt.setToolTipText(Messages.msgSelectRootTt);
      dirTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      dirBtn = new Button(container, SWT.PUSH);
      dirBtn.setText(Messages.msgBrowseBtn);
      setButtonLayoutData(dirBtn);

      workspaceRadioBtn = new Button(container, SWT.RADIO);
      workspaceRadioBtn.setText(Messages.msgSelectWorkspaceRbtn);

      workspaceTxt = new Text(container, SWT.BORDER);
      workspaceTxt.setToolTipText(Messages.msgSelectWorkspaceTt);
      workspaceTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      workspaceBtn = new Button(container, SWT.PUSH);
      workspaceBtn.setText(Messages.msgBrowseBtn);
      setButtonLayoutData(workspaceBtn);
   }

   private void createViewArea(Composite area)
   {
      GridLayout layout;

      Composite container = new Composite(area, SWT.NONE);

      GridData data = new GridData(GridData.FILL_BOTH);
      data.widthHint = new PixelConverter(new FontRegistry().defaultFont()).convertWidthInCharsToPixels(150);
      data.heightHint = new PixelConverter(new FontRegistry().defaultFont()).convertHeightInCharsToPixels(35);
      container.setLayoutData(data);

      layout = new GridLayout(2, true);
      layout.marginWidth = 0;
      container.setLayout(layout);


      // The CheckboxTreeViever on left side
      Composite leftContainer = new Composite(container, SWT.BORDER);
      leftContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

      layout = new GridLayout(1, false);
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      layout.verticalSpacing = 0;
      leftContainer.setLayout(layout);

      ToolBar toolBarLeft = new ToolBar(leftContainer, (SWT.HORIZONTAL | SWT.NONE));
      toolBarLeft.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      dirTreeViewer = new CheckboxTreeViewer(leftContainer, SWT.NONE);
      dirTreeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

      dirTreeViewer.setContentProvider(new ContentProvider());
      dirTreeViewer.setLabelProvider(new LabelProvider());

      refresh = new ToolItem(toolBarLeft, SWT.PUSH);
      refresh.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.jdt.ui",
         "$nl$/icons/full/elcl16/refresh.gif").createImage());
      refresh.setToolTipText(Messages.msgRestoreTt);

      selAll = new ToolItem(toolBarLeft, SWT.CHECK);
      selAll.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui",
         "$nl$/icons/full/elcl16/step_done.gif").createImage());
      selAll.setToolTipText(Messages.msgSelectDeselectTt);

      addPrefix = new ToolItem(toolBarLeft, SWT.PUSH);
      addPrefix.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.jdt.ui",
         "$nl$/icons/full/obj16/change.gif").createImage());
      // TODO Tooltip
      addPrefix.setEnabled(false);

      // The preview TreeViewer on right side
      Composite rightContainer = new Composite(container, SWT.BORDER);
      rightContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

      layout = new GridLayout(1, false);
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      layout.verticalSpacing = 0;
      rightContainer.setLayout(layout);

      ToolBar toolBarRight = new ToolBar(rightContainer, (SWT.HORIZONTAL | SWT.NONE));
      toolBarRight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      previewTreeViewer = new TreeViewer(rightContainer, SWT.NONE | SWT.MULTI);
      previewTreeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

      previewTreeViewer.setContentProvider(new ContentProvider());
      previewTreeViewer.setLabelProvider(new LabelProvider());

      Transfer[] transfer = new Transfer[] { FileTransfer.getInstance() };
      previewTreeViewer.addDragSupport(DND.DROP_MOVE, transfer, new DragListener(previewTreeViewer));
      previewTreeViewer.addDropSupport(DND.DROP_MOVE, transfer, new DropListener(previewTreeViewer));

      add = new ToolItem(toolBarRight, SWT.PUSH);
      add.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD));
      add.setToolTipText(Messages.msgAddNewWSTt);

      delete = new ToolItem(toolBarRight, SWT.PUSH);
      delete.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
      delete.setToolTipText(Messages.msgDelWSTt);
      delete.setEnabled(false);

      expandAll = new ToolItem(toolBarRight, SWT.PUSH);
      expandAll.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui",
         "$nl$/icons/full/elcl16/expandall.gif").createImage());
      expandAll.setToolTipText(Messages.msgExpandAllTt);

      toggleMode = new ToolItem(toolBarRight, SWT.CHECK);
      toggleMode.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_UP));
      toggleMode.setToolTipText(Messages.msgToggleModeTt);

      // Comparator for the Viewers
      ViewerComparator compa = new ViewerComparator()
      {
         @SuppressWarnings("unchecked")
         public int compare(Viewer viewer, Object o1, Object o2)
         {
            Node n1 = ((Node) o1);
            Node n2 = ((Node) o2);

            if (n1 instanceof NodeProject && n2 instanceof NodeProject)
            {
               return getComparator().compare(n1.getName(), n2.getName());
            }
            if (n1 instanceof NodeProject)
            {
               return o2.hashCode();
            }
            if (n2 instanceof NodeProject)
            {
               return o1.hashCode();
            }
            return getComparator().compare(n1.getName(), n2.getName());
         }
      };
      previewTreeViewer.setComparator(compa);
      dirTreeViewer.setComparator(compa);

      preSelect();
   }

   /**
    * Will select an Directory and put it in the correct Widget, if any directory was given.
    */
   private void preSelect()
   {
      if (!preSelect.isEmpty())
      {
         currentDirectory = "";
         if (preSelect.getFirstElement() instanceof IResource)
         {
            dirRadioBtn.setSelection(false);
            dirTxt.setEnabled(false);
            dirBtn.setEnabled(false);
            workspaceRadioBtn.setSelection(true);
            workspaceTxt.setEnabled(true);
            workspaceBtn.setEnabled(true);
            currentDirectory = ((IResource) preSelect.getFirstElement()).getLocation().toString();
            workspaceTxt.setText(currentDirectory);
         }
         if (preSelect.getFirstElement() instanceof File)
         {
            dirRadioBtn.setSelection(true);
            dirTxt.setEnabled(true);
            dirBtn.setEnabled(true);
            workspaceRadioBtn.setSelection(false);
            workspaceTxt.setEnabled(false);
            workspaceBtn.setEnabled(false);
            currentDirectory = ((File) preSelect.getFirstElement()).getPath();
            dirTxt.setText(currentDirectory);
         }

         if (bckend.testOnLocalDrive(currentDirectory))
         {
            bckend.handleTreeViewers(dirTreeViewer, previewTreeViewer, currentDirectory);
            selAll.setSelection(true);
            setPageComplete(true);
         }
      }
      else
      {
         dirRadioBtn.setSelection(true);
         workspaceTxt.setEnabled(false);
         workspaceBtn.setEnabled(false);
      }
   }

   private void addListeners()
   {
      dirBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            workspaceTxt.setText("");
            dirTxt.setText(bckend.showDirectorySelectDialog(dirTxt.getText(), dialogShell));

         }
      });

      workspaceBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            dirTxt.setText("");
            workspaceTxt.setText(bckend.showWorkspaceSelectDialog(dialogShell));

         }
      });

      dirRadioBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            if (dirRadioBtn.isEnabled())
            {
               dirTxt.setEnabled(true);
               dirBtn.setEnabled(true);
               workspaceTxt.setEnabled(false);
               workspaceBtn.setEnabled(false);
            }
         }
      });

      workspaceRadioBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            if (workspaceRadioBtn.isEnabled())
            {
               workspaceTxt.setEnabled(true);
               workspaceBtn.setEnabled(true);
               dirTxt.setEnabled(false);
               dirBtn.setEnabled(false);
            }
         }
      });

      // Listener for the Texts
      ModifyListener modLis = new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            currentDirectory = ((Text) e.widget).getText();

            if (bckend.testOnLocalDrive(currentDirectory))
            {
               bckend.handleTreeViewers(dirTreeViewer, previewTreeViewer, currentDirectory);

               selAll.setSelection(true);

               setPageComplete(true);
            }
         }
      };
      dirTxt.addModifyListener(modLis);
      workspaceTxt.addModifyListener(modLis);

      refresh.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            bckend.handleTreeViewers(dirTreeViewer, previewTreeViewer, currentDirectory);
         }
      });

      selAll.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            if (selAll.getSelection())
            {
               // check All
               bckend.doCheck(dirTreeViewer, true);
            }
            else
            {
               // un-check All
               bckend.doCheck(dirTreeViewer, false);
            }
            bckend.refreshPreviewViewer(previewTreeViewer, dirTreeViewer);
         }
      });

      addPrefix.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            NodeModule selected = (NodeModule) ((IStructuredSelection) dirTreeViewer.getSelection()).getFirstElement();
            if (selected != null)
            {
               if (selected.getPrefix() == null)
               {
                  ((NodeModule) selected).setPrefix(bckend.showInputDialog(dialogShell));
                  bckend.refreshPreviewViewer(previewTreeViewer, dirTreeViewer);
               }
               else
               {
                  ((NodeModule) selected).setPrefix(null);
                  bckend.refreshPreviewViewer(previewTreeViewer, dirTreeViewer);
               }
            }
            dirTreeViewer.refresh();
         }
      });


      // adds a new working set
      add.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            new NodeWorkingSet((Node) previewTreeViewer.getInput(), Messages.msgDefaultWSName);
            previewTreeViewer.refresh();
         }
      });

      // deletes the selected working set
      delete.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            Node selected = (Node) ((IStructuredSelection) previewTreeViewer.getSelection()).getFirstElement();
            if (selected instanceof NodeWorkingSet)
               selected.deleteNodeAssigningChildrenToParent();

            previewTreeViewer.refresh();
         }
      });

      expandAll.addListener(SWT.Selection, new Listener()
      {

         public void handleEvent(Event event)
         {
            previewTreeViewer.expandAll();
         }

      });

      toggleMode.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            if (toggleMode.getSelection())
            {
               bckend.setPreviewMode(true);
               bckend.refreshPreviewViewer(previewTreeViewer, dirTreeViewer);
            }
            else
            {
               bckend.setPreviewMode(false);
               bckend.refreshPreviewViewer(previewTreeViewer, dirTreeViewer);
            }
         }
      });

      dirTreeViewer.addSelectionChangedListener(new ISelectionChangedListener()
      {
         public void selectionChanged(SelectionChangedEvent event)
         {
            Node selected = (Node) ((IStructuredSelection) event.getSelection()).getFirstElement();
            if (selected != null && selected instanceof NodeModule)
               addPrefix.setEnabled(true);
            else
               addPrefix.setEnabled(false);
         }
      });

      previewTreeViewer.addSelectionChangedListener(new ISelectionChangedListener()
      {
         public void selectionChanged(SelectionChangedEvent event)
         {
            Node selected = (Node) ((IStructuredSelection) event.getSelection()).getFirstElement();
            if (selected != null && selected instanceof NodeWorkingSet)
               delete.setEnabled(true);
            else
               delete.setEnabled(false);
         }
      });

      // user can change the name of a WorkingSet after double click on it
      previewTreeViewer.addDoubleClickListener(new IDoubleClickListener()
      {
         public void doubleClick(DoubleClickEvent event)
         {
            setPageComplete(false);
            final Node node = (Node) ((IStructuredSelection) event.getSelection()).getFirstElement();

            final TreeEditor editor = new TreeEditor(previewTreeViewer.getTree());
            editor.horizontalAlignment = SWT.LEFT;
            editor.grabHorizontal = true;

            final TreeItem item = previewTreeViewer.getTree().getSelection()[0];
            final Text txt = new Text(previewTreeViewer.getTree(), SWT.NONE);
            txt.setText(node.getName());
            txt.selectAll();
            txt.setFocus();

            txt.addFocusListener(new FocusListener()
            {
               public void focusLost(FocusEvent e)
               {
                  node.setName(txt.getText());
                  txt.dispose();
                  previewTreeViewer.refresh();
                  setPageComplete(true);
               }

               public void focusGained(FocusEvent e)
               {
               }
            });

            txt.addKeyListener(new KeyListener()
            {
               public void keyPressed(KeyEvent e)
               {
                  switch (e.keyCode)
                  {
                     case SWT.CR :
                        node.setName(txt.getText());
                     case SWT.ESC :
                        txt.dispose();
                        previewTreeViewer.refresh();
                        setPageComplete(true);
                        break;
                  }
               }

               public void keyReleased(KeyEvent e)
               {
               }

            });
            editor.setEditor(txt, item);
         }
      });


      // if a category is checked in the tree, check all its children
      // handles also the appear/disappear of elements in the preview TreeViewer
      dirTreeViewer.addCheckStateListener(new ICheckStateListener()
      {
         public void checkStateChanged(CheckStateChangedEvent event)
         {
            Node elementNode = (Node) event.getElement();

            if (event.getChecked())
            {
               dirTreeViewer.setSubtreeChecked(elementNode, true);

//               bckend.refreshPreviewViewer(previewTreeViewer, dirTreeViewer);
               if (elementNode instanceof NodeProject)
                  bckend.addProjectToPrevievTree(previewTreeViewer, elementNode);
               else
                  for (Node iter : elementNode.getProjectChildren())
                     bckend.addProjectToPrevievTree(previewTreeViewer, iter);

            }
            else
            {
               dirTreeViewer.setSubtreeChecked(elementNode, false);
               // TODO eyeCandy: if all sub elements are unchecked, uncheck the element
               // TODO or remove the WS only if the Module is unchecked
//               bckend.refreshPreviewViewer(previewTreeViewer, dirTreeViewer);

//               if (elementNode instanceof NodeProject )
//                  bckend.deleteProjectFromPrevievTree(previewTreeViewer, elementNode);
//               else
                  for (Node iter : elementNode.getAllSubNodes())
                     bckend.deleteProjectFromPrevievTree(previewTreeViewer, iter);
                  bckend.deleteProjectFromPrevievTree(previewTreeViewer, elementNode);
               selAll.setSelection(false);
            }
         }
      });
   }

   /**
    * @return the root Node of the Preview Viewer
    */
   public Node getPreviewRootNode()
   {
      return (Node) previewTreeViewer.getInput();
   }
}
