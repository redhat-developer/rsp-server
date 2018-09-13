package org.jboss.tools.rsp.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashMap;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.launching.internal.LaunchingActivator;
import org.jboss.tools.rsp.launching.java.ILaunchModes;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.junit.Test;

public class ServerTypeTest {
	@Test
	public void testServerTypeAttributes() {
		ServerModel sm = new ServerModel();
		IServerType testType = new TestType();
		sm.addServerType(testType);

		Attributes attrs = sm.getRequiredAttributes(testType.getId());
		assertNotNull(attrs);
		assertEquals(attrs.getAttributes().size(), 1);
		assertEquals(attrs.getAttributes().keySet().iterator().next(), "flag.1");

		attrs = sm.getOptionalAttributes(testType.getId());
		assertNotNull(attrs);
		assertEquals(attrs.getAttributes().size(), 1);
		assertEquals(attrs.getAttributes().keySet().iterator().next(), "flag.2");

		attrs = sm.getRequiredLaunchAttributes(testType.getId());
		assertNotNull(attrs);
		assertEquals(attrs.getAttributes().size(), 1);
		assertEquals(attrs.getAttributes().keySet().iterator().next(), "flag.3");

		attrs = sm.getOptionalLaunchAttributes(testType.getId());
		assertNotNull(attrs);
		assertEquals(attrs.getAttributes().size(), 1);
		assertEquals(attrs.getAttributes().keySet().iterator().next(), "flag.4");

	}

	@Test
	public void testInvalidAttributeType() {
		ServerModel sm = new ServerModel();
		IServerType testType = new TestType() {
			@Override
			public Attributes getRequiredAttributes() {
				CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
				attrs.addAttribute("flag.1", "set", // invalid
						"flag.1.desc", null);
				return attrs.toPojo();
			}

			public Attributes getOptionalAttributes() {
				return getRequiredAttributes();
			}

			public Attributes getOptionalLaunchAttributes() {
				return getRequiredAttributes();
			}

			public Attributes getRequiredLaunchAttributes() {
				return getRequiredAttributes();
			}
		};
		sm.addServerType(testType);

		Attributes ret = sm.getRequiredAttributes(testType.getId());
		assertNotNull(ret);
		assertEquals(0, ret.getAttributes().size());

		ret = sm.getOptionalAttributes(testType.getId());
		assertNotNull(ret);
		assertEquals(0, ret.getAttributes().size());

		ret = sm.getRequiredLaunchAttributes(testType.getId());
		assertNotNull(ret);
		assertEquals(0, ret.getAttributes().size());

		ret = sm.getOptionalLaunchAttributes(testType.getId());
		assertNotNull(ret);
		assertEquals(0, ret.getAttributes().size());
	}

	@Test
	public void createServerMissingAttribute() {
		ServerModel sm = new ServerModel();
		IServerType testType = new TestType();
		sm.addServerType(testType);
		IStatus stat = sm.createServer(testType.getId(), "test.name1", new HashMap<String, Object>());
		assertNotNull(stat);
		assertFalse(stat.isOK());
	}

	@Test
	public void createServerMissingType() {
		ServerModel sm = new ServerModel();
		IStatus stat = sm.createServer("test1", "test.name1", new HashMap<String, Object>());
		assertNotNull(stat);
		assertFalse(stat.isOK());
	}

	@Test
	public void testAttributeWrongType() {
		ServerModel sm = new ServerModel();
		IServerType testType = new TestType() {
			@Override
			public Attributes getRequiredAttributes() {
				CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
				attrs.addAttribute("flag.1", 
						ServerManagementAPIConstants.ATTR_TYPE_BOOL, // invalid 
						"flag.1.desc", null);
				return attrs.toPojo();
			}
		};
		sm.addServerType(testType);
		
		
		HashMap<String, Object> attr = new HashMap<String, Object>();
		attr.put("flag.1", new Integer(5));
		IStatus stat = sm.createServer(testType.getId(), "test.name1", attr);
		assertNotNull(stat);
		assertFalse(stat.isOK());
		
		
		attr = new HashMap<String, Object>();
		attr.put("flag.1", "value");
		stat = sm.createServer(testType.getId(), "test.name1", attr);
		assertNotNull(stat);
		assertFalse(stat.isOK());

		attr = new HashMap<String, Object>();
		attr.put("flag.1", Arrays.asList(new String[] {"test1","test2"}));
		stat = sm.createServer(testType.getId(), "test.name1", attr);
		assertNotNull(stat);
		assertFalse(stat.isOK());

		attr = new HashMap<String, Object>();
		attr.put("flag.1", Arrays.asList(new HashMap<String, Object>(){{ put("One", "1"); put("Two", "2");}}));
		stat = sm.createServer(testType.getId(), "test.name1", attr);
		assertNotNull(stat);
		assertFalse(stat.isOK());

		attr = new HashMap<String, Object>();
		attr.put("flag.1", true);
		stat = sm.createServer(testType.getId(), "test.name1", attr);
		assertNotNull(stat);
		assertTrue(stat.isOK());
	}
	@Test
	public void testValidationError() {
		ServerModel sm = new ServerModel();
		IServerType testType = new TestType() {
			@Override
			public Attributes getRequiredAttributes() {
				CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
				attrs.addAttribute("flag.1", 
						ServerManagementAPIConstants.ATTR_TYPE_BOOL, // invalid 
						"flag.1.desc", null);
				return attrs.toPojo();
			}
			@Override
			public IServerDelegate createServerDelegate(IServer server) {
				return errorMock(server);
			}
		};
		sm.addServerType(testType);
		HashMap<String, Object> attr = new HashMap<String, Object>();
		attr.put("flag.1", true);
		IStatus stat = sm.createServer(testType.getId(), "test.name1", attr);
		assertNotNull(stat);
		assertFalse(stat.isOK());

	}

	
	private class TestType implements IServerType {
		@Override
		public String getId() {
			return "test1";
		}

		@Override
		public String getName() {
			return "test1.name";
		}

		@Override
		public String getDescription() {
			return "test1.desc";
		}

		@Override
		public ServerLaunchMode[] getLaunchModes() {
			return new ServerLaunchMode[] {
					new ServerLaunchMode(ILaunchModes.RUN, ILaunchModes.RUN_DESC),
					new ServerLaunchMode("TestMode", "TestMode.desc")
			};
		}

		@Override
		public Attributes getRequiredAttributes() {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			attrs.addAttribute("flag.1", 
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"flag.1.desc", null);
			return attrs.toPojo();
		}

		@Override
		public Attributes getOptionalAttributes() {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			attrs.addAttribute("flag.2", 
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"flag.2.desc", null);
			return attrs.toPojo();
		}

		@Override
		public Attributes getRequiredLaunchAttributes() {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			attrs.addAttribute("flag.3", 
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"flag.3.desc", null);
			return attrs.toPojo();
		}

		@Override
		public Attributes getOptionalLaunchAttributes() {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			attrs.addAttribute("flag.4", 
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"flag.4.desc", null);
			return attrs.toPojo();
		}


		@Override
		public IServerDelegate createServerDelegate(IServer server) {
			return okMock(server);
		}
		public IServerDelegate errorMock(IServer server) {
			IStatus error = new Status(IStatus.ERROR, LaunchingActivator.BUNDLE_ID, "Some param is invalid");
			IServerDelegate del = mock(IServerDelegate.class);
			doReturn(error).when(del).validate();
			return del;			
		}

		public IServerDelegate okMock(IServer server) {
			IServerDelegate del = mock(IServerDelegate.class);
			doReturn(Status.OK_STATUS).when(del).validate();
			return del;
		}

	}
}
