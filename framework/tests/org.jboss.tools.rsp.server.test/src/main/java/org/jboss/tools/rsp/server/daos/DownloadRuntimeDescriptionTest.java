/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.daos;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.tools.rsp.api.dao.DownloadRuntimeDescription;
import org.junit.Test;

public class DownloadRuntimeDescriptionTest {

	private DownloadRuntimeDescription cheddar = new DownloadRuntimeDescription(
			"cheddar-id", "cheddar-name", "v1", "https://cheese.com/cheddar/");
	private DownloadRuntimeDescription gorgonzola = new DownloadRuntimeDescription(
			"gorgonzola-id", "gorgonzola-name", "v3", "https://cheese.com/gorgonzola/");

	@Test
	public void isEqualUponEqualIdNameVersionUrl() {
		// given
		DownloadRuntimeDescription cheddar2 = new DownloadRuntimeDescription(
				cheddar.getId(),
				cheddar.getName(), 
				cheddar.getVersion(), 
				cheddar.getUrl());
		// then
		assertThat(cheddar2).isEqualTo(cheddar);
	}

	@Test
	public void isEqualUponEqualId() {
		// given
		DownloadRuntimeDescription cheddarGorgonzola = new DownloadRuntimeDescription(
				cheddar.getId(), 
				gorgonzola.getName(), 
				gorgonzola.getVersion(), 
				gorgonzola.getUrl());
		// then
		assertThat(cheddarGorgonzola).isEqualTo(cheddar);
	}

	@Test
	public void isNotEqualUponDifferentId() {
		DownloadRuntimeDescription cheddarGorgonzola = new DownloadRuntimeDescription(
				cheddar.getId(), 
				gorgonzola.getName(), 
				gorgonzola.getVersion(), 
				gorgonzola.getUrl());
		assertThat(cheddarGorgonzola).isNotEqualTo(gorgonzola);
	}
}
