/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.git.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryTreeNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */

@SuppressWarnings("restriction")
public abstract class B2GitCheckoutHandler extends AbstractHandler
{
   public Object getSelectedNode(ExecutionEvent event) throws ExecutionException
   {
      ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
      if (selection instanceof IStructuredSelection)
      {
         IStructuredSelection structuredSelection = (IStructuredSelection) selection;
         if (selection instanceof IStructuredSelection)
         {
            Object element = structuredSelection.getFirstElement();
            if (element instanceof RepositoryTreeNode)
            {
               final RepositoryTreeNode<?> selectedGitRepoResource = (RepositoryTreeNode<?>) element;
               return selectedGitRepoResource;
            }
         }
      }
      return null;

   }

   public Shell getShell(ExecutionEvent event)
   {
      return HandlerUtil.getActiveShell(event);
   }


}
