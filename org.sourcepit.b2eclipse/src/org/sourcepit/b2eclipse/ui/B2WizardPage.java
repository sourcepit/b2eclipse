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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;
import org.eclipse.ui.dialogs.WorkingSetGroup;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.sourcepit.b2eclipse.Activator;
import org.sourcepit.b2eclipse.input.Category;
import org.sourcepit.b2eclipse.input.TreeViewerInput;
import org.sourcepit.b2eclipse.provider.ContentProvider;
import org.sourcepit.b2eclipse.provider.LabelProvider;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */
public class B2WizardPage extends WizardPage {

	private Text dirTxt, workspaceTxt;
	private Button dirBtn, workspaceBtn, dirRadioBtn, workspaceRadioBtn,
			copyModecheckBtn, selectAllBtn, deselectAllBtn, easyButton;
	private Shell dialogShell;
	private Composite modulePageWidgetContainer;
	private CheckboxTreeViewer dirTreeViewer;

	private IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
			.getWorkingSetManager();
	private IWorkingSet[] workingSets;

	private String selectedDirectory; //$NON-NLS-1$
	private boolean workingSetcheckButtonSelection = false,
			copyModecheckButtonSelection = false, easyButtonSelection = false;
	private IPath projectPath;
	private File workingSetXMLFile;
	private TreeViewerInput treeViewerInput;
	private static String previouslyBrowsedDirectory = "";
	private ArrayList<String> fileList = new ArrayList<String>();
	private Map<String, ArrayList<String>> moduleMap = new HashMap<String, ArrayList<String>>();
	private IStructuredSelection currentSelection;
	private WorkingSetGroup workingSetGroup;
	private List<IProject> createdProjects = new ArrayList<IProject>();
	private List<File> projectList;
	private final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	private IProjectDescription projectDescription = null;
	private IProject project = null;

	public B2WizardPage(String name, IStructuredSelection currentSelection) {

		super(name);
		this.currentSelection = currentSelection;
		setPageComplete(false);
		setTitle(Messages.B2WizardPage_1);
		setDescription(Messages.B2WizardPage_2);

	}

	/**
	 * Create specific controls for the wizard page.
	 */
	public void createControl(Composite parent) {
		modulePageWidgetContainer = new Composite(parent, SWT.NONE);
		setControl(modulePageWidgetContainer);

		modulePageWidgetContainer.setLayout(new GridLayout());
		modulePageWidgetContainer.setLayoutData(new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

		addWidgets(modulePageWidgetContainer);

		if (getPath() != null) {
			dirTxt.setText(String.valueOf(getPath()));
			dirTreeViewer.setInput(new TreeViewerInput(new File(String
					.valueOf(getPath()))));
		}

		dialogShell = parent.getShell();

		treeViewerInput = new TreeViewerInput();

		addListener();

		setControl(modulePageWidgetContainer);


	}

	/**
	 * add Widgets on Wizard Page
	 */
	public void addWidgets(Composite workArea) {

		createRootAndWorkspaceArea(workArea);
		createProjectsArea(workArea);
		createOptionsArea(workArea);
		createWorkingSetGroup(workArea);
		Dialog.applyDialogFont(workArea);

	}

	/**
	 * add Listener to the specific widgets
	 */
	private void addListener() {
		dirBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (getTreeViewerInput() != null)
					clearArrayList();

				DirectoryDialog directoryDialog = new DirectoryDialog(
						dialogShell, SWT.OPEN);
				directoryDialog.setText(Messages.B2WizardPage_11);

				String directoryName = dirTxt.getText().trim();
				if (directoryName.length() == 0) {
					directoryName = previouslyBrowsedDirectory;
				}

				if (directoryName.length() == 0) {
					directoryDialog.setFilterPath(IDEWorkbenchPlugin
							.getPluginWorkspace().getRoot().getLocation()
							.toOSString());
				} else {
					File path = new File(directoryName);
					if (path.exists()) {
						directoryDialog.setFilterPath(new Path(directoryName)
								.toOSString());
					}
				}

				selectedDirectory = directoryDialog.open();
				if (selectedDirectory != null) {
					previouslyBrowsedDirectory = selectedDirectory;
					dirTxt.setText(selectedDirectory);
					workspaceTxt.setText(""); //$NON-NLS-1$
					dirTreeViewer.setInput(new TreeViewerInput(new File(
							selectedDirectory)));

					dirTreeViewer.expandAll();
				}

			}
		});

		workspaceBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (getTreeViewerInput() != null)
					clearArrayList();

				ElementTreeSelectionDialog elementTreeSelectionDialog = new ElementTreeSelectionDialog(
						dialogShell, new WorkbenchLabelProvider(),
						new BaseWorkbenchContentProvider());
				elementTreeSelectionDialog.setTitle(Messages.B2WizardPage_13);
				elementTreeSelectionDialog.setMessage(Messages.B2WizardPage_14);
				elementTreeSelectionDialog.setInput(ResourcesPlugin
						.getWorkspace().getRoot());
				elementTreeSelectionDialog.open();
				if (elementTreeSelectionDialog.getFirstResult() != null) {
					selectedDirectory = String
							.valueOf(((IResource) elementTreeSelectionDialog
									.getFirstResult()).getLocation());
					workspaceTxt.setText(selectedDirectory);
					dirTxt.setText(""); //$NON-NLS-1$
					dirTreeViewer.setInput(new TreeViewerInput(new File(
							selectedDirectory)));
				}
			}
		});

		dirRadioBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (dirRadioBtn.isEnabled()) {
					dirTxt.setEnabled(true);
					dirBtn.setEnabled(true);
					workspaceTxt.setEnabled(false);
					workspaceBtn.setEnabled(false);
				}

			}

		});

		workspaceRadioBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {

				if (workspaceRadioBtn.isEnabled()) {
					workspaceTxt.setEnabled(true);
					workspaceBtn.setEnabled(true);
					dirTxt.setEnabled(false);
					dirBtn.setEnabled(false);
				}

			}

		});

		copyModecheckBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (copyModecheckBtn.getSelection()) {

					copyModecheckButtonSelection = true;

				} else {
					copyModecheckButtonSelection = false;
				}
			}
		});

		selectAllBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {

				setCategoriesChecked();
				if (getTreeViewerInput() != null) {
					for (int i = 0; i < getTreeViewerInput()
							.getProjectFileList().size(); i++) {

						dirTreeViewer.setSubtreeChecked(getTreeViewerInput()
								.getProjectFileList().get(i), true);

					}

				}
				setPageComplete(dirTreeViewer.getCheckedElements().length > 0);
				easyButton.setEnabled(dirTreeViewer.getCheckedElements().length > 0);
			}

		});

		deselectAllBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				setCategoriesUnchecked();

				if (getTreeViewerInput() != null) {
					dirTreeViewer.setCheckedElements(new Object[0]);
					setPageComplete(false);
					easyButton.setEnabled(false);

				}
			}

		});

		easyButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				easyButton.setImage(Activator.getImageFromPath("icons/State3.png"));

				easyButtonSelection = true;

				List<File> list = getSelectedProjects();
				for (File file : list) {
					String result = doParentSearch(file);

					if (moduleMap.containsKey(result)) {

						ArrayList<String> b = moduleMap.get(result);
						b.add(file.getAbsolutePath());
						moduleMap.put(result, b);

					} else {
						ArrayList<String> dummyList = new ArrayList<String>();
						dummyList.add(file.getAbsolutePath());
						moduleMap.put(result, dummyList);
					}

				}

				Iterator<String> it = moduleMap.keySet().iterator();
				while (it.hasNext()) {
					String aKey = it.next();
					ArrayList<String> b = moduleMap.get(aKey);
					System.out.println("Key: " + aKey + " Value: " + b);
				}

				if (((B2Wizard) getWizard()).performFinish() == true) {

					((B2Wizard) getWizard()).getShell().close();
				}

			}

		});
		easyButton.addListener(SWT.MouseEnter, new Listener() {

			public void handleEvent(Event event) {
				easyButton.setImage(Activator.getImageFromPath("icons/State2.png"));

			}

		});

		easyButton.addListener(SWT.MouseExit, new Listener() {

			public void handleEvent(Event event) {
				easyButton.setImage(Activator.getImageFromPath("icons/State1.png"));

			}

		});

		// if a category is checked in the tree, check all its children
		dirTreeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {

				if (event.getChecked()) {
					dirTreeViewer.setSubtreeChecked(event.getElement(), true);
				} else {
					dirTreeViewer.setSubtreeChecked(event.getElement(), false);
				}
				setPageComplete(dirTreeViewer.getCheckedElements().length > 0);
				easyButton.setEnabled(dirTreeViewer.getCheckedElements().length > 0);

			}
		});

	}

	private void addDropSupport(final CheckboxTreeViewer treeviewer) {
		int ops = DND.DROP_DEFAULT | DND.DROP_MOVE;
		DropTarget target = new DropTarget(treeviewer.getTree(), ops);
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer[] types = new Transfer[] { fileTransfer };
		target.setTransfer(types);

		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				if (fileTransfer.isSupportedType(event.currentDataType)) {
					String[] files = (String[]) event.data;
					File file = new File(files[0]);
					if (file.isDirectory()) {
						dirTxt.setText(file.getAbsolutePath());
						dirTreeViewer.setInput(new TreeViewerInput(file));
					} else {
						MessageDialog
								.openInformation(new Shell(), "Information",
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
	public List<File> getSelectedProjects() {
		Object[] getCheckedElements = dirTreeViewer.getCheckedElements();
		ArrayList<File> getSelectedProjects = new ArrayList<File>();

		for (final Object checkedElement : getCheckedElements) {
			if (getTreeViewerInput().getCategories().contains(checkedElement)) {
				continue;
			}
			getSelectedProjects.add(new File(checkedElement.toString()));
		}
		getSelectedProjects.trimToSize();
		return getSelectedProjects;
	}

	public void setPath(IPath projectPath) {
		if (projectPath == null) {
			throw new IllegalArgumentException();
		} else {
			this.projectPath = projectPath;
		}

	}

	private IPath getPath() {
		return projectPath;
	}

	public void setWorkingSetXML(File workingSetXMLFile) {

		if (workingSetXMLFile == null) {
			throw new IllegalArgumentException();
		} else {
			this.workingSetXMLFile = workingSetXMLFile;
		}

	}

	public File getWorkingSetXML() {
		return workingSetXMLFile;
	}

	public IWorkingSetManager getWorkingSetManager() {
		return workingSetManager;

	}

	public IWorkingSet[] getWorkingSets() {
		return workingSets;
	}

	private void setCategoriesChecked() {
		if (getTreeViewerInput() != null) {
			for (final Category category : getTreeViewerInput().getCategories()) {
				dirTreeViewer.setChecked(category, true);
			}
		}

	}

	private void setCategoriesUnchecked() {
		if (getTreeViewerInput() != null) {
			for (final Category category : getTreeViewerInput().getCategories()) {
				dirTreeViewer.setChecked(category, false);
			}
		}
	}

	public boolean getWorkingSetCheckButtonSelection() {
		return workingSetcheckButtonSelection;
	}

	public boolean getEasyButtonSelection() {
		return easyButtonSelection;
	}

	public boolean getCopyModeCheckButtonSelection() {
		return copyModecheckButtonSelection;
	}

	public void clearArrayList() {
		if (treeViewerInput != null)
			treeViewerInput.clearArrayList();
	}

	public TreeViewerInput getTreeViewerInput() {
		treeViewerInput = (TreeViewerInput) dirTreeViewer.getInput();
		return treeViewerInput;
	}

	public CheckboxTreeViewer getTreeViewer() {
		return dirTreeViewer;
	}

	private String doParentSearch(File file) {
		File[] elementList = file.getParentFile().listFiles();
		getFileList().clear();

		for (File i : elementList) {
			setFileList(i.getName());
		}

		if (fileList.contains("module.xml")) {
			for (File element : elementList) {
				if (element.getName().equals("module.xml")) {
					return element.getParentFile().getName();
				}
			}
		}
		return doParentSearch(file.getParentFile());

	}

	public Map<String, ArrayList<String>> getModuleMap() {
		return moduleMap;
	}

	private ArrayList<String> getFileList() {
		return fileList;
	}

	private void setFileList(String file) {
		fileList.add(file);
	}

	private void createWorkingSetGroup(Composite workArea) {
		String[] workingSetIds = new String[] {
				"org.eclipse.ui.resourceWorkingSetPage", //$NON-NLS-1$
				"org.eclipse.jdt.ui.JavaWorkingSetPage" }; //$NON-NLS-1$
		
		Group workingSetGroup = new Group(workArea, SWT.NONE);
		workingSetGroup.setFont(workArea.getFont());
		workingSetGroup
				.setText(WorkbenchMessages.WorkingSetGroup_WorkingSets_group);
		workingSetGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				false));
		workingSetGroup.setLayout(new GridLayout(1, false));

		WorkingSetConfigurationBlock workingSetBlock = new WorkingSetConfigurationBlock(workingSetIds,
				WorkbenchPlugin.getDefault().getDialogSettings(),Messages.B2WizardPage_9,null,Messages.B2WizardPage_10);
		workingSetBlock.setWorkingSets(workingSetBlock
				.findApplicableWorkingSets(currentSelection));
		workingSetBlock.createContent(workingSetGroup);
		
		
	}

	private void createProjectsArea(Composite workArea) {
		Composite treeViewerComposite = new Composite(workArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.makeColumnsEqualWidth = false;
		treeViewerComposite.setLayout(layout);

		treeViewerComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

		dirTreeViewer = new CheckboxTreeViewer(treeViewerComposite, SWT.BORDER);
		dirTreeViewer.setContentProvider(new ContentProvider());
		dirTreeViewer.setLabelProvider(new LabelProvider());
		addDropSupport(dirTreeViewer);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = new PixelConverter(dirTreeViewer.getControl())
				.convertWidthInCharsToPixels(25);
		gridData.heightHint = new PixelConverter(dirTreeViewer.getControl())
				.convertHeightInCharsToPixels(10);
		dirTreeViewer.getControl().setLayoutData(gridData);
		createSelectionButtonsArea(treeViewerComposite);
	}

	private void createSelectionButtonsArea(Composite workArea) {
		Composite buttonsComposite = new Composite(workArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttonsComposite.setLayout(layout);

		buttonsComposite.setLayoutData(new GridData(
				GridData.VERTICAL_ALIGN_BEGINNING));

		selectAllBtn = new Button(buttonsComposite, SWT.PUSH);
		selectAllBtn.setText(Messages.B2WizardPage_7);
		Dialog.applyDialogFont(selectAllBtn);
		setButtonLayoutData(selectAllBtn);

		deselectAllBtn = new Button(buttonsComposite, SWT.PUSH);
		deselectAllBtn.setText(Messages.B2WizardPage_8);
		Dialog.applyDialogFont(deselectAllBtn);
		setButtonLayoutData(deselectAllBtn);

		easyButton = new Button(buttonsComposite, SWT.PUSH);
		easyButton.setImage(Activator.getImageFromPath("icons/State1.png"));
		easyButton.setEnabled(false);
		Dialog.applyDialogFont(easyButton);
		setButtonLayoutData(easyButton);

	}

	private void createRootAndWorkspaceArea(Composite workArea) {
		Composite projectGroup = new Composite(workArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 0;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		dirRadioBtn = new Button(projectGroup, SWT.RADIO);
		dirRadioBtn.setText(Messages.B2WizardPage_3);
		dirRadioBtn.setSelection(true);

		dirTxt = new Text(projectGroup, SWT.BORDER);

		GridData directoryPathData = new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		directoryPathData.widthHint = new PixelConverter(dirTxt)
				.convertWidthInCharsToPixels(25);
		dirTxt.setLayoutData(directoryPathData);

		dirBtn = new Button(projectGroup, SWT.PUSH);
		dirBtn.setText(Messages.B2WizardPage_4);
		setButtonLayoutData(dirBtn);

		workspaceRadioBtn = new Button(projectGroup, SWT.RADIO);
		workspaceRadioBtn.setText(Messages.B2WizardPage_5);

		workspaceTxt = new Text(projectGroup, SWT.BORDER);
		workspaceTxt.setEnabled(false);

		GridData archivePathData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		archivePathData.widthHint = new PixelConverter(workspaceTxt)
				.convertWidthInCharsToPixels(25);
		workspaceTxt.setLayoutData(archivePathData); 

		workspaceBtn = new Button(projectGroup, SWT.PUSH);
		workspaceBtn.setText(Messages.B2WizardPage_6);
		workspaceBtn.setEnabled(false);
		setButtonLayoutData(workspaceBtn);
	}

	private void createOptionsArea(Composite workArea) {

		Composite optionsGroup = new Composite(workArea, SWT.NONE);
		optionsGroup.setLayout(new GridLayout());
		optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		copyModecheckBtn = new Button(optionsGroup, SWT.CHECK);
		copyModecheckBtn.setText(Messages.B2WizardPage_15);
		copyModecheckBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	}

	public WorkingSetGroup getWorkingSetGroup() {
		return workingSetGroup;
	}

	public boolean doPerformFinish() {

		projectList = getSelectedProjects();
		createdProjects.removeAll(createdProjects);

		final IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					ResourcesPlugin.getWorkspace().run(
							new IWorkspaceRunnable() {
								public void run(IProgressMonitor monitor)
										throws CoreException {
									monitor.beginTask(Messages.B2Wizard_3,
											projectList.size());
									try {
										if (monitor.isCanceled()) {
											throw new OperationCanceledException();
										}
										for (int i = 0; i < projectList.size(); i++) {

											monitor.subTask(Messages.B2Wizard_4
													+ " "
													+ projectList.get(i)
															.getParent());
											if (getCopyModeCheckButtonSelection())
												createdProjects
														.add(copyProjects(i));
											else
												createdProjects
														.add(linkProjects(i));

											monitor.worked(1);

										}
									} finally {
										monitor.done();
									}
								}
							}, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		};

		runWithProgress(runnableWithProgress);

		return true;
	}

	private void runWithProgress(final IRunnableWithProgress runnable) {
		try {
			getContainer().run(true, true, runnable);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
		if (!getEasyButtonSelection())
			addToWorkingSets();
	}

	public IProject linkProjects(int projectsListPosition) {

		try {
			createProjects(projectsListPosition);
			JavaCapabilityConfigurationPage.createProject(project,
					projectDescription.getLocationURI(), null);

			if (getEasyButtonSelection()) {
				easyAddToWorkingSets(projectsListPosition);
			}
			return project;
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}

	}

	private IProject copyProjects(int projectsListPosition) {
		try {
			createProjects(projectsListPosition);
			JavaCapabilityConfigurationPage.createProject(project, workspace
					.getRoot().getLocationURI(), null);
			final JavaCapabilityConfigurationPage jcpage = new JavaCapabilityConfigurationPage();
			IJavaProject ijava = JavaCore.create(project);
			jcpage.init(ijava, null, null, false);

			try {
				jcpage.configureJavaProject(null);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}

			if (getEasyButtonSelection()) {
				easyAddToWorkingSets(projectsListPosition);
			}
			return project;
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}

	}

	private void addToWorkingSets() {

		IWorkingSet[] selectedWorkingSets = getWorkingSetGroup()
				.getSelectedWorkingSets();
		if (selectedWorkingSets == null || selectedWorkingSets.length == 0)
			return;
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
				.getWorkingSetManager();
		for (Iterator<IProject> i = createdProjects.iterator(); i.hasNext();) {
			IProject project = (IProject) i.next();
			workingSetManager.addToWorkingSets(project, selectedWorkingSets);
		}
	}

	private void easyAddToWorkingSets(int filePosition) {

		Iterator<String> it = getModuleMap().keySet().iterator();
		while (it.hasNext()) {
			String aKey = it.next();
			ArrayList<String> b = getModuleMap().get(aKey);
			for (String file : b) {
				if (file.equals(projectList.get(filePosition).getAbsolutePath())) {
					for (int i = 0; i < getWorkingSetManager()
							.getAllWorkingSets().length; i++) {
						if (getWorkingSetManager().getAllWorkingSets()[i]
								.getName().equals(aKey)) {
							getWorkingSetManager().getWorkingSet(aKey)
									.setElements(getNewElements(aKey, project));
							break;

						} else {

							if ((i + 1) == getWorkingSetManager()
									.getAllWorkingSets().length) {
								getWorkingSetManager()
										.addWorkingSet(
												getWorkingSetManager()
														.createWorkingSet(
																aKey,
																new IAdaptable[] { project }));
								break;
							}
						}
					}

				}
			}
		}
	}

	private void createProjects(int projectsListPosition) throws CoreException {
		final IPath projectFile = new Path(String.valueOf(projectList
				.get(projectsListPosition)));
		projectDescription = workspace.loadProjectDescription(projectFile);
		project = workspace.getRoot().getProject(projectDescription.getName());
	}

	private IAdaptable[] getNewElements(String key, IProject project) {

		IAdaptable[] oldElements = getWorkingSetManager().getWorkingSet(key)
				.getElements();
		IAdaptable[] newElements = new IAdaptable[oldElements.length + 1];

		for (int i = 0; i < newElements.length; i++) {
			if (i == oldElements.length) {
				newElements[i] = project;
				break;
			} else {
				newElements[i] = oldElements[i];
			}

		}

		return newElements;

	}

}