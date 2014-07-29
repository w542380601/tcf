/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.terminals.core.launcher;

import java.util.EventObject;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tcf.te.runtime.events.DisposedEvent;
import org.eclipse.tcf.te.runtime.interfaces.events.IEventListener;

/**
 * Remote terminals launcher default notification listener implementation.
 * <p>
 * <b>Note:</b> The notifications may occur in every thread!
 */
public class TerminalsLauncherEventListener extends PlatformObject implements IEventListener {
	// Reference to the parent launcher
	private final TerminalsLauncher parent;

	/**
	 * Constructor.
	 *
	 * @param parent The parent launcher. Must not be <code>null</code>.
	 */
	public TerminalsLauncherEventListener(TerminalsLauncher parent) {
		super();

		Assert.isNotNull(parent);
		this.parent = parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.events.IEventListener#eventFired(java.util.EventObject)
	 */
	@Override
	public void eventFired(EventObject event) {
		if (event instanceof DisposedEvent) {
			// Get the custom data object from the disposed event
			Object data = ((DisposedEvent)event).getData();
			// The custom data object must be of type TerminalsLauncher and match the parent launcher
			if (data instanceof TerminalsLauncher && parent.equals(data)) {
				// Terminate the remote terminal (leads to the disposal of the parent)
				parent.exit();
			}
		}
	}
}
