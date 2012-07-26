/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.sourcepit.b2eclipse.Activator;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */

public class B2Wizard extends Wizard implements IImportWizard, ISelectionListener
{
   private B2WizardPage modulePage;
   private IStructuredSelection currentSelection = null;


   public B2Wizard()
   {
      super();

      modulePage = new B2WizardPage(Messages.B2Wizard_2, currentSelection);
      setNeedsProgressMonitor(true);

      addPage(modulePage);

   }

   /**
    * After pressing the finish button the selected projects will be create {@inheritDoc}
    */

   public boolean performFinish()
   {
      try
      {
         return modulePage.doPerformFinish();
      }
      catch (RuntimeException e)
      {
         Activator.error(e);
      }
      return false;
   }

   /**
    * By clicking project in the package explorer firstElement gets the absolute path of the selected project
    * {@inheritDoc}
    */
   public void init(IWorkbench workbench, IStructuredSelection selection)
   {
      setWindowTitle(Messages.B2Wizard_1);
      Image projectFolder = Activator.getImageFromPath("icons/ProjectFolder.gif");
      setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(projectFolder));
      this.currentSelection = selection;
      workbench.getActiveWorkbenchWindow().getSelectionService().addSelectionListener(this);

      if (selection instanceof IStructuredSelection)
      {

         final Object firstElement = selection.getFirstElement();

         if (firstElement instanceof IAdaptable)
         {
            final IResource selectedResource = (IResource) ((IAdaptable) firstElement).getAdapter(IResource.class);
            if (selectedResource != null)
            {
               final IPath location;
               if (selectedResource.getType() == IResource.FILE)
               {
                  location = selectedResource.getParent().getLocation();
               }
               else
               {
                  location = selectedResource.getLocation();
               }
               modulePage.setPath(location);
            }
         }
      }

   }

   public void selectionChanged(IWorkbenchPart part, ISelection selection)
   {

      if (part != B2Wizard.this)
      {
         init(PlatformUI.getWorkbench(), (IStructuredSelection) selection);
      }

   }

   /**
    * disposes the SelectionListener
    */
   public void dispose()
   {
      modulePage.clearArrayList();
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().removeSelectionListener(this);
      super.dispose();
   }

   public B2WizardPage getB2WizardPage()
   {
      return modulePage;
   }

}
