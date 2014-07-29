/*******************************************************************************
 * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.ui.handler;

/**
 * Run diagnostics tests loop command handler implementation.
 */
public class RunTestsLoopCommandHandler extends AbstractDiagnosticsCommandHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.launch.ui.handler.AbstractDiagnosticsCommandHandler#runAsLoop()
	 */
	@Override
	protected boolean runAsLoop() {
	    return true;
	}
}
