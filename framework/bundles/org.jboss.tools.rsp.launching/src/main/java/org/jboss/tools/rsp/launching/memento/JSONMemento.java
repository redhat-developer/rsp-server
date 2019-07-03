/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.rsp.launching.memento;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JSONMemento implements IMemento {

	private JsonObject jsonObject;
	private String name;
	private boolean htmlEncode;

	public JSONMemento(JsonObject jsonObject, String name) {
		this(jsonObject, name, false);
	}

	public JSONMemento(JsonObject jsonObject, String name, boolean htmlEncode) {
		this.jsonObject = jsonObject;
		this.name = name;
		this.htmlEncode = htmlEncode;
	}

	private static Gson createGson(boolean htmlEncode) {
		GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
		if( !htmlEncode ) {
			builder = builder.disableHtmlEscaping();
		}
		Gson gson = builder.create();
		return gson;
	}
	
	public static JSONMemento loadMemento(InputStream in) {
		return createReadRoot(in);
	}
	
	public static JSONMemento createReadRoot(InputStream in) {
		return createReadRoot(in, false);
	}
	
	public static JSONMemento createReadRoot(InputStream in, boolean encode) {
		Gson gson = createGson(encode);
		try (Reader reader = new InputStreamReader(in)) {
			JsonElement rootElement = gson.fromJson(reader, JsonElement.class);
			return new JSONMemento(rootElement.getAsJsonObject(), "");
		} catch (IOException e) {
			return null;
		}
	}
	
	/*
	 * Creates root JSONMemento with "root" name
	 */
	public static JSONMemento createWriteRoot() {
		return new JSONMemento(new JsonObject(), "");
	}

	@Override
	public JSONMemento createChild(String childName) {
		JsonObject childObject = new JsonObject();
		this.jsonObject.add(childName, childObject);
		return new JSONMemento(childObject, childName);
	}

	@Override
	public JSONMemento getChild(String childName) {
		JsonObject obj = this.jsonObject.getAsJsonObject(childName);
		if( obj == null )
			return null;
		return new JSONMemento(obj, childName);
	}

	@Override
	public JSONMemento[] getChildren(String name) {
		if (name == null) {
			return new JSONMemento[0];
		}
		List<IMemento> children = new ArrayList<>();
		for (String key: this.jsonObject.keySet()) {
			if (name.equals(key) && this.jsonObject.get(name).isJsonObject()) {
				children.add(new JSONMemento(this.jsonObject.getAsJsonObject(name), name));
			}
		}
		return children.toArray(new JSONMemento[0]);
	}

	@Override
	public JSONMemento[] getChildren() {
		List<IMemento> children = new ArrayList<>();
		for (String key: this.jsonObject.keySet()) {
			if (this.jsonObject.get(key).isJsonObject()) {
				children.add(new JSONMemento(this.jsonObject.getAsJsonObject(key), key));
			}
		}
		return children.toArray(new JSONMemento[0]);
	}

	@Override
	public Float getFloat(String key) {
		return this.jsonObject.getAsJsonPrimitive(key).getAsFloat();
	}

	@Override
	public Integer getInteger(String key) {
		return this.jsonObject.getAsJsonPrimitive(key).getAsInt();
	}

	@Override
	public String getString(String key) {
		return this.jsonObject.getAsJsonPrimitive(key).getAsString();
	}

	@Override
	public Boolean getBoolean(String key) {
		return this.jsonObject.getAsJsonPrimitive(key).getAsBoolean();
	}

	@Override
	public List<String> getNames() {
		List<String> ret = new ArrayList<>();
		for (String key: this.jsonObject.keySet()) {
			if (!this.jsonObject.get(key).isJsonObject()) {
				ret.add(key);
			}
		}
		return ret;
	}

	@Override
	public String getNodeName() {
		return name;
	}

	@Override
	public void putInteger(String key, int value) {
		this.jsonObject.addProperty(key, value);
	}

	@Override
	public void putBoolean(String key, boolean value) {
		this.jsonObject.addProperty(key, value);
	}

	@Override
	public void putString(String key, String value) {
		this.jsonObject.addProperty(key, value);
	}
	
	/**
	 * Saves the memento to the given file.
	 *
	 * @param filename java.lang.String
	 * @exception java.io.IOException
	 */
	@Override
	public void saveToFile(String filename) throws IOException {
		save(new FileOutputStream(filename));
	}
	
	@Override
	public void save(OutputStream os) throws IOException {
		Gson gson = createGson(htmlEncode);
		JsonElement jsonElement = gson.fromJson(this.jsonObject, JsonElement.class);
		
		try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os))) {
			gson.toJson(jsonElement, bw);
			bw.flush();
		}
	}

}
