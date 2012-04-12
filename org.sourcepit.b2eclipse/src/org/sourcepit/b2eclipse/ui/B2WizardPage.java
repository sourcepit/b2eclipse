/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.sourcepit.b2eclipse.input.TreeViewerInput;
import org.sourcepit.b2eclipse.provider.ContentProvider;
import org.sourcepit.b2eclipse.provider.LabelProvider;

/**
 * @author Marco Grupe
 */
public class B2WizardPage extends WizardPage
{

   private Text dirTxt, workspaceTxt;
   private Button dirBtn, workspaceBtn, rBtn1, rBtn2, checkBtn, workingSetBtn, selectAllBtn, deselectAllBtn;
   private Shell dirShell;
   private Composite modulePageWidgetContainer;
   private CheckboxTreeViewer dirTreeViewer;
   private GridData gridData, gridData2, gridData3;
   private Combo workingSetCombo;
   private IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
   private IWorkingSet[] workingSet;
   private IWorkingSetSelectionDialog workingSetSelectionDialog;
   private IWorkingSet workingSetComboItem;
   private String directoryName, comboBoxItems = "";
   private String[] splitItems;
   private Object[] getCheckedElements;
   private List<File> getSelectedProjects;
   private DirectoryDialog directoryDialog;
   private ElementTreeSelectionDialog elementTreeSelectionDialog;
   private static final B2WizardPage B2WIZARDPAGE_INSTANCE = new B2WizardPage("Module");
   private boolean checkButtonSelection = false;
   private static final String DIALOG_SETTINGS_KEY = "workingSets";


   public B2WizardPage(String name)
   {
      super(name);
      setTitle("Import Modules");
      setDescription("Please specify a project or directory to import. ");
   }


   public static B2WizardPage getInstance()
   {
      return B2WIZARDPAGE_INSTANCE;
   }


   /**
    * 
    * @return the selected projects in TreeViewer
    */
   public List<File> getSelectedProjects()
   {
      getCheckedElements = dirTreeViewer.getCheckedElements();
      getSelectedProjects = new ArrayList<File>();

      for (int i = 0; i < getCheckedElements.length; i++)
      {
         if (TreeViewerInput.getCategories().contains(getCheckedElements[i]))
         {
            continue;
         }
         getSelectedProjects.add(new File(getCheckedElements[i].toString()));
      }

      return getSelectedProjects;
   }


   /**
    * add Widgets on Wizard Page
    */
   private void addWidgets()
   {
      gridData = new GridData();
      gridData.horizontalAlignment = SWT.FILL;

      gridData2 = new GridData();
      gridData2.horizontalAlignment = SWT.FILL;
      gridData2.widthHint = 90;
      gridData2.verticalAlignment = SWT.TOP;

      gridData3 = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
      gridData3.widthHint = 500;
      gridData3.heightHint = 300;

      rBtn1 = new Button(modulePageWidgetContainer, SWT.RADIO);
      rBtn1.setText("Select root directory:");
      rBtn1.setSelection(true);

      dirTxt = new Text(modulePageWidgetContainer, SWT.BORDER);
      dirTxt.setLayoutData(gridData);

      dirBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      dirBtn.setText("Browse...");
      dirBtn.setLayoutData(gridData2);

      rBtn2 = new Button(modulePageWidgetContainer, SWT.RADIO);
      rBtn2.setText("Select workspace project:");

      workspaceTxt = new Text(modulePageWidgetContainer, SWT.BORDER);
      workspaceTxt.setLayoutData(gridData);
      workspaceTxt.setEnabled(false);

      workspaceBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      workspaceBtn.setText("Browse...");
      workspaceBtn.setEnabled(false);
      workspaceBtn.setLayoutData(gridData2);

      dirTreeViewer = new CheckboxTreeViewer(modulePageWidgetContainer);
      dirTreeViewer.setContentProvider(new ContentProvider());
      dirTreeViewer.setLabelProvider(new LabelProvider());
      dirTreeViewer.getTree().setLayoutData(gridData3);


      selectAllBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      selectAllBtn.setText("Select All");
      selectAllBtn.setLayoutData(gridData2);

      deselectAllBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      deselectAllBtn.setText("Deselect All");
      deselectAllBtn.setLayoutData(gridData2);

      checkBtn = new Button(modulePageWidgetContainer, SWT.CHECK);
      checkBtn.setText("Select Working Set:");
      checkBtn.setLayoutData(gridData);

      workingSetCombo = new Combo(modulePageWidgetContainer, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.HORIZONTAL
         | SWT.LEFT_TO_RIGHT);
      workingSetCombo.setEnabled(false);
      workingSetCombo.setLayoutData(gridData);

      workingSetBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      workingSetBtn.setText("Select...");
      workingSetBtn.setEnabled(false);
      workingSetBtn.setLayoutData(gridData);
   }


   /**
    * add Listener to the specific widgets
    */
   private void addListener()
   {
      dirBtn.addListener(SWT.Selection, new Listener()
      {
         @Override
         public void handleEvent(Event event)
         {
            TreeViewerInput.clearArrayList();

            directoryDialog = new DirectoryDialog(dirShell, SWT.OPEN);
            directoryDialog.setText("Directory Selection...");
            directoryName = directoryDialog.open();
            if (directoryName == null)
               return;
            dirTxt.setText(directoryName);
            workspaceTxt.setText("");

            dirTreeViewer.setInput(new TreeViewerInput(new File(directoryName)));

            dirTreeViewer.expandAll();

         }
      });

      workspaceBtn.addListener(SWT.Selection, new Listener()
      {
         @Override
         public void handleEvent(Event event)
         {
            TreeViewerInput.clearArrayList();
            elementTreeSelectionDialog = new ElementTreeSelectionDialog(dirShell, new WorkbenchLabelProvider(),
               new BaseWorkbenchContentProvider());
            elementTreeSelectionDialog.setTitle("Project Selection");
            elementTreeSelectionDialog.setMessage("Select a project:");
            elementTreeSelectionDialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
            elementTreeSelectionDialog.open();
            if (elementTreeSelectionDialog.getFirstResult() != null)
            {
               directoryName = String.valueOf(((IResource) elementTreeSelectionDialog.getFirstResult()).getLocation());
               workspaceTxt.setText(directoryName);
               dirTxt.setText("");
               dirTreeViewer.setInput(new TreeViewerInput(new File(directoryName)));
            }
         }
      });

      rBtn1.addListener(SWT.Selection, new Listener()
      {

         @Override
         public void handleEvent(Event event)
         {

            if (rBtn1.isEnabled())
            {
               dirTxt.setEnabled(true);
               dirBtn.setEnabled(true);
               workspaceTxt.setEnabled(false);
               workspaceBtn.setEnabled(false);
            }

         }


      });

      rBtn2.addListener(SWT.Selection, new Listener()
      {

         @Override
         public void handleEvent(Event event)
         {

            if (rBtn2.isEnabled())
            {
               workspaceTxt.setEnabled(true);
               workspaceBtn.setEnabled(true);
               dirTxt.setEnabled(false);
               dirBtn.setEnabled(false);
            }

         }


      });


      checkBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (checkBtn.getSelection())
            {
               if (getDialogSettings().get(DIALOG_SETTINGS_KEY) != null)
                  workingSetCombo.add(getDialogSettings().get(DIALOG_SETTINGS_KEY));
               workingSetCombo.setText(getDialogSettings().get(DIALOG_SETTINGS_KEY));

               checkButtonSelection = true;
               workingSetBtn.setEnabled(true);
               workingSetCombo.setEnabled(true);

            }
            else
            {
               checkButtonSelection = true;
               workingSetBtn.setEnabled(false);
               workingSetCombo.setEnabled(false);
            }
         }
      });


      workingSetBtn.addListener(SWT.Selection, new Listener()
      {
         @Override
         public void handleEvent(Event event)
         {
            workingSetSelectionDialog = workingSetManager.createWorkingSetSelectionDialog(dirShell, true);
            if (dirTreeViewer.getCheckedElements().length != 0)
            {
               selectWorkingSetSelectionDialog();
            }
            workingSet = workingSetSelectionDialog.getSelection();


            addItemToCombo();
         }
      });

      selectAllBtn.addListener(SWT.Selection, new Listener()
      {

         @Override
         public void handleEvent(Event event)
         {
            setCategoriesChecked();

            for (int i = 0; i < TreeViewerInput.getInstance().getProjectFileList().size(); i++)
            {

               dirTreeViewer.setSubtreeChecked(TreeViewerInput.getInstance().getProjectFileList().get(i), true);

            }

         }


      });

      deselectAllBtn.addListener(SWT.Selection, new Listener()
      {


         @Override
         public void handleEvent(Event event)
         {
            setCategoriesUnchecked();

            for (int i = 0; i < TreeViewerInput.getInstance().getProjectFileList().size(); i++)
            {

               dirTreeViewer.setSubtreeChecked(TreeViewerInput.getInstance().getProjectFileList().get(i), false);

            }

         }


      });

      workingSetCombo.addSelectionListener(new SelectionListener()
      {
         public void widgetSelected(SelectionEvent e)
         {
            if (workingSetCombo.getText().contains(","))
            {
               splitItems = workingSetCombo.getText().split(",");
               workingSet = new IWorkingSet[splitItems.length];
               for (int i = 0; i < splitItems.length; i++)
               {
                  workingSetComboItem = workingSetManager.getWorkingSet(splitItems[i]);
                  workingSet[i] = workingSetComboItem;
               }
            }
            else
            {
               workingSetComboItem = workingSetManager.getWorkingSet(workingSetCombo.getText());
               workingSet = new IWorkingSet[] { workingSetComboItem };
            }
         }

         public void widgetDefaultSelected(SelectionEvent e)
         {
         }
      });

      // if a category is checked in the tree, check all its children
      dirTreeViewer.addCheckStateListener(new ICheckStateListener()
      {

         @Override
         public void checkStateChanged(CheckStateChangedEvent event)
         {
            if (event.getChecked())
            {
               dirTreeViewer.setSubtreeChecked(event.getElement(), true);
            }
            else
            {
               dirTreeViewer.setSubtreeChecked(event.getElement(), false);
            }

         }
      });

   }

   /**
    * Create specific controls for the wizard page.
    */
   public void createControl(Composite parent)
   {
      modulePageWidgetContainer = new Composite(parent, SWT.NONE);
      modulePageWidgetContainer.setLayout(new GridLayout(3, false));

      addWidgets();

      if (B2Wizard.getPath() != null)
      {
         dirTxt.setText(String.valueOf(B2Wizard.getPath()));
         dirTreeViewer.setInput(new TreeViewerInput(new File(String.valueOf(B2Wizard.getPath()))));


      }


      dirShell = parent.getShell();

      addListener();

      setControl(modulePageWidgetContainer);

      setPageComplete(true);


   }


   public IWorkingSetManager getWorkingSetManager()
   {
      return workingSetManager;

   }

   public IWorkingSet[] getWorkingSet()
   {
      return workingSet;
   }

   private void addItemToCombo()
   {
      if (getWorkingSet() != null)
      {
         if (getWorkingSet().length == 1)
         {
            for (int i = 0; i < getWorkingSet().length; i++)
            {


               for (int y = 0; y < workingSetCombo.getItemCount(); y++)
               {
                  if (workingSetCombo.getItem(y).equals(getWorkingSet()[i].getName()))
                  {
                     return;
                  }

               }


               workingSetCombo.add(getWorkingSet()[i].getName());
               if(getDialogSettings() != null)
                  getDialogSettings().put(DIALOG_SETTINGS_KEY,getWorkingSet()[i].getName());


            }
            workingSetCombo.setText(getWorkingSet()[0].getName());
         }
         else
         {
            for (int i = 0; i < getWorkingSet().length; i++)
            {


               for (int y = 0; y < workingSetCombo.getItemCount(); y++)
               {
                  if (workingSetCombo.getItem(y).equals(comboBoxItems))
                  {
                     return;
                  }

               }

               comboBoxItems = comboBoxItems.concat(getWorkingSet()[i].getName().concat(","));


            }
            comboBoxItems = comboBoxItems.substring(0, comboBoxItems.length() - 1);
            workingSetCombo.add(comboBoxItems);
            if(getDialogSettings() != null)
               getDialogSettings().put(DIALOG_SETTINGS_KEY,comboBoxItems);
            workingSetCombo.setText(comboBoxItems);
            comboBoxItems = "";
         }


      }

   }

   private void selectWorkingSetSelectionDialog()
   {
      if (workingSetCombo.getText().trim().isEmpty())
      {
         workingSetSelectionDialog.setSelection(null);
         workingSetSelectionDialog.open();
      }
      else if (workingSetComboItem != null)
      {
         workingSetSelectionDialog.setSelection(getWorkingSet());
         workingSetSelectionDialog.open();
      }
      else if (getWorkingSet() != null)
      {
         workingSetSelectionDialog.setSelection(getWorkingSet());
         workingSetSelectionDialog.open();

      }


   }


   private void setCategoriesChecked()
   {
      for (int i = 0; i < TreeViewerInput.getCategories().size(); i++)
      {
         dirTreeViewer.setChecked(TreeViewerInput.getCategories().get(i), true);
      }


   }

   private void setCategoriesUnchecked()
   {
      for (int i = 0; i < TreeViewerInput.getCategories().size(); i++)
      {
         dirTreeViewer.setChecked(TreeViewerInput.getCategories().get(i), false);
      }
   }

   public boolean getCheckButtonSelection()
   {
      return checkButtonSelection;
   }


}