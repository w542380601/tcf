/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.handlers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSClipboard;
import org.eclipse.tcf.te.tcf.filesystem.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.handlers.HandlerUtil;
/**
 * The handler that copies the selected files or folders to the clip board.
 */
public class CopyFilesHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FSClipboard cb = UIPlugin.getDefault().getClipboard();
		List<URL> files = new ArrayList<URL>();
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		List<FSTreeNode> nodes = selection.toList();
		for (FSTreeNode node : nodes) {
			files.add(node.getLocationURL());
		}
		// Copy these files to the clip board.
		cb.copyFiles(files);
		// Refresh the file system tree to display the decorations of the cut nodes.
		FSModel.getInstance().fireNodeStateChanged(null);
		return null;
	}
}
