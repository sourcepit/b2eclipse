
package org.sourcepit.b2eclipse.git.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryTreeNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class B2GitCheckoutHandler extends AbstractHandler
{
   public Object getSelectedNode(ExecutionEvent event) throws ExecutionException
   {
      ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
      IStructuredSelection ss = (IStructuredSelection) selection;
      if (selection instanceof IStructuredSelection)
      {
         Object element = ss.getFirstElement();
         if (element instanceof RepositoryTreeNode)
         {
            final RepositoryTreeNode<?> selectedGitRepoResource = (RepositoryTreeNode<?>) element;
            return selectedGitRepoResource;
         }
      }
      return null;

   }

   public Shell getShell(ExecutionEvent event)
   {
      return HandlerUtil.getActiveShell(event);
   }


}
