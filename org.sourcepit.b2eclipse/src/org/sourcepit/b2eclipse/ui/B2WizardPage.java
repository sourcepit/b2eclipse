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
import org.eclipse.jface.viewers.CheckboxTreeViewer;
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
import org.sourcepit.b2eclipse.provider.ContentProvider;
import org.sourcepit.b2eclipse.provider.LabelProvider;
import org.sourcepit.b2eclipse.structure.TreeviewerInput;

/**
 * @author Marco Grupe
 */
public class B2WizardPage extends WizardPage
{
   private Text dirTxt, workspaceTxt;
   private Shell dirShell;
   private Composite modulePageWidgetContainer;
   private CheckboxTreeViewer dirTreeViewer;
   private String directoryName;
   private GridData gridData, gridData2, gridData3;
   private Button dirBtn, workspaceBtn, rBtn1, rBtn2, checkBtn, workingSetBtn, selectAllBtn, deselectAllBtn,
      selectPluginsBtn, selectTestsBtn, selectDocsBtn;
   private ContentProvider moduleTreeContentProvider = new ContentProvider();
   private Combo workingSetCombo;
   private IWorkingSetManager workingSetManager;
   private IWorkingSet[] workingSet;
   private IWorkingSetSelectionDialog workingSetSelectionDialog;
   private IWorkingSet workingSetComboItem;
   private String comboBoxItems = "";
   private Object[] getCheckedElements;
   private List<File> getSelectedProjects;
   private DirectoryDialog dd;
   private ElementTreeSelectionDialog etsd;
   private String[] splitItems;
   private static final B2WizardPage INSTANCE = new B2WizardPage("Module");


   public B2WizardPage(String name)
   {

      super(name);
      setTitle("Module");
      setDescription("Please specify a project or directory to import. ");

   }

   public static B2WizardPage getInstance()
   {
      return INSTANCE;
   }


   /**
    * 
    * @return die ausgewählten Projekte im Treeviewer
    */


   public List<File> getSelectedProjects()
   {
      getCheckedElements = dirTreeViewer.getCheckedElements();
      getSelectedProjects = new ArrayList<File>();
      for (int i = 0; i < getCheckedElements.length; i++)
      {

         getSelectedProjects.add(new File(getCheckedElements[i].toString()));

      }

      return getSelectedProjects;
   }


   /**
    * fügt die Widgets hinzu
    */
   private void addWidgets()
   {


      gridData = new GridData();
      gridData.horizontalAlignment = SWT.FILL;

      gridData2 = new GridData();
      gridData2.horizontalAlignment = SWT.FILL;
      gridData2.widthHint = 90;
      gridData2.verticalAlignment = SWT.TOP;

      gridData3 = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 5);
      gridData3.widthHint = 500;
      gridData3.heightHint = 300;

      rBtn1 = new Button(modulePageWidgetContainer, SWT.RADIO);
      rBtn1.setText("Select module directory:");
      rBtn1.setSelection(true);

      dirTxt = new Text(modulePageWidgetContainer, SWT.BORDER);
      dirTxt.setLayoutData(gridData);

      dirBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      dirBtn.setText("Browse...");
      dirBtn.setLayoutData(gridData2);


      rBtn2 = new Button(modulePageWidgetContainer, SWT.RADIO);
      rBtn2.setText("Select project:");


      workspaceTxt = new Text(modulePageWidgetContainer, SWT.BORDER);
      workspaceTxt.setLayoutData(gridData);
      workspaceTxt.setEnabled(false);

      workspaceBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      workspaceBtn.setText("Browse...");
      workspaceBtn.setEnabled(false);
      workspaceBtn.setLayoutData(gridData2);


      dirTreeViewer = new CheckboxTreeViewer(modulePageWidgetContainer);
      dirTreeViewer.setContentProvider(moduleTreeContentProvider);
      dirTreeViewer.setLabelProvider(new LabelProvider());
      dirTreeViewer.getTree().setLayoutData(gridData3);


      selectAllBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      selectAllBtn.setText("Select All");
      selectAllBtn.setLayoutData(gridData2);

      deselectAllBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      deselectAllBtn.setText("Deselect All");
      deselectAllBtn.setLayoutData(gridData2);

      selectPluginsBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      selectPluginsBtn.setText("Select Plugins");
      selectPluginsBtn.setLayoutData(gridData2);

      selectTestsBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      selectTestsBtn.setText("Select Tests");
      selectTestsBtn.setLayoutData(gridData2);

      selectDocsBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      selectDocsBtn.setText("Select Docs");
      selectDocsBtn.setLayoutData(gridData2);


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
    * fügt Listener den jeweiligen Widgets hinzu
    */
   private void addListener()
   {
      dirBtn.addListener(SWT.Selection, new Listener()
      {
         @Override
         public void handleEvent(Event event)
         {
            TreeviewerInput.clearArrayList();

            dd = new DirectoryDialog(dirShell, SWT.OPEN);
            dd.setText("Directory Selection...");
            directoryName = dd.open();
            if (directoryName == null)
               return;
            dirTxt.setText(directoryName);
            workspaceTxt.setText("");

            dirTreeViewer.setInput(new TreeviewerInput(new File(directoryName)));

         }
      });

      workspaceBtn.addListener(SWT.Selection, new Listener()
      {
         @Override
         public void handleEvent(Event event)
         {
            TreeviewerInput.clearArrayList();
            etsd = new ElementTreeSelectionDialog(dirShell, new WorkbenchLabelProvider(),
               new BaseWorkbenchContentProvider());
            etsd.setTitle("Project Selection");
            etsd.setMessage("Select a project:");
            etsd.setInput(ResourcesPlugin.getWorkspace().getRoot());
            etsd.open();
            if (etsd.getFirstResult() != null)
            {
               directoryName = String.valueOf(((IResource) etsd.getFirstResult()).getLocation());
               workspaceTxt.setText(directoryName);
               dirTxt.setText("");
               dirTreeViewer.setInput(new TreeviewerInput(new File(directoryName)));
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


               workingSetBtn.setEnabled(true);
               workingSetCombo.setEnabled(true);

            }
            else
            {

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
            workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
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


            for (int i = 0; i < moduleTreeContentProvider.getProjects().size(); i++)
            {

               dirTreeViewer.setSubtreeChecked(moduleTreeContentProvider.getProjects().get(i), true);

            }


         }


      });

      deselectAllBtn.addListener(SWT.Selection, new Listener()
      {


         @Override
         public void handleEvent(Event event)
         {

            for (int i = 0; i < moduleTreeContentProvider.getProjects().size(); i++)
            {

               dirTreeViewer.setSubtreeChecked(moduleTreeContentProvider.getProjects().get(i), false);

            }

         }


      });

      selectTestsBtn.addListener(SWT.Selection, new Listener()
      {

         @Override
         public void handleEvent(Event event)
         {

            for (int i = 0; i < moduleTreeContentProvider.getProjects().size(); i++)
            {

               if (moduleTreeContentProvider.getProjects().get(i).getParent().endsWith(".tests"))
               {
                  dirTreeViewer.setSubtreeChecked(moduleTreeContentProvider.getProjects().get(i), true);
               }

            }

         }

      });

      selectDocsBtn.addListener(SWT.Selection, new Listener()
      {

         @Override
         public void handleEvent(Event event)
         {

            for (int i = 0; i < moduleTreeContentProvider.getProjects().size(); i++)
            {

               if (moduleTreeContentProvider.getProjects().get(i).getParent().endsWith(".doc"))
               {
                  dirTreeViewer.setSubtreeChecked(moduleTreeContentProvider.getProjects().get(i), true);
               }

            }

         }

      });

      selectPluginsBtn.addListener(SWT.Selection, new Listener()
      {

         @Override
         public void handleEvent(Event event)
         {

            for (int i = 0; i < moduleTreeContentProvider.getProjects().size(); i++)
            {

               if (moduleTreeContentProvider.getProjects().get(i).getParent().endsWith(".module"))
               {
                  dirTreeViewer.setSubtreeChecked(moduleTreeContentProvider.getProjects().get(i), true);
               }

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


   }

   /**
    * Create specific controls for the wizard page.
    * 
    */
   public void createControl(Composite parent)
   {
      modulePageWidgetContainer = new Composite(parent, SWT.NONE);
      modulePageWidgetContainer.setLayout(new GridLayout(3, false));

      addWidgets();

      if (B2Wizard.getPath() != null)
      {
         dirTxt.setText(String.valueOf(B2Wizard.getPath()));
         dirTreeViewer.setInput(new TreeviewerInput(new File(String.valueOf(B2Wizard.getPath()))));


      }


      dirShell = parent.getShell();

      addListener();

      setControl(modulePageWidgetContainer);

      setPageComplete(true);


   }

   public boolean isCheckButtonSelected()
   {
      return checkBtn.getSelection();
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
            workingSetCombo.setText(comboBoxItems);
            comboBoxItems = "";
         }


      }

   }

   private void selectWorkingSetSelectionDialog()
   {
      if (workingSetComboItem != null)
      {
         workingSetSelectionDialog.setSelection(getWorkingSet());
         workingSetSelectionDialog.open();
      }
      else if (getWorkingSet() != null)
      {
         workingSetSelectionDialog.setSelection(getWorkingSet());
         workingSetSelectionDialog.open();

      }
      else
      {
         workingSetSelectionDialog.open();
      }
   }


}