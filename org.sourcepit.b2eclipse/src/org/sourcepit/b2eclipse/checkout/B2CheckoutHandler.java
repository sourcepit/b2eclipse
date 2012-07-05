package org.sourcepit.b2eclipse.checkout;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class B2CheckoutHandler extends AbstractHandler {

	public List<?> getSelectedNodes(ExecutionEvent event)
	        throws ExecutionException
	    {
	        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
	        if(selection instanceof IStructuredSelection)
	            return ((IStructuredSelection)selection).toList();
	        else
	            return Collections.emptyList();
	    }
	
	   public Shell getShell(ExecutionEvent event)
	    {
	        return HandlerUtil.getActiveShell(event);
	    }

}
