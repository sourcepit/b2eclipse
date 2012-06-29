/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.sourcepit.b2eclipse.ui.B2Wizard;

public class Open extends AbstractHandler
   implements
      IObjectActionDelegate,
      IViewActionDelegate,
      IWorkbenchWindowActionDelegate,
      IActionDelegate2
{

   private Shell shell;
   private IRepositoryResource selectedResource;
   private IProject project;

   private CheckoutAction checkoutAction;

   public Open()
   {
      checkoutAction = new CheckoutAction();
   }

   //
   // public void selectionChanged(IAction action, ISelection selection)
   // {
   //
   // }
   //
   // public void init(IViewPart view)
   // {
   // super.init(view);
   // workbench = view.getViewSite().getWorkbenchWindow().getWorkbench();
   // shell = view.getViewSite().getShell();
   //
   // }
   //
   //
   // @Override
   // protected void execute(IAction action) throws InvocationTargetException, InterruptedException
   // {
   // super.execute(action);
   // RepositoryFolder folder = null;
   // String label = folder.getLabel();
   // IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(label);
   // B2Wizard wizard = new B2Wizard();
   // wizard.init(workbench, new StructuredSelection(project));
   // WizardDialog dialog = new WizardDialog(shell, wizard);
   // dialog.open();
   // }

   public void run(IAction action)
   {

      // checkoutAction.run(action);
      // createOperation((IRepositoryContainer)selectedResource, new File("D:/TestOrdner"));
      createProject();
      try
      {
         checkoutProject(project.getLocation().toFile(), new NullProgressMonitor());

      }
      catch (CoreException e)
      {
         // TODO: git_user_name Auto-generated catch block
         e.printStackTrace();
      }
      catch (InterruptedException e)
      {
         // TODO: git_user_name Auto-generated catch block
         e.printStackTrace();
      }

      openWizard();
   }

   public Object execute(ExecutionEvent event) throws ExecutionException
   {
      // Object result = checkoutAction.execute(event);
      Object result = null;
      createProject();
      try
      {
         checkoutProject(project.getLocation().toFile(), new NullProgressMonitor());

      }
      catch (CoreException e)
      {
         // TODO: git_user_name Auto-generated catch block
         e.printStackTrace();
      }
      catch (InterruptedException e)
      {
         // TODO: git_user_name Auto-generated catch block
         e.printStackTrace();
      }
      openWizard();
      return result;
   }

   public void init(IAction action)
   {
      checkoutAction.init(action);
   }

   public void runWithEvent(IAction action, Event event)
   {
      // createOperation((IRepositoryContainer)selectedResource, new File("D:/TestOrdner"));
      // checkoutAction.runWithEvent(action, event);
      createProject();
      try
      {
         checkoutProject(project.getLocation().toFile(), new NullProgressMonitor());

      }
      catch (CoreException e)
      {
         // TODO: git_user_name Auto-generated catch block
         e.printStackTrace();
      }
      catch (InterruptedException e)
      {
         // TODO: git_user_name Auto-generated catch block
         e.printStackTrace();
      }
      openWizard();
   }

   public void init(IWorkbenchWindow window)
   {
      checkoutAction.init(window);
   }

   public void setActivePart(IAction action, IWorkbenchPart targetPart)
   {
      checkoutAction.setActivePart(action, targetPart);
   }

   public void selectionChanged(IAction action, ISelection selection)
   {
      // checkoutAction.selectionChanged(action, selection);
      IStructuredSelection se = (IStructuredSelection) selection;
      if (selection instanceof IStructuredSelection)
      {
         Object element = se.getFirstElement();
         if (element instanceof RepositoryResource)
         {
            selectedResource = ((RepositoryResource) element).getRepositoryResource();
            System.out.println("Pfad:" + selectedResource.getUrl());
         }
      }


   }

   public void init(IViewPart view)
   {
      checkoutAction.init(view);
      shell = view.getViewSite().getShell();

   }

   public void openWizard()
   {
      B2Wizard wizard = new B2Wizard();
      WizardDialog dialog = new WizardDialog(shell, wizard);
      dialog.open();
   }

   public IActionOperation createOperation(IRepositoryContainer container, File location)
   {

      CheckoutAsOperation checkout = new CheckoutAsOperation(location, container, 3, false, true);
      AddRepositoryLocationOperation add = new AddRepositoryLocationOperation(container.getRepositoryLocation());
      SaveRepositoryLocationsOperation save = new SaveRepositoryLocationsOperation();
      CompositeOperation op = new CompositeOperation(checkout.getOperationName(), checkout.getMessagesClass());
      op.add(checkout);
      op.add(add, new IActionOperation[] { checkout });
      op.add(save, new IActionOperation[] { add });
      return op;
   }

   public void checkoutProject(File location, IProgressMonitor monitor) throws CoreException, InterruptedException
   {

      IRepositoryContainer container = (IRepositoryContainer) selectedResource;
      IActionOperation op = createOperation(container, location);
      ProgressMonitorUtility.doTaskExternal(op, monitor, ILoggedOperationFactory.EMPTY);
      IStatus status = op.getStatus();
      if (status != null && !status.isOK())
         throw new CoreException(status);
      else
         return;
   }

   public void createProject()
   {
      

   }


}
