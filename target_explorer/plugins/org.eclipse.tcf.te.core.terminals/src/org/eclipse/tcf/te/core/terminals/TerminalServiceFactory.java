/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.core.terminals;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.te.core.terminals.activator.CoreBundleActivator;
import org.eclipse.tcf.te.core.terminals.interfaces.ITerminalService;
import org.eclipse.tcf.te.core.terminals.nls.Messages;
import org.osgi.framework.Bundle;

/**
 * Terminal service factory implementation.
 * <p>
 * Provides access to the terminal service instance.
 */
public final class TerminalServiceFactory {
	public static ITerminalService instance = null;

	static {
		// Tries to instantiate the terminal service implementation
		// from the o.e.tcf.te.ui.terminals bundle
		Bundle bundle = Platform.getBundle("org.eclipse.tcf.te.ui.terminals"); //$NON-NLS-1$
		if (bundle != null && (bundle.getState() == Bundle.RESOLVED || bundle.getState() == Bundle.ACTIVE)) {
			try {
	            Class<?> clazz = bundle.loadClass("org.eclipse.tcf.te.ui.terminals.services.TerminalService"); //$NON-NLS-1$
	            instance = (ITerminalService) clazz.newInstance();
            }
            catch (Exception e) {
            	if (Platform.inDebugMode()) {
            		Platform.getLog(bundle).log(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), Messages.TerminalServiceFactory_error_serviceImplLoadFailed, e));
            	}
            }
		}
	}

	/**
	 * Returns the terminal service instance.
	 */
	public static ITerminalService getService() {
		return instance;
	}
}