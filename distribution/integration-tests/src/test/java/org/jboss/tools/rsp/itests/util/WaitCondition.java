package org.jboss.tools.rsp.itests.util;

@FunctionalInterface
public interface WaitCondition {
	boolean test();
}
