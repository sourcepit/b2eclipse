package org.sourcepit.b2eclipse.checkout;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public interface ICheckout extends IViewActionDelegate, IActionDelegate2 {
	
	void run(IAction action);
	void init(IAction action);
	void runWithEvent(IAction action, Event event);
	void selectionChanged(IAction action, ISelection selection);
	void init(IViewPart view);
	void openWizard();
	void createProjects();
	void dispose();
	

}
