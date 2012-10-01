/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;

import java.io.File;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
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
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.sourcepit.b2eclipse.input.Node;
import org.sourcepit.b2eclipse.provider.LabelProvider;
import org.sourcepit.b2eclipse.provider.ContentProvider;

/**
 * @author WD
 */
public class B2WizardPage extends WizardPage
{

   private Composite widgetContainer; // viell. nutzlos

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
   private ToolItem add;
   private ToolItem delete;

   private B2Wizard bckend;
   private Shell dialogShell;


   protected B2WizardPage(String pageName, B2Wizard parent)
   {
      super(pageName);
      setPageComplete(false);
      setTitle(Messages.msgImportHeader);
      setDescription(Messages.msgImportSuperscription);
      bckend = parent;
   }

   public void createControl(Composite parent)
   {

      initializeDialogUnits(parent);

      dialogShell = parent.getShell(); // überprüfen obs anders geht

      widgetContainer = new Composite(parent, SWT.NONE);
      widgetContainer.setLayout(new GridLayout());

      createFileChooserArea(widgetContainer);
      createVievArea(widgetContainer);

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
      dirRadioBtn.setSelection(true); // init mark

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
      workspaceTxt.setEnabled(false); // init mark

      workspaceBtn = new Button(container, SWT.PUSH);
      workspaceBtn.setText(Messages.msgBrowseBtn);
      setButtonLayoutData(workspaceBtn);
      workspaceBtn.setEnabled(false); // init mark
   }

   private void createVievArea(Composite area)
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


      // Der CheckboxTreeViever links
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

      leftContainer.setBackground(dirTreeViewer.getControl().getBackground());

      refresh = new ToolItem(toolBarLeft, SWT.PUSH);
      refresh.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.jdt.ui",
         "$nl$/icons/full/elcl16/refresh.gif").createImage());
      refresh.setToolTipText("refresh"); // TODO Alle Tooltips global machen


      selAll = new ToolItem(toolBarLeft, SWT.CHECK);
      selAll.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui",
         "$nl$/icons/full/elcl16/step_done.gif").createImage());
      selAll.setToolTipText("select/deselect All Projects");


      // Der previev Treeviever rechts
      Composite rightContainer = new Composite(container, SWT.BORDER);
      rightContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

      layout = new GridLayout(1, false);
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      layout.verticalSpacing = 0;
      rightContainer.setLayout(layout);

      ToolBar toolBarRight = new ToolBar(rightContainer, (SWT.HORIZONTAL | SWT.NONE));
      toolBarRight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      previewTreeViewer = new TreeViewer(rightContainer, SWT.NONE);
      previewTreeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

      previewTreeViewer.setContentProvider(new ContentProvider());
      previewTreeViewer.setLabelProvider(new LabelProvider());
      
      
      Transfer[] transferTypes = new Transfer[]{FileTransfer.getInstance()};
      previewTreeViewer.addDragSupport(DND.DROP_MOVE, transferTypes , new DragListener(previewTreeViewer));
      previewTreeViewer.addDropSupport(DND.DROP_MOVE, transferTypes , new DropListener(previewTreeViewer));

      rightContainer.setBackground(previewTreeViewer.getControl().getBackground());

      add = new ToolItem(toolBarRight, SWT.PUSH);
      add.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD));
      add.setToolTipText("add a new Java Working Set");

      delete = new ToolItem(toolBarRight, SWT.PUSH);
      delete.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
      delete.setToolTipText("delete selected Working Set");

   }

   private void addListeners()
   {
      dirBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            dirTxt.setText(bckend.showDirectorySelectDialog(dirTxt.getText(), dialogShell));
            workspaceTxt.setText("");
         }
      });

      workspaceBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            workspaceTxt.setText(bckend.showWorkspaceSelectDialog(dialogShell));
            dirTxt.setText("");
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

      dirTxt.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            String txt = ((Text) e.widget).getText();

            if (bckend.testOnLocalDrive(txt))
            {
               bckend.handleDirTreeViever(dirTreeViewer, previewTreeViewer, txt);

               selAll.setSelection(true);
               
               setPageComplete(true);
            }
         }
      });

      workspaceTxt.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            // TODO
            // Text txt = (Text) e.widget;
            // boolean result = testOnLocalDrive(txt.getText());
            // if (result == true)
            // {
            // dirTreeViewer.setInput(new TreeViewerInput(new File(txt.getText())));
            // dirTreeViewer.expandToLevel(2);
            // setChecked();
            // setPageComplete(dirTreeViewer.getCheckedElements().length > 0);
            // easyButton.setEnabled(dirTreeViewer.getCheckedElements().length > 0);
            // }

         }
      });

      refresh.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            if (!dirTxt.getText().equals(""))
            {
               String text = dirTxt.getText();
               if (!new File(text).getParentFile().equals(null))
               {
                  dirTxt.setText(text);                  
                  // easyButton.setEnabled(dirTreeViewer.getCheckedElements().length > 0);
                  // setPageComplete(dirTreeViewer.getCheckedElements().length > 0);
               }
            }
         }
      });

      selAll.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            if (selAll.getSelection())
            {
               // Alle markieren
               bckend.doCheck(dirTreeViewer, true);
            }
            else
            {
               // Alle unmarkieren
               bckend.doCheck(dirTreeViewer, false);
            }
         }
      });
      
      
      
      //dirTreeViewer.add

      // if a category is checked in the tree, check all its children
      dirTreeViewer.addCheckStateListener(new ICheckStateListener()
      {
         public void checkStateChanged(CheckStateChangedEvent event)
         {         
            System.out.println("möp");
            // System.out.println(event.getElement().getClass()); --> erkenntnis: das ist ein Node
            Node elementNode = (Node) event.getElement();
            
            if (event.getChecked())
            {   
               dirTreeViewer.setSubtreeChecked(elementNode, true);
               
               if(elementNode.getType() == Node.Type.PROJECT)                  
                  bckend.addProjectToPrevievTree(previewTreeViewer, elementNode);
               else
                  for (Node iter : elementNode.getProjectChildren()) //will noch nich arbeiten wenn das "Modul" bereits weg ist
                     bckend.addProjectToPrevievTree(previewTreeViewer, iter);
               
            }
            else
            {
               dirTreeViewer.setSubtreeChecked(elementNode, false);
               //TODO eyeCandy: wenn alle unterelemente unmarkiert das Oberelement unmarkieren
               
               if(elementNode.getType() == Node.Type.PROJECT)
                  bckend.deleteProjectFromPrevievTree(previewTreeViewer, elementNode);    
               else
                  for (Node iter : elementNode.getProjectChildren())
                     bckend.deleteProjectFromPrevievTree(previewTreeViewer, iter);
               
               selAll.setSelection(false);
            }
            //setPageComplete(dirTreeViewer.getCheckedElements().length > 0);            
         }
      });

      dirTreeViewer.setComparator(new ViewerComparator()
      {
         @Override
         public int compare(Viewer viewer, Object e1, Object e2)
         {

            /*
             * if (e1 instanceof ParentCategory && e2 instanceof ParentCategory) { return ((ParentCategory)
             * e1).getName().compareToIgnoreCase(((ParentCategory) e2).getName()); }
             */
            return 0;
         }
      });

   }
   
   public Node getPreviewRootNode()
   {
      return (Node) previewTreeViewer.getInput();
   }

}
