/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.operations;

import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneStat;
import org.eclipse.tcf.services.IFileSystem.FileAttrs;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;

/**
 * The base file operation class for creating a file or a folder in the file system of Target
 * Explorer.
 */
public abstract class FSCreate extends FSOperation {
	// The folder in which a file/folder is going to be created.
	protected FSTreeNode folder;
	// The node that is created after the operation.
	protected FSTreeNode node;
	// The name of the node to be created.
	protected String name;
	// The error generated when creating the node.
	protected String error;

	/**
	 * Create an FSCreate instance with the specified folder and the name of the new node.
	 *
	 * @param folder The folder in which the new node is going to be created.
	 * @param name The new node's name.
	 */
	public FSCreate(FSTreeNode folder, String name) {
		this.folder = folder;
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation#doit()
	 */
	@Override
	public boolean doit() {
		IChannel channel = null;
		try {
			channel = openChannel(folder.peerNode.getPeer());
			IFileSystem service = getBlockingFileSystem(channel);
			if (service != null) {
				if (!folder.childrenQueried) {
					// If the children of folder is not queried, load it first.
					loadChildren(folder, service);
				}
				create(service);
				addNode(service);
				refresh(service);
			}
			else {
				String message = NLS.bind(Messages.FSOperation_NoFileSystemError, folder.peerNode.getPeerId());
				throw new TCFFileSystemException(message);
			}
		}
		catch (TCFException e) {
			error = e.getLocalizedMessage();
			return false;
		}
		finally {
			if (channel != null) Tcf.getChannelManager().closeChannel(channel);
		}
		return true;
	}

	/**
	 * Get the error message generated during creating.
	 *
	 * @return The error message.
	 */
	public String getError() {
		return error;
	}

	/**
	 * Refresh new node's stat using the file system service.
	 *
	 * @param service The file system service.
	 * @throws TCFFileSystemException Thrown when refreshing the new node's stat.
	 */
	private void refresh(final IFileSystem service) throws TCFFileSystemException {
		if (node != null) {
			final TCFFileSystemException[] errors = new TCFFileSystemException[1];
			String path = node.getLocation(true);
			service.stat(path, new DoneStat() {
				@Override
				public void doneStat(IToken token, FileSystemException error, FileAttrs attrs) {
					if (error == null) {
						node.setAttributes(attrs);
					}
					else {
						String message = NLS
						                .bind(Messages.StateManager_CannotGetFileStatMessage, new Object[] { node.name, error });
						errors[0] = new TCFFileSystemException(message, error);
					}
				}
			});
			if (errors[0] != null) {
				throw errors[0];
			}
		}
	}

	/**
	 * Add the new node to the folder and its FSModel.
	 *
	 * @param service The file system service to be used.
	 * @throws TCFFileSystemException Thrown when adding.
	 */
	void addNode(final IFileSystem service) throws TCFFileSystemException {
		if (Protocol.isDispatchThread()) {
			node = newTreeNode();
			folder.addChild(node);
		}
		else {
			final TCFFileSystemException[] errors = new TCFFileSystemException[1];
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					try {
						addNode(service);
					}
					catch (TCFFileSystemException e) {
						errors[0] = e;
					}
				}
			});
			if (errors[0] != null) throw errors[0];
		}
	}

	/**
	 * Create the new node, either a directory node or a file node.
	 *
	 * @return The new node.
	 */
	protected abstract FSTreeNode newTreeNode();

	/**
	 * Create the node in the target system.
	 *
	 * @param service The file system service used to create the new node.
	 * @throws TCFFileSystemException Thrown when creating the node.
	 */
	protected abstract void create(IFileSystem service) throws TCFFileSystemException;

	/**
	 * Get the node that is created by this operation.
	 *
	 * @return the node created.
	 */
	public FSTreeNode getNode() {
		return node;
	}
}
