/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.filesystem.url;

import junit.framework.Test;
import junit.framework.TestSuite;

public class URLTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("File System: URL Tests"); //$NON-NLS-1$
		suite.addTestSuite(TcfURLTests.class);
		suite.addTestSuite(URLReadTests.class);
		suite.addTestSuite(URLWriteTests.class);
		return suite;
	}
}
