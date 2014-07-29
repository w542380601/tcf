/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.tabs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.ui.terminals.nls.Messages;
import org.eclipse.tm.internal.terminal.control.ITerminalListener;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

/**
 * Terminals tab default terminal listener implementation.
 */
@SuppressWarnings("restriction")
public class TabTerminalListener implements ITerminalListener {
	/* default */ final TabFolderManager tabFolderManager;
	private final CTabItem tabItem;
	private final String tabItemTitle;

	/**
	 * Constructor.
	 *
	 * @param tabFolderManager The parent tab folder manager. Must not be <code>null</code>.
	 * @param tabItem The parent tab item. Must not be <code>null</code>.
	 */
	public TabTerminalListener(TabFolderManager tabFolderManager, CTabItem tabItem) {
		super();
		Assert.isNotNull(tabFolderManager);
		this.tabFolderManager = tabFolderManager;
		Assert.isNotNull(tabItem);
		this.tabItem = tabItem;

		// Remember the original tab item title
		tabItemTitle = tabItem.getText();
	}

	/**
	 * Returns the associated parent tab item.
	 *
	 * @return The parent tab item.
	 */
	protected final CTabItem getTabItem() {
		return tabItem;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.terminal.control.ITerminalListener#setState(org.eclipse.tcf.internal.terminal.provisional.api.TerminalState)
	 */
	@Override
	public void setState(final TerminalState state) {
		// The tab item must have been not yet disposed
		final CTabItem item = getTabItem();
		if (item == null || item.isDisposed()) return;

		// Run asynchronously in the display thread
		item.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				// Update the tab item title
				String newTitle = getTerminalConsoleTabTitle(state);
				if (newTitle != null) item.setText(newTitle);

				// Turn off the command field (if necessary)
				TabCommandFieldHandler handler = tabFolderManager.getTabCommandFieldHandler(item);
				if (TerminalState.CLOSED.equals(state) && handler != null && handler.hasCommandInputField()) {
					handler.setCommandInputField(false);
					// Trigger a selection changed event to update the action enablements
					// and the status line
					ISelectionProvider provider = tabFolderManager.getParentView().getViewSite().getSelectionProvider();
					Assert.isNotNull(provider);
					provider.setSelection(provider.getSelection());
				} else {
					// Update the status line
					tabFolderManager.updateStatusLine();
				}
			}
		});
	}

	/**
	 * Returns the title to set to the terminal console tab for the given state.
	 * <p>
	 * <b>Note:</b> This method is called from {@link #setState(TerminalState)} and
	 *              is expected to by called within the UI thread.
	 *
	 * @param state The terminal state. Must not be <code>null</code>.
	 * @return The terminal console tab title to set or <code>null</code> to leave the title unchanged.
	 */
	protected String getTerminalConsoleTabTitle(TerminalState state) {
		Assert.isNotNull(state);
		Assert.isNotNull(Display.findDisplay(Thread.currentThread()));

		// The tab item must have been not yet disposed
		CTabItem item = getTabItem();
		if (item == null || item.isDisposed()) return null;

		// Get the current tab title
		String oldTitle = item.getText();

		// Construct the new title
		String newTitle = null;

		if (TerminalState.CLOSED.equals(state)) {
			newTitle = NLS.bind(Messages.TabTerminalListener_consoleClosed, tabItemTitle, tabFolderManager.state2msg(item, state));
		}
		else if (TerminalState.CONNECTING.equals(state)) {
			newTitle = NLS.bind(Messages.TabTerminalListener_consoleConnecting, tabItemTitle, tabFolderManager.state2msg(item, state));
		}
		else if (TerminalState.CONNECTED.equals(state)) {
			newTitle = tabItemTitle;
		}

		return newTitle != null && !newTitle.equals(oldTitle) ? newTitle : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.terminal.control.ITerminalListener#setTerminalTitle(java.lang.String)
	 */
	@Override
	public void setTerminalTitle(String title) {
	}
}
