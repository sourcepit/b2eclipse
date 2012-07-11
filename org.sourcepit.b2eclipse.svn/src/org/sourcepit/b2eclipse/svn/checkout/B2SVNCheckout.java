/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.svn.checkout;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.CheckoutAsOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRepositoryLocationOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.ILoggedOperationFactory;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.action.remote.CheckoutAction;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.sourcepit.b2eclipse.ui.B2Wizard;

public class B2SVNCheckout implements IViewActionDelegate, IActionDelegate2 {

	private Shell shell;
	private IRepositoryResource selectedResource;
	private IProject project;
	private IWorkspace workspace;
	private CheckoutAction checkoutAction;

	public B2SVNCheckout() {
		checkoutAction = new CheckoutAction();
	}

	public void run(IAction action) {
	}

	public void init(IAction action) {
		checkoutAction.init(action);
	}

	public void runWithEvent(IAction action, Event event) {
		createProjects();
		try {
			checkout(project.getLocation().toFile(), new NullProgressMonitor());
			workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE,
					new NullProgressMonitor());

		} catch (CoreException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
		openWizard();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		IStructuredSelection ss = (IStructuredSelection) selection;
		if (selection instanceof IStructuredSelection) {
			Object element = ss.getFirstElement();
			if (element instanceof RepositoryResource) {
				selectedResource = ((RepositoryResource) element)
						.getRepositoryResource();
			}
		}

	}

	public void init(IViewPart view) {
		// checkoutAction.init(view);
		shell = view.getViewSite().getShell();

	}

	public void openWizard() {
		B2Wizard wizard = new B2Wizard();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		wizard.getB2WizardPage().setPath(project.getLocation());
		dialog.open();
	}

	public IActionOperation createOperation(IRepositoryContainer container,
			File location) {

		CheckoutAsOperation checkout = new CheckoutAsOperation(location,
				container, 3, false, true);
		AddRepositoryLocationOperation add = new AddRepositoryLocationOperation(
				container.getRepositoryLocation());
		SaveRepositoryLocationsOperation save = new SaveRepositoryLocationsOperation();
		CompositeOperation op = new CompositeOperation(
				checkout.getOperationName(), checkout.getMessagesClass());
		op.add(checkout);
		op.add(add, new IActionOperation[] { checkout });
		op.add(save, new IActionOperation[] { add });
		return op;
	}

	public void checkout(File location, IProgressMonitor monitor)
			throws CoreException, InterruptedException {

		IRepositoryContainer container = (IRepositoryContainer) selectedResource;
		IActionOperation op = createOperation(container, location);
		ProgressMonitorUtility.doTaskExternal(op, monitor,
				ILoggedOperationFactory.EMPTY);
		IStatus status = op.getStatus();
		if (status != null && !status.isOK())
			throw new CoreException(status);
		else
			return;
	}

	public void createProjects() {

		workspace = ResourcesPlugin.getWorkspace();
		project = workspace.getRoot().getProject(selectedResource.getName());

		try {
			project.create(new NullProgressMonitor());
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}

	}

	public void dispose() {
	}

}
