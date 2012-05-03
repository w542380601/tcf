/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.handler;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.launch.ui.nls.Messages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Delete handler implementation.
 */
public class DeleteHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			// Determine the active part
			final IWorkbenchPart part = HandlerUtil.getActivePart(event);

			// Loop over the selection and delete the elements providing
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				final Object element = iterator.next();

				// Delete the element if there is a valid delegate
				if (canDelete(element)) {
					// Determine the elements parent element
					Object parentElement = null;
					CommonViewer viewer = (CommonViewer)part.getAdapter(CommonViewer.class);
					if (viewer != null && viewer.getContentProvider() instanceof ITreeContentProvider) {
						ITreeContentProvider cp = (ITreeContentProvider)viewer.getContentProvider();
						parentElement = cp.getParent(element);
					}
					final Object finParentElement = parentElement;

					// Delete the element and refresh the tree
					if (delete(element)) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								CommonViewer viewer = (CommonViewer)part.getAdapter(CommonViewer.class);
								if (viewer != null) {
									if (finParentElement != null) {
										viewer.refresh(finParentElement, true);
									} else {
										viewer.refresh(true);
									}
								}
							}
						});
					}
				}
			}
		}

		return null;
	}

	/**
	 * Check if an element can be deleted.
	 * @param element The element to check.
	 * @return
	 */
	public boolean canDelete(Object element) {
		if (element instanceof LaunchNode) {
			LaunchNode node = (LaunchNode)element;
			return LaunchNode.TYPE_LAUNCH_CONFIG.equals(node.getType()) && !node.getLaunchConfiguration().isReadOnly();
		}
		return false;
	}

	private boolean delete(Object element) {
		Assert.isNotNull(element);

		if (element instanceof LaunchNode) {
			final LaunchNode node = (LaunchNode)element;
			if (MessageDialog.openQuestion(
							UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
							Messages.DeleteHandlerDelegate_question_title, NLS.bind(Messages.DeleteHandlerDelegate_question_message, node.getLaunchConfiguration().getName()))) {
				try {
					node.getLaunchConfiguration().delete();
					return true;
				}
				catch (Exception e) {
				}
			}
		}
		return false;
	}
}