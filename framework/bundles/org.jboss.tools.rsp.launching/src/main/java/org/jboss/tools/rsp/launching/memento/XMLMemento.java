/*******************************************************************************
 * Copyright (c) 2006-2017 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/

package org.jboss.tools.rsp.launching.memento;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Stolen from webtools wst.server.core
 * 
 * @since 1.1
 */
public final class XMLMemento implements IMemento {
	private static final Logger LOG = LoggerFactory.getLogger(XMLMemento.class);

	private Document factory;
	private Element element;

	/**
	 * Answer a memento for the document and element.  For simplicity
	 * you should use createReadRoot and createWriteRoot to create the initial
	 * mementos on a document.
	 */
	public XMLMemento(Document doc, Element el) {
		factory = doc;
		element = el;
	}

	@Override
	public IMemento createChild(String type) {
		Element child = factory.createElement(type);
		element.appendChild(child);
		return new XMLMemento(factory, child);
	}

	public void removeChild(XMLMemento child) {
		element.removeChild(child.element);
	}
	
	/**
	 * Create a Document from a Reader and answer a root memento for reading
	 * a document.
	 */
	public static XMLMemento createReadRoot(InputStream in) {
		
		Document document = null;
		try {	
			DocumentBuilderFactory factory = createDocumentBuilderFactory();
			DocumentBuilder parser = factory.newDocumentBuilder();
			document = parser.parse(new InputSource(in));
			Node node = document.getFirstChild();
			while( !(node instanceof Element) && node.getNextSibling() != null ) {
				node = node.getNextSibling();
			}
			if (node instanceof Element)
				return new XMLMemento(document, (Element) node);
			
			// ignore
		} catch (ParserConfigurationException | SAXException | IOException e) {
			logError(e);
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				// ignore
			}
		}
		return null;
	}
	
	private static void logError(Exception t) {
		String msg = (t == null || t.getMessage() == null ? "Unknown Error" : t.getMessage());
		LOG.error(msg, t);
	}

	/**
	 * Answer a root memento for writing a document.
	 *
	 * @param type a type
	 * @return a memento
	 */
	public static XMLMemento createWriteRoot(String type) {
		Document document;
		try {
			document = createDocumentBuilderFactory().newDocumentBuilder().newDocument();
			Element element = document.createElement(type);
			document.appendChild(element);
			return new XMLMemento(document, element);
		} catch (ParserConfigurationException e) {
			throw new Error(e);
		}
	}

	@Override
	public IMemento getChild(String type) {
		// Get the nodes.
		NodeList nodes = element.getChildNodes();
		int size = nodes.getLength();
		if (size == 0)
			return null;

		// Find the first node which is a child of this node.
		for (int nX = 0; nX < size; nX ++) {
			Node node = nodes.item(nX);
			if (node instanceof Element) {
				Element element2 = (Element)node;
				if (element2.getNodeName().equals(type))
					return new XMLMemento(factory, element2);
			}
		}

		// A child was not found.
		return null;
	}

	@Override
	public IMemento [] getChildren(String type) {
		// Get the nodes.
		NodeList nodes = element.getChildNodes();
		int size = nodes.getLength();
		if (size == 0)
			return new IMemento[0];

		// Extract each node with given type.
		List<Element> list = new ArrayList<>(size);
		for (int nX = 0; nX < size; nX ++) {
			Node node = nodes.item(nX);
			if (node instanceof Element) {
				Element element2 = (Element)node;
				if (element2.getNodeName().equals(type))
					list.add(element2);
			}
		}

		// Create a memento for each node.
		size = list.size();
		IMemento [] results = new IMemento[size];
		for (int x = 0; x < size; x ++) {
			results[x] = new XMLMemento(factory, list.get(x));
		}
		return results;
	}
	
	@Override
	public IMemento [] getChildren() {
		// Get the nodes.
		NodeList nodes = element.getChildNodes();
		int size = nodes.getLength();
		if (size == 0)
			return new IMemento[0];

		// Extract each node with given type.
		List<Element> list = new ArrayList<>(size);
		for (int nX = 0; nX < size; nX ++) {
			Node node = nodes.item(nX);
			if (node instanceof Element) {
				Element element2 = (Element)node;
				list.add(element2);
			}
		}

		// Create a memento for each node.
		size = list.size();
		IMemento [] results = new IMemento[size];
		for (int x = 0; x < size; x ++) {
			results[x] = new XMLMemento(factory, list.get(x));
		}
		return results;
	}

	public String[] getChildNames() {
		// Get the nodes.
		NodeList nodes = element.getChildNodes();
		int size = nodes.getLength();
		if (size == 0)
			return new String[0];

		// Extract each node with given type.
		List<String> list = new ArrayList<>();
		for (int nX = 0; nX < size; nX ++) {
			Node node = nodes.item(nX);
			if (node instanceof Element) {
				Element element2 = (Element)node;
				if (!list.contains(element2.getNodeName()))
					list.add(element2.getNodeName());
			}
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public String getNodeName() {
		return element.getNodeName();
	}
	
	/**
	 * Return the contents of this memento as a byte array.
	 *
	 * @return byte[]
	 * @throws IOException if anything goes wrong
	 */
	public byte[] getContents() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		save(out);
		return out.toByteArray();
	}

	/**
	 * Returns an input stream for writing to the disk with a local locale.
	 *
	 * @return java.io.InputStream
	 * @throws IOException if anything goes wrong
	 */
	public InputStream getInputStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		save(out);
		return new ByteArrayInputStream(out.toByteArray());
	}

	@Override
	public Float getFloat(String key) {
		Attr attr = element.getAttributeNode(key);
		if (attr == null)
			return null;
		String strValue = attr.getValue();
		try {
			return Float.valueOf(strValue);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Integer getInteger(String key) {
		Attr attr = element.getAttributeNode(key);
		if (attr == null)
			return null;
		String strValue = attr.getValue();
		try {
			return Integer.valueOf(strValue);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public String getString(String key) {
		Attr attr = element.getAttributeNode(key);
		if (attr == null)
			return null;
		return attr.getValue();
	}

	@Override
	public List<String> getNames() {
		NamedNodeMap map = element.getAttributes();
		int size = map.getLength();
		List<String> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			Node node = map.item(i);
			String name = node.getNodeName();
			list.add(name);
		}
		return list;
	}

	/**
	 * Loads a memento from the given filename.
	 *
	 * @param in java.io.InputStream
	 * @return org.eclipse.ui.IMemento
	 */
	public static IMemento loadMemento(InputStream in) {
		return createReadRoot(in);
	}

	/**
	 * Loads a memento from the given filename.
	 *
	 * @param filename java.lang.String
	 * @return org.eclipse.ui.IMemento
	 * @exception java.io.IOException
	 */
	public static IMemento loadMemento(String filename) throws IOException {
		try(InputStream in = new BufferedInputStream(new FileInputStream(filename))) {
			return XMLMemento.createReadRoot(in);
		}
	}

	@Override
	public void putInteger(String key, int n) {
		element.setAttribute(key, String.valueOf(n));
	}

	@Override
	public void putString(String key, String value) {
		if (value == null)
			return;
		element.setAttribute(key, value);
	}

	@Override
	public void save(OutputStream os) throws IOException {
		Result result = new StreamResult(os);
		Source source = new DOMSource(factory);
		try {
			TransformerFactory factory = createTransformerFactory();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2"); //$NON-NLS-1$ //$NON-NLS-2$
			transformer.transform(source, result);
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			throw new IOException(e);		
		}
	}

	/**
	 * Saves the memento to the given file.
	 *
	 * @param filename java.lang.String
	 * @exception java.io.IOException
	 */
	@Override
	public void saveToFile(String filename) throws IOException {
		try (BufferedOutputStream w = new BufferedOutputStream(new FileOutputStream(filename))) {
			save(w);
		} catch (IOException e) {
			// ignore
		}

	}

	public String saveToString() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		save(out);
		return out.toString("UTF-8"); //$NON-NLS-1$
	}

	@Override
	public Boolean getBoolean(String key) {
		Attr attr = element.getAttributeNode(key);
		if (attr == null)
			return null;
		String strValue = attr.getValue();
		return Boolean.valueOf(strValue); //$NON-NLS-1$
	}

	@Override
	public void putBoolean(String key, boolean value) {
		element.setAttribute(key, Boolean.toString(value));
	}

	/**
	 * Returns the Text node of the memento. Each memento is allowed only one Text
	 * node.
	 *
	 * @return the Text node of the memento, or <code>null</code> if the memento has
	 *         no Text node.
	 */
	public Text getTextNode() {
		// Get the nodes.
		NodeList nodes = element.getChildNodes();
		int size = nodes.getLength();
		for (int nX = 0; nX < size; nX++) {
			Node node = nodes.item(nX);
			if (node instanceof Text) {
				return (Text) node;
			}
		}
		// a Text node was not found
		return null;
	}

	public void putTextData(String data) {
		Text textNode = getTextNode();
		if (textNode == null) {
			textNode = factory.createTextNode(data);
			// Always add the text node as the first child (fixes bug 93718)
			element.insertBefore(textNode, element.getFirstChild());
		} else {
			textNode.setData(data);
		}
	}
   
	public String getTextData() {
		Text textNode = getTextNode();
		if (textNode != null) {
			return textNode.getData();
		}
		return ""; //$NON-NLS-1$
	}

	private static DocumentBuilderFactory createDocumentBuilderFactory() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		} catch(IllegalArgumentException iae) {
			// Ignore this
		}
		try {
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		} catch(IllegalArgumentException iae) {
			// Ignore this
		}
		return factory;
	}

	private static TransformerFactory createTransformerFactory() {
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		return factory;
	}
}