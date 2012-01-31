/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.callbacks;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.services.IProcesses.ProcessContext;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * The callback handler that handles the result of service.getContext when querying.
 */
public class QueryDoneGetContext implements ISysMonitor.DoneGetContext, IProcesses.DoneGetContext, Runnable {
	// The current context id.
	String contextId;
	// The channel used for query.
	IChannel channel;
	// The parent node to be queried.
	ProcessTreeNode parentNode;
	// The callback monitor to check if the query has finished.
	CallbackMonitor monitor;
	// The current child node
	ProcessTreeNode childNode;
	// The process context of this child node.
	IProcesses.ProcessContext pContext;
	// The flag to indicate if the system monitor service has returned. 
	volatile boolean sysMonitorDone;
	// The flag to indicate if the process service has returned.
	volatile boolean processesDone;

	/**
	 * Create an instance with the field parameters.
	 */
	public QueryDoneGetContext(String contextId, IChannel channel, CallbackMonitor monitor, ProcessTreeNode parentNode) {
		this.contextId = contextId;
		this.channel = channel;
		this.parentNode = parentNode;
		this.monitor = monitor;
		this.sysMonitorDone = false;
		this.processesDone = false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.ISysMonitor.DoneGetContext#doneGetContext(org.eclipse.tcf.protocol.IToken, java.lang.Exception, org.eclipse.tcf.services.ISysMonitor.SysMonitorContext)
	 */
    @Override
	public void doneGetContext(IToken token, Exception error, ISysMonitor.SysMonitorContext context) {
		if (error == null && context != null) {
			childNode = new ProcessTreeNode(parentNode, context);
		}
		sysMonitorDone = true;
		refreshChildren();
	}
    
    /**
     * Refresh the children under this child node.
     */
    private void refreshChildren() {
		if (sysMonitorDone && processesDone && childNode != null) {
			ISysMonitor service = channel.getRemoteService(ISysMonitor.class);
			if (service != null) {
				Queue<ProcessTreeNode> queue = new ConcurrentLinkedQueue<ProcessTreeNode>();
				service.getChildren(childNode.id, new RefreshDoneGetChildren(this, queue, channel, service, childNode));
			}
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IProcesses.DoneGetContext#doneGetContext(org.eclipse.tcf.protocol.IToken, java.lang.Exception, org.eclipse.tcf.services.IProcesses.ProcessContext)
	 */
	@Override
    public void doneGetContext(IToken token, Exception error, ProcessContext context) {
		if (error == null && context != null) {
			pContext = context;
		}
		processesDone = true;
		refreshChildren();
    }

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
    public void run() {
		if (childNode != null) {
			parentNode.addChild(childNode);
			childNode.setProcessContext(pContext);
		}
		monitor.unlock(contextId);
    }
}