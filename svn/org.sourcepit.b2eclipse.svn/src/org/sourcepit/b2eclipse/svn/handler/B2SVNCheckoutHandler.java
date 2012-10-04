/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.sourcepit.b2eclipse.svn.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */

public abstract class B2SVNCheckoutHandler extends AbstractHandler
{
   public Object getSelectedNode(ExecutionEvent event) throws ExecutionException
   {
      ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
      IStructuredSelection ss = (IStructuredSelection) selection;
      if (selection instanceof IStructuredSelection)
      {
         Object element = ss.getFirstElement();
         if (element instanceof RepositoryResource)
         {
            final IRepositoryResource selectedSVNRepoResource = ((RepositoryResource) element).getRepositoryResource();
            return selectedSVNRepoResource;
         }
      }
      return null;

   }

   public Shell getShell(ExecutionEvent event)
   {
      return HandlerUtil.getActiveShell(event);
   }
}
