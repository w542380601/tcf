/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.handler;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.statushandler.StatusHandlerManager;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandler;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandlerConstants;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.help.IContextHelpIds;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
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
			// Create the delete state properties container
			final IPropertiesContainer state = new PropertiesContainer();
			// Store the selection to the state as reference
			state.setProperty("selection", selection); //$NON-NLS-1$

			// Loop over the selection and delete the elements providing an IDeleteHandlerDelegate
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

					// Delete the element and refresh the parent element
					delete(element, state, new Callback() {
						@Override
						protected void internalDone(Object caller, IStatus status) {
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
					});
				}
			}
		}

		return null;
	}

	// ***** DeleteHandlerDelegate content. Clean up. *****

	private static final String KEY_CONFIRMED = "confirmed"; //$NON-NLS-1$
	private static final String KEY_SELECTION = "selection"; //$NON-NLS-1$
	private static final String KEY_PROCESSED = "processed"; //$NON-NLS-1$

	@Deprecated
	public boolean canDelete(final Object element) {
		if (element instanceof IPeerModel) {
			final AtomicBoolean canDelete = new AtomicBoolean();

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					String value = ((IPeerModel)element).getPeer().getAttributes().get("static.transient"); //$NON-NLS-1$
					canDelete.set(value != null && Boolean.parseBoolean(value.trim()));
				}
			};

			if (Protocol.isDispatchThread()) {
				runnable.run();
			}
			else {
				Protocol.invokeAndWait(runnable);
			}

			return canDelete.get();
		}
		return false;
	}

	@Deprecated
	public void delete(Object element, IPropertiesContainer state, ICallback callback) {
		Assert.isNotNull(element);
		Assert.isNotNull(state);

		if (canDelete(element)) {
			boolean confirmed = confirmDelete(state);
			if(!confirmed) {
				if (callback != null) {
					callback.done(this, Status.OK_STATUS);
				}
				return;
			}
			try {
				IURIPersistenceService service = ServiceManager.getInstance().getService(IURIPersistenceService.class);
				if (service == null) {
					throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
				}
				service.delete(element, null);
			} catch (IOException e) {
				// Create the status
				IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
								Messages.DeleteHandler_error_deleteFailed, e);

				// Fill in the status handler custom data
				IPropertiesContainer data = new PropertiesContainer();
				data.setProperty(IStatusHandlerConstants.PROPERTY_TITLE, Messages.DeleteHandler_error_title);
				data.setProperty(IStatusHandlerConstants.PROPERTY_CONTEXT_HELP_ID, IContextHelpIds.MESSAGE_DELETE_FAILED);
				data.setProperty(IStatusHandlerConstants.PROPERTY_CALLER, this);

				// Get the status handler
				IStatusHandler[] handler = StatusHandlerManager.getInstance().getHandler(element);
				if (handler.length > 0) {
					handler[0].handleStatus(status, data, null);
				}
			}

			// Get the locator model
			final ILocatorModel model = Model.getModel();
			if (model != null) {
				// Trigger a refresh of the model
				final ILocatorModelRefreshService service = model.getService(ILocatorModelRefreshService.class);
				if (service != null) {
					Protocol.invokeLater(new Runnable() {
						@Override
						public void run() {
							// Refresh the model now (must be executed within the TCF dispatch thread)
							service.refresh();
						}
					});
				}
			}
		} else {
			if (callback != null) {
				callback.done(this, Status.OK_STATUS);
			}
		}
	}

	/**
	 * Confirm the deletion with the user.
	 *
	 * @param state The state of delegation handler.
	 * @return true if the user agrees to delete or it has confirmed previously.
	 */
	private boolean confirmDelete(IPropertiesContainer state) {
	    UUID lastProcessed = (UUID) state.getProperty(KEY_PROCESSED);
	    if (lastProcessed == null || !lastProcessed.equals(state.getUUID())) {
	    	state.setProperty(KEY_PROCESSED, state.getUUID());
	    	IStructuredSelection selection = (IStructuredSelection) state.getProperty(KEY_SELECTION);
	    	if(!selection.isEmpty()) {
	    		String question = getConfirmQuestion(selection);
	    		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	    		if (MessageDialog.openQuestion(parent, Messages.DeleteHandlerDelegate_DialogTitle, question)) {
	    			state.setProperty(KEY_CONFIRMED, true);
	    		}
	    	}
	    }
	    boolean confirmed = state.getBooleanProperty(KEY_CONFIRMED);
	    return confirmed;
    }

	/**
	 * Get confirmation question displayed in the confirmation dialog.
	 *
	 * @param selection The current selection selected to delete.
	 * @return The question to ask the user.
	 */
	private String getConfirmQuestion(IStructuredSelection selection) {
	    String question;
	    if(selection.size() == 1) {
	    	Object first = selection.getFirstElement();
	    	if(first instanceof IPeerModel) {
	    		IPeerModel node = (IPeerModel)first;
	    		final IPeer peer = node.getPeer();
	    		if(Protocol.isDispatchThread()) {
	    			first = peer.getName();
	    		}
	    		else {
	    			final AtomicReference<String> ref = new AtomicReference<String>();
	    			Protocol.invokeAndWait(new Runnable(){
	    				@Override
	                    public void run() {
	    					ref.set(peer.getName());
	                    }});
	    			first = ref.get();
	    		}
	    	}
	    	question = NLS.bind(Messages.DeleteHandlerDelegate_MsgDeleteOnePeer, first);
	    }
	    else {
	    	question = NLS.bind(Messages.DeleteHandlerDelegate_MsgDeleteMultiplePeers, Integer.valueOf(selection.size()));
	    }
	    return question;
    }


}