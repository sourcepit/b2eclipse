/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.dialogs.WorkingSetGroup;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.sourcepit.b2eclipse.Activator;
import org.sourcepit.b2eclipse.input.Category;
import org.sourcepit.b2eclipse.input.TreeViewerInput;
import org.sourcepit.b2eclipse.provider.ContentProvider;
import org.sourcepit.b2eclipse.provider.LabelProvider;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */
public class B2WizardPage extends WizardPage implements IOverwriteQuery
{

   private Text dirTxt, workspaceTxt;
   private Button dirBtn, workspaceBtn, dirRadioBtn, workspaceRadioBtn, copyModecheckBtn, selectAllBtn, deselectAllBtn,
      refreshBtn, easyButton;
   private Shell dialogShell;
   private Composite modulePageWidgetContainer;
   private CheckboxTreeViewer dirTreeViewer;

   private IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();

   private String selectedDirectory, selectedProject; //$NON-NLS-1$
   private boolean copyModecheckButtonSelection = false,
      easyButtonSelection = false;
   private IPath projectPath;
   private TreeViewerInput treeViewerInput;
   private static String previouslyBrowsedDirectory = "";
   private ArrayList<String> fileList = new ArrayList<String>();
   private Map<String, ArrayList<String>> moduleMap = new HashMap<String, ArrayList<String>>();
   private IStructuredSelection currentSelection;
   private WorkingSetGroup workingSetGroup;
   private List<IProject> createdProjects = new ArrayList<IProject>();
   private List<File> projectList;
   private int dummy;
   private Image imgState1, imgState2, imgState3;

   public B2WizardPage(String name, IStructuredSelection currentSelection)
   {

      super(name);
      this.currentSelection = currentSelection;
      setPageComplete(false);
      setTitle(Messages.B2WizardPage_1);
      setDescription(Messages.B2WizardPage_2);
      clearArrayList();

   }

   /**
    * Create specific controls for the wizard page.
    */
   public void createControl(Composite parent)
   {
      initializeDialogUnits(parent);
      modulePageWidgetContainer = new Composite(parent, SWT.NONE);
      setControl(modulePageWidgetContainer);

      modulePageWidgetContainer.setLayout(new GridLayout());
      modulePageWidgetContainer.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
         | GridData.GRAB_VERTICAL));

      addWidgets(modulePageWidgetContainer);

      if (getPath() != null)
      {
         boolean result = testOnLocalDrive(getPath().toOSString());
         if (result == true)
         {
            dirTxt.setText(String.valueOf(getPath()));
            dirTreeViewer.setInput(new TreeViewerInput(new File(String.valueOf(getPath()))));
            dirTreeViewer.expandAll();
         }
      }

      dialogShell = parent.getShell();

      treeViewerInput = new TreeViewerInput();

      addListener();

      setControl(modulePageWidgetContainer);


   }

   /**
    * add Widgets on Wizard Page
    */
   private void addWidgets(Composite workArea)
   {

      createRootAndWorkspaceArea(workArea);
      createProjectsArea(workArea);
      createOptionsArea(workArea);
      createWorkingSetGroup(workArea);
      Dialog.applyDialogFont(workArea);

   }

   /**
    * add Listener to the specific widgets
    */
   private void addListener()
   {
      dirBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            DirectoryDialog directoryDialog = new DirectoryDialog(dialogShell, SWT.OPEN);
            directoryDialog.setText(Messages.B2WizardPage_11);

            String directoryName = dirTxt.getText().trim();
            if (directoryName.length() == 0)
            {
               directoryName = previouslyBrowsedDirectory;
            }

            if (directoryName.length() == 0)
            {
               directoryDialog.setFilterPath(IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getLocation()
                  .toOSString());
            }
            else
            {
               File path = new File(directoryName);
               if (path.exists())
               {
                  directoryDialog.setFilterPath(new Path(directoryName).toOSString());
               }
            }

            selectedDirectory = directoryDialog.open();
            if (selectedDirectory != null)
            {
               boolean result = testOnLocalDrive(selectedDirectory);
               if (result == true)
               {
                  previouslyBrowsedDirectory = selectedDirectory;
                  dirTxt.setText(selectedDirectory);
                  workspaceTxt.setText(""); //$NON-NLS-1$
                  // dirTreeViewer.setInput(new TreeViewerInput(new File(selectedDirectory)));

                  dirTreeViewer.expandAll();
               }
            }

         }
      });

      workspaceBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            ElementTreeSelectionDialog elementTreeSelectionDialog = new ElementTreeSelectionDialog(dialogShell,
               new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
            elementTreeSelectionDialog.setTitle(Messages.B2WizardPage_13);
            elementTreeSelectionDialog.setMessage(Messages.B2WizardPage_14);
            elementTreeSelectionDialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
            elementTreeSelectionDialog.open();
            if (elementTreeSelectionDialog.getFirstResult() != null)
            {
               selectedProject = String.valueOf(((IResource) elementTreeSelectionDialog.getFirstResult()).getLocation());
               workspaceTxt.setText(selectedProject);
               dirTxt.setText(""); //$NON-NLS-1$
               boolean result = testOnLocalDrive(selectedProject);
               if (result == true)
               {
                  // dirTreeViewer.setInput(new TreeViewerInput(new File(selectedProject)));
                  dirTreeViewer.expandAll();
               }
            }
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

            Text txt = (Text) e.widget;
            boolean result = testOnLocalDrive(txt.getText());
            if (result == true)
            {
               dirTreeViewer.setInput(new TreeViewerInput(new File(txt.getText())));
               dirTreeViewer.expandAll();
            }

         }
      });
      workspaceTxt.addModifyListener(new ModifyListener()
      {

         public void modifyText(ModifyEvent e)
         {

            Text txt = (Text) e.widget;
            boolean result = testOnLocalDrive(txt.getText());
            if (result == true)
            {
               dirTreeViewer.setInput(new TreeViewerInput(new File(txt.getText())));
               dirTreeViewer.expandAll();
            }

         }
      });

      copyModecheckBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (copyModecheckBtn.getSelection())
            {

               copyModecheckButtonSelection = true;

            }
            else
            {
               copyModecheckButtonSelection = false;
            }
         }
      });

      selectAllBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {

            setCategoriesChecked();
            if (getTreeViewerInput() != null)
            {
               for (int i = 0; i < getTreeViewerInput().getProjectFileList().size(); i++)
               {

                  dirTreeViewer.setSubtreeChecked(getTreeViewerInput().getProjectFileList().get(i), true);

               }

            }
            setPageComplete(dirTreeViewer.getCheckedElements().length > 0);
            easyButton.setEnabled(dirTreeViewer.getCheckedElements().length > 0);
         }

      });

      deselectAllBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            setCategoriesUnchecked();

            if (getTreeViewerInput() != null)
            {
               dirTreeViewer.setCheckedElements(new Object[0]);
               setPageComplete(false);
               easyButton.setEnabled(dirTreeViewer.getCheckedElements().length > 0);

            }
         }

      });

      refreshBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            if (!dirTxt.getText().equals(""))
            {

               boolean result = testOnLocalDrive(dirTxt.getText());
               if (result == true)
               {
                  dirTreeViewer.refresh(true);
                  dirTreeViewer.setCheckedElements(new Object[0]);
                  dirTreeViewer.expandAll();
                  easyButton.setEnabled(dirTreeViewer.getCheckedElements().length > 0);
                  setPageComplete(dirTreeViewer.getCheckedElements().length > 0);

               }

            }
         }

      });

      easyButton.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            easyButton.setImage(imgState3);

            easyButtonSelection = true;

            List<File> list = getSelectedProjects();
            for (File file : list)
            {
               String result = doParentSearch(file);

               if (moduleMap.containsKey(result))
               {

                  ArrayList<String> b = moduleMap.get(result);
                  b.add(file.getAbsolutePath());
                  moduleMap.put(result, b);

               }
               else
               {
                  ArrayList<String> dummyList = new ArrayList<String>();
                  dummyList.add(file.getAbsolutePath());
                  moduleMap.put(result, dummyList);
               }

            }

            if (((B2Wizard) getWizard()).performFinish() == true)
            {
               ((B2Wizard) getWizard()).getShell().close();
            }

         }

      });
      easyButton.addListener(SWT.MouseEnter, new Listener()
      {

         public void handleEvent(Event event)
         {
            easyButton.setImage(imgState2);

         }

      });

      easyButton.addListener(SWT.MouseExit, new Listener()
      {

         public void handleEvent(Event event)
         {
            easyButton.setImage(imgState1);

         }

      });

      // if a category is checked in the tree, check all its children
      dirTreeViewer.addCheckStateListener(new ICheckStateListener()
      {
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
            setPageComplete(dirTreeViewer.getCheckedElements().length > 0);
            easyButton.setEnabled(dirTreeViewer.getCheckedElements().length > 0);

         }
      });


   }

   private void addDropSupport(final CheckboxTreeViewer treeviewer)
   {
      int ops = DND.DROP_DEFAULT | DND.DROP_MOVE;
      DropTarget target = new DropTarget(treeviewer.getTree(), ops);
      final FileTransfer fileTransfer = FileTransfer.getInstance();
      Transfer[] types = new Transfer[] { fileTransfer };
      target.setTransfer(types);

      target.addDropListener(new DropTargetAdapter()
      {
         @Override
         public void drop(DropTargetEvent event)
         {
            if (fileTransfer.isSupportedType(event.currentDataType))
            {
               String[] files = (String[]) event.data;
               File file = new File(files[0]);
               if (file.isDirectory())
               {
                  dirTxt.setText(file.getAbsolutePath());
                  // dirTreeViewer.setInput(new TreeViewerInput(file));
                  dirTreeViewer.expandAll();
               }
               else
               {
                  MessageDialog.openInformation(new Shell(), "Information",
                     "Unsupported Type! Dropping directories are only allowed!");
               }

            }
         }
      });

   }

   /**
    * 
    * @return the selected projects in TreeViewer
    */
   public List<File> getSelectedProjects()
   {
      Object[] getCheckedElements = dirTreeViewer.getCheckedElements();
      ArrayList<File> getSelectedProjects = new ArrayList<File>();

      for (final Object checkedElement : getCheckedElements)
      {
         if (getTreeViewerInput().getCategories().contains(checkedElement))
         {
            continue;
         }
         getSelectedProjects.add(new File(checkedElement.toString()));
      }
      getSelectedProjects.trimToSize();
      return getSelectedProjects;
   }

   public IWorkingSetManager getWorkingSetManager()
   {
      return workingSetManager;
   
   }

   public boolean getEasyButtonSelection()
   {
      return easyButtonSelection;
   }

   public boolean getCopyModeCheckButtonSelection()
   {
      return copyModecheckButtonSelection;
   }

   private IPath getPath()
   {
      return projectPath;
   }

   public TreeViewerInput getTreeViewerInput()
   {
      treeViewerInput = (TreeViewerInput) dirTreeViewer.getInput();
      return treeViewerInput;
   }

   public Map<String, ArrayList<String>> getModuleMap()
   {
      return moduleMap;
   }

   private ArrayList<String> getFileinFilelist()
   {
      return fileList;
   }

   private File getProject(int position){
      return projectList.get(position);
      
   }

   public void setPath(IPath projectPath)
   {
      if (projectPath == null)
      {
         throw new IllegalArgumentException();
      }
      else
      {
         this.projectPath = projectPath;
      }

   }

   private void setCategoriesChecked()
   {
      if (getTreeViewerInput() != null)
      {
         for (final Category category : getTreeViewerInput().getCategories())
         {
            dirTreeViewer.setChecked(category, true);
         }
      }
   
   }

   private void setCategoriesUnchecked()
   {
      if (getTreeViewerInput() != null)
      {
         for (final Category category : getTreeViewerInput().getCategories())
         {
            dirTreeViewer.setChecked(category, false);
         }
      }
   }

   public void clearArrayList()
   {
      if (treeViewerInput != null)
         treeViewerInput.clearArrayList();
   }

   private String doParentSearch(File file)
   {
      File[] elementList = file.getParentFile().listFiles();
      getFileinFilelist().clear();

      for (File i : elementList)
      {
         addFiletoFilelist(i.getName());
      }

      if (fileList.contains("module.xml"))
      {
         for (File element : elementList)
         {
            if (element.getName().equals("module.xml"))
            {
               return element.getParentFile().getName();
            }
         }
      }
      return doParentSearch(file.getParentFile());

   }

   private void addFiletoFilelist(String file)
   {
      fileList.add(file);
   }

   private void createWorkingSetGroup(Composite workArea)
   {
      String[] workingSetIds = new String[] { "org.eclipse.ui.resourceWorkingSetPage", //$NON-NLS-1$
         "org.eclipse.jdt.ui.JavaWorkingSetPage" }; //$NON-NLS-1$
      workingSetGroup = new WorkingSetGroup(workArea, currentSelection, workingSetIds);


   }

   private void createProjectsArea(Composite workArea)
   {
      Composite treeViewerComposite = new Composite(workArea, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 2;
      layout.marginWidth = 0;
      layout.makeColumnsEqualWidth = false;
      treeViewerComposite.setLayout(layout);

      treeViewerComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
         | GridData.FILL_BOTH));

      dirTreeViewer = new CheckboxTreeViewer(treeViewerComposite, SWT.BORDER);
      GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
      gridData.widthHint = new PixelConverter(dirTreeViewer.getControl()).convertWidthInCharsToPixels(100);
      gridData.heightHint = new PixelConverter(dirTreeViewer.getControl()).convertHeightInCharsToPixels(25);
      dirTreeViewer.getControl().setLayoutData(gridData);

      dirTreeViewer.setContentProvider(new ContentProvider());
      dirTreeViewer.setLabelProvider(new LabelProvider());
      addDropSupport(dirTreeViewer);
      createSelectionButtonsArea(treeViewerComposite);
   }

   private void createSelectionButtonsArea(Composite workArea)
   {
      Composite buttonsComposite = new Composite(workArea, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      buttonsComposite.setLayout(layout);

      buttonsComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

      selectAllBtn = new Button(buttonsComposite, SWT.PUSH);
      selectAllBtn.setText(Messages.B2WizardPage_7);
      Dialog.applyDialogFont(selectAllBtn);
      setButtonLayoutData(selectAllBtn);

      deselectAllBtn = new Button(buttonsComposite, SWT.PUSH);
      deselectAllBtn.setText(Messages.B2WizardPage_8);
      Dialog.applyDialogFont(deselectAllBtn);
      setButtonLayoutData(deselectAllBtn);

      refreshBtn = new Button(buttonsComposite, SWT.PUSH);
      refreshBtn.setText(Messages.B2WizardPage_16);
      Dialog.applyDialogFont(refreshBtn);
      setButtonLayoutData(refreshBtn);


      easyButton = new Button(buttonsComposite, SWT.PUSH);
      easyButton.setToolTipText(Messages.B2WizardPage_19);
      imgState1 = Activator.getImageFromPath("icons/State1.png");
      imgState2 = Activator.getImageFromPath("icons/State2.png");
      imgState3 = Activator.getImageFromPath("icons/State3.png");
      easyButton.setImage(imgState1);
      easyButton.setEnabled(dirTreeViewer.getCheckedElements().length > 0);
      Dialog.applyDialogFont(easyButton);
      setButtonLayoutData(easyButton);

   }

   private void createRootAndWorkspaceArea(Composite workArea)
   {
      Composite rootAndWorkspaceComposite = new Composite(workArea, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 3;
      layout.makeColumnsEqualWidth = false;
      layout.marginWidth = 0;
      rootAndWorkspaceComposite.setLayout(layout);
      rootAndWorkspaceComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      dirRadioBtn = new Button(rootAndWorkspaceComposite, SWT.RADIO);
      dirRadioBtn.setText(Messages.B2WizardPage_3);
      dirRadioBtn.setSelection(true);

      dirTxt = new Text(rootAndWorkspaceComposite, SWT.BORDER);
      dirTxt.setToolTipText(Messages.B2WizardPage_17);

      GridData directoryPathData = new GridData(SWT.FILL, SWT.FILL, true, true);
      directoryPathData.widthHint = new PixelConverter(dirTxt).convertWidthInCharsToPixels(25);
      dirTxt.setLayoutData(directoryPathData);

      dirBtn = new Button(rootAndWorkspaceComposite, SWT.PUSH);
      dirBtn.setText(Messages.B2WizardPage_4);
      setButtonLayoutData(dirBtn);

      workspaceRadioBtn = new Button(rootAndWorkspaceComposite, SWT.RADIO);
      workspaceRadioBtn.setText(Messages.B2WizardPage_5);

      workspaceTxt = new Text(rootAndWorkspaceComposite, SWT.BORDER);
      workspaceTxt.setToolTipText(Messages.B2WizardPage_18);
      workspaceTxt.setEnabled(false);

      GridData workspaceData = new GridData(SWT.FILL, SWT.FILL, true, true);
      workspaceData.widthHint = new PixelConverter(workspaceTxt).convertWidthInCharsToPixels(25);
      workspaceTxt.setLayoutData(workspaceData);

      workspaceBtn = new Button(rootAndWorkspaceComposite, SWT.PUSH);
      workspaceBtn.setText(Messages.B2WizardPage_6);
      workspaceBtn.setEnabled(false);
      setButtonLayoutData(workspaceBtn);
   }

   private void createOptionsArea(Composite workArea)
   {

      Composite copyModeComposite = new Composite(workArea, SWT.NONE);
      copyModeComposite.setLayout(new GridLayout());
      copyModeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      copyModecheckBtn = new Button(copyModeComposite, SWT.CHECK);
      copyModecheckBtn.setText(Messages.B2WizardPage_15);
      copyModecheckBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

   }

   public boolean doPerformFinish()
   {

      projectList = getSelectedProjects();
      removeAllCreatedProjectsList();

      final IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress()
      {
         public void run(IProgressMonitor monitor) throws InvocationTargetException
         {
            try
            {
               ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable()
               {
                  public void run(IProgressMonitor monitor) throws CoreException
                  {
                     monitor.beginTask(Messages.B2Wizard_3, projectList.size());
                     try
                     {
                        for (int i = 0; i < projectList.size(); i++)
                        {
                           dummy = i;
                           monitor.subTask(Messages.B2Wizard_4 + " " + getProject(i).getParent());
                           Display.getDefault().syncExec(new Runnable()
                           {

                              public void run()
                              {
                                 if (getCopyModeCheckButtonSelection())
                                    copyProjects(dummy);
                                 else
                                    linkProjects(dummy);
                              }
                           });

                           monitor.worked(1);

                        }
                     }
                     finally
                     {
                        monitor.done();
                     }
                  }
               }, monitor);
            }
            catch (OperationCanceledException e)
            {
               // ignore
            }
            catch (CoreException e)
            {
               throw new InvocationTargetException(e);
            }
         }
      };

      runWithProgress(runnableWithProgress);

      return true;
   }

   private void runWithProgress(final IRunnableWithProgress runnable)
   {
      try
      {
         getContainer().run(true, true, runnable);
      }
      catch (InvocationTargetException e)
      {
         throw new IllegalStateException(e);
      }
      catch (InterruptedException e)
      {
         throw new IllegalStateException(e);
      }

      if (!getEasyButtonSelection())
         addToWorkingSets();


   }

   private void linkProjects(int projectsListPosition)
   {

      try
      {
         final IWorkspace workspace = ResourcesPlugin.getWorkspace();
         final IPath projectFile = new Path(String.valueOf(getProject(projectsListPosition)));
         IProjectDescription projectDescription = workspace.loadProjectDescription(projectFile);
         IProject project = workspace.getRoot().getProject(projectDescription.getName());
         JavaCapabilityConfigurationPage.createProject(project, projectDescription.getLocationURI(), null);

         if (getEasyButtonSelection())
         {
            easyAddToWorkingSets(projectsListPosition, project);

         }
         addCreatedProject(project);
      }
      catch (CoreException e)
      {
         throw new IllegalStateException(e);
      }

   }

   private void copyProjects(int projectsListPosition)
   {
      final IWorkspace workspace = ResourcesPlugin.getWorkspace();
      final IPath projectFile = new Path(String.valueOf(getProject(projectsListPosition)));
      IProject project = null;

      try
      {
         IProjectDescription projectDescription = workspace.loadProjectDescription(projectFile);
         URI locationURI = projectDescription.getLocationURI();
         projectDescription.setLocation(null);
         project = workspace.getRoot().getProject(projectDescription.getName());
         project.create(projectDescription, new NullProgressMonitor());
         project.open(null);
         File importSource = new File(locationURI);
         List<?> filesToImport = FileSystemStructureProvider.INSTANCE.getChildren(importSource);
         ImportOperation operation = new ImportOperation(project.getFullPath(), importSource,
            FileSystemStructureProvider.INSTANCE, this, filesToImport);
         operation.setContext(getShell());
         operation.setOverwriteResources(true);
         operation.setCreateContainerStructure(false);
         operation.run(null);
         if (getEasyButtonSelection())
         {
            easyAddToWorkingSets(projectsListPosition, project);

         }


      }
      catch (ResourceException e)
      {
         MessageDialog
            .openInformation(new Shell(), "Information", "Resource " + project.getName() + " already exists.");
      }
      catch (CoreException e)
      {
         throw new IllegalStateException(e);
      }
      catch (InvocationTargetException e)
      {
         throw new IllegalStateException(e);
      }
      catch (InterruptedException e)
      {
         return;
      }
      addCreatedProject(project);


   }


   private void addToWorkingSets()
   {
      IWorkingSet[] selectedWorkingSets = workingSetGroup.getSelectedWorkingSets();
      if (selectedWorkingSets == null || selectedWorkingSets.length == 0)
         return;
      IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
      for (Iterator<IProject> i = createdProjects.iterator(); i.hasNext();)
      {
         IProject project = (IProject) i.next();
         workingSetManager.addToWorkingSets(project, selectedWorkingSets);
      }
   }

   private void easyAddToWorkingSets(int filePosition, IProject project)
   {
   
      Iterator<String> it = getModuleMap().keySet().iterator();
      while (it.hasNext())
      {
         final String workingsetName = it.next();
         final List<String> projectPaths = getModuleMap().get(workingsetName);
         for (String projectPath : projectPaths)
         {
            if (projectPath.equals(getProject(filePosition).getAbsolutePath()))
            {
               final IWorkingSetManager manager = getWorkingSetManager();
   
               // org.eclipse.ui.resourceWorkingSetPage = Resource WorkingSet
               // org.eclipse.jdt.ui.JavaWorkingSetPage = Java WorkingSet
               IWorkingSet workingSet = manager.getWorkingSet(workingsetName);
               if (workingSet == null)
               {
                  workingSet = manager.createWorkingSet(workingsetName, new IAdaptable[] { project });
                  workingSet.setId("org.eclipse.jdt.ui.JavaWorkingSetPage");
                  manager.addWorkingSet(workingSet);
   
               }
               else
               {
                  manager.addToWorkingSets(project, new IWorkingSet[] { workingSet });
               }
   
            }
         }
      }
   
   
   }

   private void removeAllCreatedProjectsList(){
      createdProjects.removeAll(createdProjects);
   }

   public String queryOverwrite(String pathString)
   {
      Path path = new Path(pathString);

      String messageString;

      if (path.getFileExtension() == null || path.segmentCount() < 2)
      {
         messageString = NLS.bind(IDEWorkbenchMessages.WizardDataTransfer_existsQuestion, pathString);
      }
      else
      {
         messageString = NLS.bind(IDEWorkbenchMessages.WizardDataTransfer_overwriteNameAndPathQuestion,
            path.lastSegment(), path.removeLastSegments(1).toOSString());
      }

      final MessageDialog dialog = new MessageDialog(getContainer().getShell(), IDEWorkbenchMessages.Question, null,
         messageString, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL,
            IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.NO_TO_ALL_LABEL,
            IDialogConstants.CANCEL_LABEL }, 0)
      {
         protected int getShellStyle()
         {
            return super.getShellStyle() | SWT.SHEET;
         }
      };
      String[] response = new String[] { YES, ALL, NO, NO_ALL, CANCEL };
      getControl().getDisplay().syncExec(new Runnable()
      {
         public void run()
         {
            dialog.open();
         }
      });
      return dialog.getReturnCode() < 0 ? CANCEL : response[dialog.getReturnCode()];
   }

   private boolean testOnLocalDrive(String path)
   {
      File pathFile = new File(path);
      if (pathFile.getParentFile() != null)
         return true;
      else
         return false;
   }
   
   private void addCreatedProject(IProject project){
      createdProjects.add(project);
   }


}