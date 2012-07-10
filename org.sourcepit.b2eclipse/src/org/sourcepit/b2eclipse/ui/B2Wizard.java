/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.sourcepit.b2eclipse.Activator;

/**
 * @author Marco Grupe
 */

public class B2Wizard extends Wizard implements IImportWizard,
		ISelectionListener {
	private B2WizardPage modulePage;
	private List<File> projectList;
	private static final String DIALOG_SETTING_FILE = "workingSets.xml";
	private File workingSetsXML;
	private DialogSettings dialogSettings;

	public B2Wizard() {
		super();
		setWindowTitle(Messages.B2Wizard_1);
		setDefaultPageImageDescriptor(ImageDescriptor.createFromFile(
				B2WizardPage.class, "ProjectFolder.gif"));
		modulePage = new B2WizardPage(Messages.B2Wizard_2);
		setNeedsProgressMonitor(true);
		dialogSettings = new DialogSettings("workingSets");

		workingSetsXML = new File(DIALOG_SETTING_FILE);
		modulePage.setWorkingSetXML(workingSetsXML);

		if (!workingSetsXML.exists()) {
			try {

				workingSetsXML.createNewFile();
				dialogSettings.save(DIALOG_SETTING_FILE);

			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		try {
			dialogSettings.load(DIALOG_SETTING_FILE);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		setDialogSettings(dialogSettings);

		addPage(modulePage);

	}

	/**
	 * After pressing the finish button the selected projects will be create
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		try {
			return doPerformFinish();
		} catch (RuntimeException e) {
			Activator.error(e);
		}
		return false;
	}

	private boolean doPerformFinish() {
		saveDialogSettings();

		projectList = modulePage.getSelectedProjects();

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
										for (int i = 0; i < projectList.size(); i++) {
											if (monitor.isCanceled()) {
												return;
											}
											monitor.subTask(Messages.B2Wizard_4
													+ " "
													+ projectList.get(i)
															.getParent());
											if (modulePage
													.getCopyModeCheckButtonSelection())
												copyProjects(i);
											else
												createProjects(i);
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

	private void runWithProgress(
			final IRunnableWithProgress runnableWithProgress) {
		try {
			getContainer().run(true, true, runnableWithProgress);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	private void saveDialogSettings() {
		try {
			getDialogSettings().save(DIALOG_SETTING_FILE);
		} catch (IOException e1) {
			throw new IllegalStateException(e1);
		}
	}

	/**
	 * By clicking project in the package explorer firstElement gets the
	 * absolute path of the selected project {@inheritDoc}
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		workbench.getActiveWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);

		if (selection instanceof IStructuredSelection) {

			final Object firstElement = selection.getFirstElement();

			if (firstElement instanceof IAdaptable) {
				final IResource selectedResource = (IResource) ((IAdaptable) firstElement)
						.getAdapter(IResource.class);
				if (selectedResource != null) {
					final IPath location;
					if (selectedResource.getType() == IResource.FILE) {
						location = selectedResource.getParent().getLocation();
					} else {
						location = selectedResource.getLocation();
					}
					modulePage.setPath(location);
				}
			}
		}

	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

		if (part != B2Wizard.this) {
			init(PlatformUI.getWorkbench(), (IStructuredSelection) selection);
		}

	}

	/**
	 * disposes the SelectionListener
	 */
	public void dispose() {
		modulePage.clearArrayList();
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService().removeSelectionListener(this);
		super.dispose();
	}

	public B2WizardPage getB2WizardPage() {
		return modulePage;
	}

	private void createProjects(int projectsListPosition) {

		try {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			final IPath projectFile = new Path(String.valueOf(projectList
					.get(projectsListPosition)));
			final IProjectDescription projectDescription = workspace
					.loadProjectDescription(projectFile);
			final IProject project = workspace.getRoot().getProject(
					projectDescription.getName());
			JavaCapabilityConfigurationPage.createProject(project,
					projectDescription.getLocationURI(), null);

			if (modulePage.getWorkingSetCheckButtonSelection()
					&& modulePage.getWorkingSet() != null) {
				modulePage.getWorkingSetManager().addToWorkingSets(project,
						modulePage.getWorkingSet());
			}
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}

	}

	private void copyProjects(int projectsListPosition) {

		
	}

}
