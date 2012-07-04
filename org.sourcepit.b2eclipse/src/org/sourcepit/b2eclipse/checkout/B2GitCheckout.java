package org.sourcepit.b2eclipse.checkout;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.egit.ui.internal.branch.BranchOperationUI;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryTreeNode;
import org.eclipse.jgit.lib.Ref;

public class B2GitCheckout extends B2CheckoutHandler {

	public B2GitCheckout() {

	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		System.out.println("Bin reingegangen!");
		RepositoryTreeNode node = (RepositoryTreeNode) getSelectedNodes(arg0)
				.get(0);
		if (!(node.getObject() instanceof Ref)) {
			return null;
		} else {
			Ref ref = (Ref) node.getObject();
			System.out.println("Hallo" + ref.getName());
			org.eclipse.jgit.lib.Repository repo = node.getRepository();
			BranchOperationUI op = BranchOperationUI.checkout(repo,
					ref.getName());
			op.start();
			return null;
		}
	}

}
