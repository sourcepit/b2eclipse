/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.ui.dialogs.WorkingSetGroup;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.sourcepit.b2eclipse.Activator;
import org.sourcepit.b2eclipse.input.Category;
import org.sourcepit.b2eclipse.input.ParentCategory;
import org.sourcepit.b2eclipse.input.TreeViewerInput;
import org.sourcepit.b2eclipse.provider.ContentProvider;
import org.sourcepit.b2eclipse.provider.LabelProvider;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */
public class B2WizardPage extends WizardPage
{

   private Text dirTxt, workspaceTxt;
   private Button dirBtn, workspaceBtn, dirRadioBtn, workspaceRadioBtn, selectAllBtn, deselectAllBtn, refreshBtn,
      easyButton;
   private Shell dialogShell;
   private Composite modulePageWidgetContainer;
   private CheckboxTreeViewer dirTreeViewer;

   private IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();

   private String selectedDirectory, selectedProject; //$NON-NLS-1$
   private boolean easyButtonSelection = false;
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
      setTitle(Messages.msgImportHeader);
      setDescription(Messages.msgImportSuperscription);
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
            dirTreeViewer.expandToLevel(2);
            setChecked();
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
            directoryDialog.setText(Messages.msgSelectDirTitle);

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
            elementTreeSelectionDialog.setTitle(Messages.msgSelectProjectTitle);
            elementTreeSelectionDialog.setMessage(Messages.msgSelectProject);
            elementTreeSelectionDialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
            elementTreeSelectionDialog.open();
            if (elementTreeSelectionDialog.getFirstResult() != null)
            {
               selectedProject = String.valueOf(((IResource) elementTreeSelectionDialog.getFirstResult()).getLocation());

               boolean result = testOnLocalDrive(selectedProject);
               if (result == true)
               {
                  workspaceTxt.setText(selectedProject);
                  dirTxt.setText(""); //$NON-NLS-1$
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
               dirTreeViewer.expandToLevel(2);
               setChecked();
               setPageComplete(dirTreeViewer.getCheckedElements().length > 0);
               easyButton.setEnabled(dirTreeViewer.getCheckedElements().length > 0);
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
               dirTreeViewer.expandToLevel(2);
               setChecked();
               setPageComplete(dirTreeViewer.getCheckedElements().length > 0);
               easyButton.setEnabled(dirTreeViewer.getCheckedElements().length > 0);
            }

         }
      });

      selectAllBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {

            if (getTreeViewerInput() != null)
            {
               for (int i = 0; i < getTreeViewerInput().getCategories().size(); i++)
               {

                  dirTreeViewer.setSubtreeChecked(getTreeViewerInput().getCategories().get(i), true);

               }
               setPageComplete(dirTreeViewer.getCheckedElements().length > 0);
               easyButton.setEnabled(dirTreeViewer.getCheckedElements().length > 0);
            }

         }

      });

      deselectAllBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {

            if (getTreeViewerInput() != null)
            {
               dirTreeViewer.setCheckedElements(new Object[0]);
               setPageComplete(dirTreeViewer.getCheckedElements().length > 0);
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
               String text = dirTxt.getText();
               boolean result = testOnLocalDrive(text);
               if (result == true)
               {
                  dirTxt.setText(text);
                  dirTreeViewer.setCheckedElements(new Object[0]);
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

            List<File> projectList = getSelectedProjects();
            for (File project : projectList)
            {
               String result = doParentSearch(project);

               if (moduleMap.containsKey(result))
               {

                  ArrayList<String> b = moduleMap.get(result);
                  b.add(project.getAbsolutePath());
                  moduleMap.put(result, b);

               }
               else
               {
                  ArrayList<String> dummyList = new ArrayList<String>();
                  dummyList.add(project.getAbsolutePath());
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

      dirTreeViewer.setComparator(new ViewerComparator()
      {
         @Override
         public int compare(Viewer viewer, Object e1, Object e2)
         {

            if (e1 instanceof ParentCategory && e2 instanceof ParentCategory)
            {
               return ((ParentCategory) e1).getName().compareToIgnoreCase(((ParentCategory) e2).getName());
            }
            return 0;
         }
      });


   }

   private void addDropSupport(final CheckboxTreeViewer treeviewer)
   {
      int style = DND.DROP_DEFAULT | DND.DROP_MOVE;
      DropTarget target = new DropTarget(treeviewer.getTree(), style);
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
   private List<File> getSelectedProjects()
   {
      Object[] getCheckedElements = dirTreeViewer.getCheckedElements();
      ArrayList<File> getSelectedProjects = new ArrayList<File>();

      for (final Object checkedElement : getCheckedElements)
      {
         if (checkedElement instanceof Category)
         {
            continue;
         }
         getSelectedProjects.add(new File(checkedElement.toString()));
      }
      getSelectedProjects.trimToSize();
      return getSelectedProjects;
   }

   private IWorkingSetManager getWorkingSetManager()
   {
      return workingSetManager;

   }

   private boolean getEasyButtonSelection()
   {
      return easyButtonSelection;
   }

   private IPath getPath()
   {
      return projectPath;
   }

   private TreeViewerInput getTreeViewerInput()
   {
      treeViewerInput = (TreeViewerInput) dirTreeViewer.getInput();
      return treeViewerInput;
   }

   private Map<String, ArrayList<String>> getModuleMap()
   {
      return moduleMap;
   }

   private ArrayList<String> getFileList()
   {
      return fileList;
   }

   private File getProject(int position)
   {
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

   private void setChecked()
   {
      if (getTreeViewerInput() != null)
      {
         for (int i = 0; i < getTreeViewerInput().getCategories().size(); i++)
         {

            dirTreeViewer.setSubtreeChecked(getTreeViewerInput().getCategories().get(i), true);

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
      getFileList().clear();

      for (File fileElement : elementList)
      {
         addFiletoFilelist(fileElement.getName());
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
      dirRadioBtn.setText(Messages.msgSelectRootRbtn);
      dirRadioBtn.setSelection(true);

      dirTxt = new Text(rootAndWorkspaceComposite, SWT.BORDER);
      dirTxt.setToolTipText(Messages.msgSelectRootTt);

      GridData directoryPathData = new GridData(SWT.FILL, SWT.FILL, true, true);
      directoryPathData.widthHint = new PixelConverter(dirTxt).convertWidthInCharsToPixels(25);
      dirTxt.setLayoutData(directoryPathData);

      dirBtn = new Button(rootAndWorkspaceComposite, SWT.PUSH);
      dirBtn.setText(Messages.msgBrowseBtn);
      setButtonLayoutData(dirBtn);

      workspaceRadioBtn = new Button(rootAndWorkspaceComposite, SWT.RADIO);
      workspaceRadioBtn.setText(Messages.msgSelectWorkspaceRbtn);

      workspaceTxt = new Text(rootAndWorkspaceComposite, SWT.BORDER);
      workspaceTxt.setToolTipText(Messages.msgSelectWorkspaceTt);
      workspaceTxt.setEnabled(false);

      GridData workspaceData = new GridData(SWT.FILL, SWT.FILL, true, true);
      workspaceData.widthHint = new PixelConverter(workspaceTxt).convertWidthInCharsToPixels(25);
      workspaceTxt.setLayoutData(workspaceData);

      workspaceBtn = new Button(rootAndWorkspaceComposite, SWT.PUSH);
      workspaceBtn.setText(Messages.msgBrowseBtn);
      workspaceBtn.setEnabled(false);
      setButtonLayoutData(workspaceBtn);
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
      selectAllBtn.setText(Messages.msgSelectAllBtn);
      selectAllBtn.setToolTipText(Messages.msgDeselectAllTt);
      Dialog.applyDialogFont(selectAllBtn);
      setButtonLayoutData(selectAllBtn);

      deselectAllBtn = new Button(buttonsComposite, SWT.PUSH);
      deselectAllBtn.setText(Messages.msgDeselectAllBtn);
      deselectAllBtn.setToolTipText(Messages.msgSelectAllTt);
      Dialog.applyDialogFont(deselectAllBtn);
      setButtonLayoutData(deselectAllBtn);

      refreshBtn = new Button(buttonsComposite, SWT.PUSH);
      refreshBtn.setText(Messages.msgRefreshBtn);
      refreshBtn.setToolTipText(Messages.msgRefreshTt);
      Dialog.applyDialogFont(refreshBtn);
      setButtonLayoutData(refreshBtn);


      easyButton = new Button(buttonsComposite, SWT.PUSH);
      easyButton.setToolTipText(Messages.msgEasyTt);
      imgState1 = Activator.getImageFromPath("icons/State1.png");
      imgState2 = Activator.getImageFromPath("icons/State2.png");
      imgState3 = Activator.getImageFromPath("icons/State3.png");
      easyButton.setImage(imgState1);
      easyButton.setEnabled(dirTreeViewer.getCheckedElements().length > 0);
      Dialog.applyDialogFont(easyButton);
      setButtonLayoutData(easyButton);

   }

   private void createWorkingSetGroup(Composite workArea)
   {
      String[] workingSetIds = new String[] { "org.eclipse.ui.resourceWorkingSetPage", //$NON-NLS-1$
         "org.eclipse.jdt.ui.JavaWorkingSetPage" }; //$NON-NLS-1$
      workingSetGroup = new WorkingSetGroup(workArea, currentSelection, workingSetIds);


   }

   public boolean doPerformFinish()
   {

      projectList = getSelectedProjects();
      removeAllCreatedProjectsInList();

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
                     monitor.beginTask(Messages.msgSelectRootRbtn, projectList.size());
                     try
                     {
                        for (int i = 0; i < projectList.size(); i++)
                        {
                           dummy = i;
                           monitor.subTask(Messages.msgBrowseBtn + " " + getProject(i).getParent());
                           Display.getDefault().syncExec(new Runnable()
                           {

                              public void run()
                              {
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

   private void removeAllCreatedProjectsInList()
   {
      createdProjects.removeAll(createdProjects);
   }

   private boolean testOnLocalDrive(String path)
   {
      File filePath = new File(path);
      if (filePath.getParentFile() != null)
         return true;
      else
         return false;
   }

   private void addCreatedProject(IProject project)
   {
      createdProjects.add(project);
   }


}