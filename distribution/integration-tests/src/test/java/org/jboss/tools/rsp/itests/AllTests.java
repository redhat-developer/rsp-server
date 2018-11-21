/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests;

import org.jboss.tools.rsp.itests.wildfly.WildFlyDiscoveryTest;
import org.jboss.tools.rsp.itests.wildfly.WildFlyLaunchingTest;
import org.jboss.tools.rsp.itests.wildfly.WildFlyPublishingTest;
import org.jboss.tools.rsp.itests.wildfly.WildFlyServerModelTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    WildFlyDiscoveryTest.class,
    WildFlyServerModelTest.class,
    WildFlyLaunchingTest.class,
    WildFlyPublishingTest.class
})
public class AllTests {

}