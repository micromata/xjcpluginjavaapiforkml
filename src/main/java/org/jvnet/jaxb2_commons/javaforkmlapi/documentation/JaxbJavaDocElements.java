package org.jvnet.jaxb2_commons.javaforkmlapi.documentation;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Helper class. Only needed by jaxb to marshall and unmarshall the map of javadoc-elements.
 */
@XmlRootElement
public class JaxbJavaDocElements {
	private ArrayList<JaxbJavaDoc> elements;

	public JaxbJavaDocElements() {
		this.elements = new ArrayList<JaxbJavaDoc>();
	}

	public JaxbJavaDocElements(ArrayList<JaxbJavaDoc> javadocElements) {
		this.elements = javadocElements;
	}

	public void setElements(ArrayList<JaxbJavaDoc> element) {
		this.elements = element;
	}

	public ArrayList<JaxbJavaDoc> getElements() {
		return this.elements;
	}
}
