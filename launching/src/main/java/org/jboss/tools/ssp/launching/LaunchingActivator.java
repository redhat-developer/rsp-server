package org.jboss.tools.ssp.launching;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

public class LaunchingActivator implements BundleActivator {
	public static final String BUNDLE_ID = "org.jboss.tools.ssp.launching";
	private LogService logger = null;
	private BundleContext bc = null;
	@Override
	public void start(BundleContext context) throws Exception {
		bc = context;
		System.out.println("Launching bundle activated");
		sendLogsToSysout();
		getLog().log(LogService.LOG_ERROR, "This sucks");
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		bc = null;
		logger = null;
	}
	
	private LogService getLog() {
		if (logger == null) {
			logger = findLogService();
		}
		return logger;
	}

	private LogService findLogService() {
		ServiceReference ref = bc.getServiceReference(LogService.class.getName());
		if (ref != null) {
			return (LogService) bc.getService(ref);
		}
		return null;
	}

	private void sendLogsToSysout() {
		ServiceReference ref = bc.getServiceReference(LogReaderService.class.getName());
		if (ref != null) {
			LogReaderService lrs = (LogReaderService) bc.getService(ref);
			lrs.addLogListener(new LogListener() {
				@Override
				public void logged(LogEntry arg0) {
					if( arg0.getLevel() <= LogService.LOG_WARNING) { 
						StringBuffer sb = new StringBuffer();
						sb.append("[");
						sb.append(arg0.getLevel());
						sb.append("] ");
						sb.append(arg0.getTime());
						sb.append(": ");
						sb.append(arg0.getMessage());
						System.out.println(sb.toString());
					}
				}
			});
		}
	}
	
}
