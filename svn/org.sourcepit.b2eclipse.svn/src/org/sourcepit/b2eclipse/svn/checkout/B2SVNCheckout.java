/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.svn.checkout;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
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
import org.eclipse.ui.PlatformUI;
import org.sourcepit.b2eclipse.svn.handler.B2SVNCheckoutHandler;
import org.sourcepit.b2eclipse.ui.B2Wizard;

/**
 * SVN Support for b2eclipse Wizard.
 * 
 * @author Marco Grupe <marco.grupe@googlemail.com>
 * @author WD
 */
@SuppressWarnings("restriction")
public class B2SVNCheckout extends B2SVNCheckoutHandler
{
   private Shell shell;
   private IRepositoryResource selectedResource;
   private IProject project;
   private final IWorkspace workspace = ResourcesPlugin.getWorkspace();

   public void openWizard()
   {
      B2Wizard wizard = new B2Wizard();
      WizardDialog dialog = new WizardDialog(shell, wizard);
      wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(project.getLocation().toFile()));
      dialog.open();
   }

   private IActionOperation createOperation(IRepositoryContainer container, File location)
   {
      // Prepare Operation Queue
      CheckoutAsOperation checkout = new CheckoutAsOperation(location, container, 3, false, true);
      AddRepositoryLocationOperation addOp = new AddRepositoryLocationOperation(container.getRepositoryLocation());
      SaveRepositoryLocationsOperation saveOp = new SaveRepositoryLocationsOperation();
      CompositeOperation compOp = new CompositeOperation(checkout.getOperationName(), checkout.getMessagesClass());
      compOp.add(checkout);
      compOp.add(addOp, new IActionOperation[] { checkout });
      compOp.add(saveOp, new IActionOperation[] { addOp });
      return compOp;
   }

   private void checkout(File location, IProgressMonitor monitor) throws CoreException
   {
      IRepositoryContainer container = (IRepositoryContainer) selectedResource;

      IActionOperation op = createOperation(container, location);
      ProgressMonitorUtility.doTaskExternal(op, monitor, ILoggedOperationFactory.EMPTY);
      IStatus status = op.getStatus();
      if (status != null && !status.isOK())
      {
         throw new CoreException(status);
      }
      else
      {
         return;
      }
   }

   private void createProjects(IProgressMonitor monitor) throws CoreException
   {
      project = workspace.getRoot().getProject(selectedResource.getName());
      project.create(monitor);
      project.open(monitor);
   }

   public void dispose()
   {
   }

   private void doRun(IProgressMonitor monitor) throws CoreException
   {
      try
      {
         createProjects(monitor);
         checkout(project.getLocation().toFile(), monitor);
      }
      catch (ResourceException e)
      {
         MessageDialog.openInformation(new Shell(), "Resource", "Resource already exists.");
         // throw new CoreException(e.getStatus());
      }
      catch (CoreException e)
      {
         throw new CoreException(e.getStatus());
      }
   }

   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException
   {
      shell = getShell(event);
      selectedResource = (IRepositoryResource) getSelectedNode(event);

      IRunnableWithProgress dialogRunnable = new IRunnableWithProgress()
      {
         public void run(IProgressMonitor monitor) throws InvocationTargetException
         {
            try
            {
               ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable()
               {
                  public void run(IProgressMonitor monitor) throws CoreException
                  {
                     doRun(monitor);
                  }
               }, monitor);
            }
            catch (OperationCanceledException e)
            {
               /* can't abort */
            }
            catch (CoreException e)
            {
               throw new InvocationTargetException(e);
            }
         }
      };

      try
      {
         // Run checkout Operation
         PlatformUI.getWorkbench().getProgressService().run(true, false, dialogRunnable);

         // refresh Workspace
         workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

         // Run Wizard on checked out Project
         openWizard();
      }
      catch (InvocationTargetException e)
      {
         e.printStackTrace();
         throw new ExecutionException(null);
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
         throw new ExecutionException(null);
      }
      catch (CoreException e)
      {
         e.printStackTrace();
         throw new ExecutionException(null);
      }

      return null;
   }
}
