package org.sourcepit.b2eclipse.checkout;

import java.io.File;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Path;
import org.eclipse.egit.ui.internal.repository.tree.FolderNode;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryTreeNode;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryTreeNodeType;
import org.eclipse.jface.wizard.WizardDialog;
import org.sourcepit.b2eclipse.ui.B2Wizard;

public class B2GitCheckout extends B2CheckoutHandler {
	private String path;

	public B2GitCheckout() {

	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		RepositoryTreeNode<?> node = (RepositoryTreeNode<?>) getSelectedNodes(arg0)
				.get(0);

		if (node.getType() == RepositoryTreeNodeType.WORKINGDIR)
			path = node.getRepository().getWorkTree().toString();
		else if(node.getType() == RepositoryTreeNodeType.FOLDER)
			path = ((File) ((FolderNode) node).getObject()).getPath()
					.toString();
		else
			return null;

		B2Wizard wizard = new B2Wizard();
		WizardDialog dialog = new WizardDialog(getShell(arg0), wizard);
		wizard.getB2WizardPage().setPath(new Path(path));
		dialog.open();
		return null;
	}

}
