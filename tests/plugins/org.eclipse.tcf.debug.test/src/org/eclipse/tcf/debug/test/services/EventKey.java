/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.services;


abstract class EventKey<V> extends Key<V> {
    private Object fClientKey;

    public EventKey(Class<V> eventClazz, Object clientKey) {
        super(eventClazz);
        fClientKey = clientKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj) && obj instanceof EventKey<?>) {
            return ((EventKey<?>)obj).fClientKey.equals(fClientKey);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + fClientKey.hashCode();
    }
}