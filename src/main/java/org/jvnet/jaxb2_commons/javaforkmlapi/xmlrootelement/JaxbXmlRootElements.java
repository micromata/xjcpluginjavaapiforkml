package org.jvnet.jaxb2_commons.javaforkmlapi.xmlrootelement;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Helper class. Only needed by jaxb to marshall and unmarshall the map of javadoc-elements.
 */
@XmlRootElement
public class JaxbXmlRootElements {
	private List<JaxbXmlRootElement> elements;

	public JaxbXmlRootElements() {
		this.elements = new ArrayList<JaxbXmlRootElement>();
	}

	public JaxbXmlRootElements(final ArrayList<JaxbXmlRootElement> javadocElements) {
		this.elements = javadocElements;
	}

	public void setElements(final ArrayList<JaxbXmlRootElement> element) {
		this.elements = element;
	}

	public List<JaxbXmlRootElement> getElements() {
		return this.elements;
	}
}
