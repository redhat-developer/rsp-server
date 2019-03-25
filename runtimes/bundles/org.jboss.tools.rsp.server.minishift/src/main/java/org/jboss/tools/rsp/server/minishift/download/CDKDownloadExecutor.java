package org.jboss.tools.rsp.server.minishift.download;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeWorkflowConstants;
import org.jboss.tools.rsp.server.minishift.discovery.MinishiftDiscovery;
import org.jboss.tools.rsp.server.minishift.impl.Activator;
import org.jboss.tools.rsp.server.minishift.servertype.IMinishiftServerAttributes;
import org.jboss.tools.rsp.server.minishift.servertype.impl.MinishiftServerTypes;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.runtimes.AbstractStacksDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.wildfly.runtimes.download.AbstractDownloadManagerExecutor;

public class CDKDownloadExecutor extends AbstractDownloadManagerExecutor {

	public CDKDownloadExecutor(DownloadRuntime dlrt, IServerManagementModel model) {
		super(dlrt, model);
	}

	@Override
	protected IStatus createServer(DownloadRuntime dlrt, String newHome, TaskModel tm) {
		// The wtp-runtime id is used in stacks.yaml, 
		String wtpRuntimeId = dlrt.getProperty(AbstractStacksDownloadRuntimesProvider.PROP_WTP_RUNTIME);
		
		// but rsp-server doesn't really have a server / runtime split. 
		// So now we need to get the rsp-server server type id
		String serverType = MinishiftServerTypes.RUNTIME_TO_SERVER.get(wtpRuntimeId);
		
		// Now we have to somehow create this thing... ... ... 
		Set<String> serverIds = getServerModel().getServers().keySet();
		String suggestedId = new File(newHome).getName();
		String chosenId = getUniqueServerId(suggestedId, serverIds);
		
		Map<String,Object> attributes = new HashMap<>();
		File binFile = new MinishiftDiscovery().getMinishiftBinaryFromFolder(new File(newHome), false);
		if( binFile == null ) {
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID, "Unable to locate minishift binary");
		}
		binFile.setExecutable(true);
		attributes.put(ServerManagementAPIConstants.SERVER_HOME_FILE, binFile.getAbsolutePath());
		attributes.put(IMinishiftServerAttributes.MINISHIFT_REG_USERNAME, 
				(String)tm.getObject(IDownloadRuntimeWorkflowConstants.USERNAME_KEY));
		attributes.put(IMinishiftServerAttributes.MINISHIFT_REG_PASSWORD, 
				(String)tm.getObject(IDownloadRuntimeWorkflowConstants.PASSWORD_KEY));
		
		CreateServerResponse response = getServerModel().createServer(serverType, chosenId, attributes);
		return StatusConverter.convert(response.getStatus());
	}

}
