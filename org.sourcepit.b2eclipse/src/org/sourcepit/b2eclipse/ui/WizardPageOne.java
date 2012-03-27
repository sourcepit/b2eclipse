
package org.sourcepit.b2eclipse.ui;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
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
   private static String directoryName;
   private Label dirLbl, workspaceLbl, workingSetLbl;
   private GridData gridData, gridData2;
   private Button dirBtn, workspaceBtn, rBtn1, rBtn2, rBtn3, workingSetBtn, createProjectsBtn, selectAllBtn,
      deselectAllBtn;
   TreeContentProvider moduleTreeContentProvider = new TreeContentProvider();

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

      

      rBtn1 = new Button(modulePageWidgetContainer, SWT.RADIO);
      rBtn1.setSelection(true);

      dirLbl = new Label(modulePageWidgetContainer, SWT.NONE);
      dirLbl.setText("Select module directory:");

      gridData = new GridData();
      gridData.horizontalAlignment = SWT.FILL;

      gridData2 = new GridData();
      gridData2.horizontalAlignment = SWT.FILL;
      gridData2.verticalAlignment = SWT.TOP;


      dirTxt = new Text(modulePageWidgetContainer, SWT.BORDER);
      dirTxt.setLayoutData(gridData);

      dirBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      dirBtn.setText("Browse...");
      dirBtn.setLayoutData(gridData2);

      rBtn2 = new Button(modulePageWidgetContainer, SWT.RADIO);

      workspaceLbl = new Label(modulePageWidgetContainer, SWT.NONE);
      workspaceLbl.setText("Select project:");

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
      dirTreeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 3));


      createProjectsBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      createProjectsBtn.setText("Create...");
      createProjectsBtn.setLayoutData(gridData2);

      selectAllBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      selectAllBtn.setText("Select All");
      selectAllBtn.setLayoutData(gridData2);

      deselectAllBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      deselectAllBtn.setText("Deselect All");
      deselectAllBtn.setLayoutData(gridData2);


      rBtn3 = new Button(modulePageWidgetContainer, SWT.CHECK);

      workingSetLbl = new Label(modulePageWidgetContainer, SWT.NONE);
      workingSetLbl.setText("Add project/s to working sets:");

      workingSetBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      workingSetBtn.setText("Select...");
      workingSetBtn.setEnabled(false);


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


      rBtn3.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (rBtn3.getSelection())
            {

               workingSetBtn.setEnabled(true);
            }
            else
            {

               workingSetBtn.setEnabled(false);
            }
         }
      });


      workingSetBtn.addListener(SWT.Selection, new Listener()
      {

         @Override
         public void handleEvent(Event event)
         {

            IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
            IWorkingSetSelectionDialog workingSetSelectionDialog = workingSetManager.createWorkingSetSelectionDialog(
               dirShell, true);
            workingSetSelectionDialog.open();


         }


      });

      createProjectsBtn.addListener(SWT.Selection, new Listener()
      {

         @Override
         public void handleEvent(Event event)
         {

            Runnable runnable = new Runnable()
            {
               public void run()
               {
                  try
                  {
                     List<File> projects = getSelectedProjects();

                     for (int i = 0; i < projects.size(); i++)
                     {

                        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
                        IPath projectDotProjectFile = new Path(String.valueOf(projects.get(i)));
                        IProjectDescription projectDescription = workspace
                           .loadProjectDescription(projectDotProjectFile);
                        IProject project = workspace.getRoot().getProject(projectDescription.getName());
                        JavaCapabilityConfigurationPage.createProject(project, projectDescription.getLocationURI(),
                           null);


                     }
                  }
                  catch (CoreException e)
                  {
                     e.printStackTrace();
                  }
               }
            };


            final IWorkbench workbench = PlatformUI.getWorkbench();
            workbench.getDisplay().syncExec(runnable);


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
      modulePageWidgetContainer.setLayout(new GridLayout(4, false));
      

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