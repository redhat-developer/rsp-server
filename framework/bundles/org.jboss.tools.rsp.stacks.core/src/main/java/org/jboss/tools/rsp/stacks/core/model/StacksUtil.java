/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.stacks.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jboss.jdf.stacks.model.Archetype;
import org.jboss.jdf.stacks.model.ArchetypeVersion;
import org.jboss.jdf.stacks.model.Runtime;
import org.jboss.jdf.stacks.model.Stacks;

public class StacksUtil {
	
	private static final Map<String, String> RUNTIMES_MAP;

	static {
		// TODO this code should eventually be moved out?
		Map<String, String> serverIdMap = new HashMap<>();
		
		serverIdMap.put("org.jboss.ide.eclipse.as.runtime.eap.60","jbosseap6runtime"); //$NON-NLS-1$ //$NON-NLS-2$
		serverIdMap.put("org.jboss.ide.eclipse.as.runtime.71","jboss-as711runtime"); //$NON-NLS-1$ //$NON-NLS-2$
		//serverIdMap.put("org.jboss.ide.eclipse.as.runtime.71","jboss-as710runtime");
		serverIdMap.put("org.jboss.ide.eclipse.as.runtime.70","jboss-as702runtime-web"); //$NON-NLS-1$ //$NON-NLS-2$
		//serverIdMap.put("org.jboss.ide.eclipse.as.runtime.70","jboss-as702runtime-full");
		
		RUNTIMES_MAP = Collections.unmodifiableMap(serverIdMap);
	}
	
	/** 
	 * This string constant is only provisional api and 
	 * may be moved without warning! 
	 */
	public static final String EAP_TYPE = "EAP"; //$NON-NLS-1$

	/** 
	 * This string constant is only provisional api and 
	 * may be moved without warning! 
	 */
	public static final String AS_TYPE = "AS"; //$NON-NLS-1$

	private StacksUtil() {
		// no need for public constructor
	}

	/**
	 * Returns the Archetype matching a given stacks archetype id
	 */
	public static Archetype getArchetype(String archetypeId, Stacks fromStacks) {
		if (fromStacks == null || archetypeId == null) {
			return null;
		}

		for (Archetype a : fromStacks.getAvailableArchetypes()) {
			if (archetypeId.equals(a.getId())) {
				return a;
			}
		}

		return null;
	}
	
	public static List<Runtime> getCompatibleRuntimes(Archetype archetype, Stacks fromStacks, String ... runtimeTypes) {
		if (fromStacks == null || archetype == null) {
			return Collections.emptyList();
		}

		List<Runtime> runtimes = new ArrayList<>();
		
		for (Runtime runtime : getRuntimes(fromStacks, runtimeTypes)) {
			List<ArchetypeVersion> versions = getCompatibleArchetypeVersions(archetype, runtime); 
			if (!versions.isEmpty()) {
				runtimes.add(runtime);
			}
		}
		return Collections.unmodifiableList(runtimes);
	}

	public static List<Runtime> getRuntimes(Stacks fromStacks, String ... runtimeTypes) {
		if (fromStacks == null) {
			return Collections.emptyList();
		}
		
		List<Runtime> runtimes = new ArrayList<>();
		List<String> runtimeTypeFilter = null;
		if (runtimeTypes != null && runtimeTypes.length > 0) {
			runtimeTypeFilter = Arrays.asList(runtimeTypes);
		}
		
		for (Runtime runtime : fromStacks.getAvailableRuntimes()) {
			
			if (runtimeTypeFilter != null) {
				String runtimeType = getRuntimeType(runtime);
				if (!runtimeTypeFilter.contains(runtimeType)) {
					continue;
				}
			}
			runtimes.add(runtime);
		}
		return Collections.unmodifiableList(runtimes);
	}

	public static Runtime getRuntime(Stacks fromStacks, String runtimeId) {
		if (fromStacks == null || runtimeId == null) {
			return null;
		}
		
		for (Runtime runtime : fromStacks.getAvailableRuntimes()) {
			if (runtimeId.equals(runtime.getId())) {
				return runtime;
			}
		}
		return null;
	}
	
	public static Runtime getRuntimeFromWtpId(Stacks fromStacks, String wtpRuntimeId) {
		if (fromStacks == null || wtpRuntimeId == null) {
			return null;
		}
		for (Runtime runtime : fromStacks.getAvailableRuntimes()) {
			Properties p = runtime.getLabels();
			if (p != null && wtpRuntimeId.equals(p.get("wtp-runtime-id"))) { //$NON-NLS-1$
				return runtime;
			}
		}
		//Fall back on hard coded map
		String stacksRuntimeId = RUNTIMES_MAP.get(wtpRuntimeId);
		return getRuntime(fromStacks, stacksRuntimeId);
	}

	public static List<Runtime> getCompatibleServerRuntimes(Archetype archetype, Stacks fromStacks) {
		return getCompatibleRuntimes(archetype, fromStacks, AS_TYPE, EAP_TYPE);
	}

	/**
	 * Returns an unmodifiable {@link List} of compatible {@link ArchetypeVersion} of an {@link Archetype} for a given {@link Runtime}. 
	 * The recommended {@link ArchetypeVersion} is always first in the list. 
	 * @param archetype
	 * @param runtime
	 * @return a non-null {@link List} of compatible {@link ArchetypeVersion}.  
	 */
	public static List<ArchetypeVersion> getCompatibleArchetypeVersions(Archetype archetype, Runtime runtime) {
		if (archetype == null || runtime == null) {
			return Collections.emptyList();
		}

		List<ArchetypeVersion> compatibleVersions = new ArrayList<>(); 
		List<ArchetypeVersion> versions = runtime.getArchetypes();
		if (versions != null && !versions.isEmpty()) {
			String bestVersion = archetype.getRecommendedVersion(); 
			for (ArchetypeVersion v : versions) {
				if (archetype.equals(v.getArchetype())) {
					if (v.getVersion().equals(bestVersion)) {
						//Put best version on top
						compatibleVersions.add(0, v);
					}
					else {
						compatibleVersions.add(v);
					}
				}
			}
		}
		return Collections.unmodifiableList(compatibleVersions);
	}

	public static boolean isRuntimeCompatible(ArchetypeVersion archetypeVersion, Runtime runtime) {
		if (archetypeVersion == null || runtime == null) {
			return false;
		}

		List<ArchetypeVersion> versions = runtime.getArchetypes();
		return versions != null && versions.contains(archetypeVersion);
	}

	public static boolean isEnterprise(Runtime runtime) {
		return EAP_TYPE.equals(getRuntimeType(runtime)); 
	}

	public static String getRuntimeType(Runtime runtime) {
		if (runtime == null) {
			return null;
		}
		
		Properties p = runtime.getLabels();
		return (String)p.get("runtime-type");  //$NON-NLS-1$
	}

	public static ArchetypeVersion getDefaultArchetypeVersion(String archetypeId, Stacks fromStacks) {
		if (fromStacks == null || archetypeId == null) {
			return null;
		}

		Archetype targetArchetype = getArchetype(archetypeId, fromStacks);
		return getDefaultArchetypeVersion(targetArchetype, fromStacks);
	}

	public static ArchetypeVersion getDefaultArchetypeVersion(Archetype archetype, Stacks fromStacks) {
		if (fromStacks == null || archetype == null) {
			return null;
		}

		List<ArchetypeVersion>  versions = fromStacks.getAvailableArchetypeVersions(); 
		for (ArchetypeVersion version : versions) {
			if (archetype.equals(version.getArchetype())
			&& version.getVersion().equals(archetype.getRecommendedVersion())) {
				return version;
			}
		}
		return (versions.isEmpty())?null:versions.get(0);
	}

	
}