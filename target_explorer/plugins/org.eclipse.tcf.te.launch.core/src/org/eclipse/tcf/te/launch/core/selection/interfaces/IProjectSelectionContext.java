/*******************************************************************************
 * Copyright (c) 2012, 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.selection.interfaces;

import org.eclipse.core.resources.IProject;

/**
 * A selection context providing the project context for the launch.
 */
public interface IProjectSelectionContext extends ISelectionContext {

	/**
	 * Returns the project context.
	 *
	 * @return The project context or <code>null</code>.
	 */
	public IProject getProjectCtx();
}
