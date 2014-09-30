/*******************************************************************************
 * Copyright (c) 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.local.showin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tcf.te.ui.terminals.interfaces.ILauncherDelegate;
import org.eclipse.tcf.te.ui.terminals.launcher.LauncherDelegateManager;
import org.eclipse.tcf.te.ui.terminals.local.showin.interfaces.IExternalExecutablesProperties;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Dynamic "Show In" contribution items implementation.
 */
public class DynamicContributionItems extends CompoundContributionItem implements IWorkbenchContribution {
	// Service locator to located the handler service.
	protected IServiceLocator serviceLocator;
	// Reference to the local terminal launcher delegate
	/* default */ ILauncherDelegate delegate;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.menus.IWorkbenchContribution#initialize(org.eclipse.ui.services.IServiceLocator)
	 */
	@Override
	public void initialize(IServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;

		// Get the local terminal launcher delegate
		delegate = LauncherDelegateManager.getInstance().getLauncherDelegate("org.eclipse.tcf.te.ui.terminals.local.launcher.local", false); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
	 */
	@Override
	protected IContributionItem[] getContributionItems() {
		List<IContributionItem> items = new ArrayList<IContributionItem>();

		if (delegate != null) {
			List<Map<String, Object>> l = ExternalExecutablesManager.load();
			if (l != null && !l.isEmpty()) {
				for (Map<String, Object> executableData : l) {
					String name = (String) executableData.get(IExternalExecutablesProperties.PROP_NAME);
					String path = (String) executableData.get(IExternalExecutablesProperties.PROP_PATH);
					String args = (String) executableData.get(IExternalExecutablesProperties.PROP_ARGS);
					String icon = (String) executableData.get(IExternalExecutablesProperties.PROP_ICON);

					if (name != null && !"".equals(name) && path != null && !"".equals(path)) { //$NON-NLS-1$ //$NON-NLS-2$
						IAction action = createAction(name, path, args);

						ImageData id = icon != null ? ExternalExecutablesManager.loadImage(icon) : null;
						if (id != null) {
							ImageDescriptor desc = ImageDescriptor.createFromImageData(id);
							if (desc != null) action.setImageDescriptor(desc);
						}

						IContributionItem item = new ActionContributionItem(action);
						items.add(item);
					}
				}
			}
		}

		return items.toArray(new IContributionItem[items.size()]);
	}

	/**
	 * Creates the action to execute.
	 *
	 * @param label The label. Must not be <code>null</code>.
	 * @param path The executable path. Must not be <code>null</code>.
	 * @param args The executable arguments or <code>null</code>.
	 *
	 * @return The action to execute.
	 */
	protected IAction createAction(final String label, final String path, final String args) {
		Assert.isNotNull(label);
		Assert.isNotNull(path);

		IAction action = new Action(label) {
			@Override
			public void run() {
				Assert.isNotNull(delegate);

				ISelectionService service = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
				ISelection selection = service != null ? service.getSelection() : null;
				if (selection != null && selection.isEmpty()) selection = null;

				IPropertiesContainer properties = new PropertiesContainer();
				properties.setProperty(ITerminalsConnectorConstants.PROP_DELEGATE_ID, delegate.getId());
		    	properties.setProperty(ITerminalsConnectorConstants.PROP_CONNECTOR_TYPE_ID, "org.eclipse.tcf.te.ui.terminals.type.local"); //$NON-NLS-1$
		    	if (selection != null) properties.setProperty(ITerminalsConnectorConstants.PROP_SELECTION, selection);
		    	properties.setProperty(ITerminalsConnectorConstants.PROP_PROCESS_PATH, path);
		    	if (args != null) properties.setProperty(ITerminalsConnectorConstants.PROP_PROCESS_ARGS, args);

		    	delegate.execute(properties, new Callback() {
		    		@Override
		    		protected void internalDone(Object caller, IStatus status) {
		    		}
		    	});
			}
		};

		return action;
	}
}