
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
import org.sourcepit.b2eclipse.provider.TreeContentProvider;
import org.sourcepit.b2eclipse.provider.TreeLabelProvider;


public class WizardPageOne extends WizardPage
{


   private Text dirTxt, workspaceTxt;
   private Shell dirShell;
   private Composite modulePageWidgetContainer;
   private CheckboxTreeViewer dirTreeViewer;
   private String directoryName;
   private GridData gridData, gridData2, gridData3;
   public Button dirBtn, workspaceBtn, rBtn1, rBtn2, checkBtn, workingSetBtn, selectAllBtn, deselectAllBtn;
   private TreeContentProvider moduleTreeContentProvider = new TreeContentProvider();
   public Combo workingSetCombo;
   IWorkingSetManager workingSetManager;
   IWorkingSet[] workingSet, wsTemp;

   public WizardPageOne(String name)
   {
      super(name);
      setTitle("Module");
      setDescription("Please specify a project or directory to import. ");
   }


   /**
    * 
    * @return die ausgewählten Projekte im Treeviewer
    */


   public List<File> getSelectedProjects()
   {
      Object[] getCheckedElements = dirTreeViewer.getCheckedElements();
      List<File> getSelectedProjects = new ArrayList<File>();
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
      gridData2.verticalAlignment = SWT.TOP;

      gridData3 = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
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
      dirTreeViewer.setLabelProvider(new TreeLabelProvider());
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
    * fügt Listener den jeweiligen Widgets hinzu
    */
   private void addListener()
   {
      dirBtn.addListener(SWT.Selection, new Listener()
      {


         @Override
         public void handleEvent(Event event)
         {

            dirTreeViewer.remove(TreeContentProvider.clearArrayList());

            DirectoryDialog dd = new DirectoryDialog(dirShell, SWT.OPEN);
            dd.setText("Directory Selection...");
            directoryName = dd.open();


            if (directoryName == null)
               return;


            dirTxt.setText(directoryName);
            workspaceTxt.setText("");

            dirTreeViewer.setInput(new File(directoryName));

         }


      });

      workspaceBtn.addListener(SWT.Selection, new Listener()
      {
         @Override
         public void handleEvent(Event event)
         {
            dirTreeViewer.remove(TreeContentProvider.clearArrayList());

            ElementTreeSelectionDialog etsd = new ElementTreeSelectionDialog(dirShell, new WorkbenchLabelProvider(),
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
               dirTreeViewer.setInput(new File(directoryName));
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
            IWorkingSetSelectionDialog workingSetSelectionDialog = workingSetManager.createWorkingSetSelectionDialog(
               dirShell, true);
            if (dirTreeViewer.getCheckedElements().length != 0)
            {

               workingSetSelectionDialog.open();

            }


            workingSet = workingSetSelectionDialog.getSelection();

            if (workingSet != null)
            {

               for (int i = 0; i < workingSet.length; i++)
               {
                  workingSetCombo.add(workingSet[i].getName().concat(","), workingSetCombo.getItemCount());
                  workingSetCombo.setText(workingSetCombo.getItem(i));
               }

            }


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
         dirTreeViewer.setInput(new File(String.valueOf(B2Wizard.getPath())));


      }


      dirShell = parent.getShell();

      addListener();


      setControl(modulePageWidgetContainer);
      setPageComplete(true);

   }


}