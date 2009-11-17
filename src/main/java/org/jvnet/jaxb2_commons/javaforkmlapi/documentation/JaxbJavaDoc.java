package org.jvnet.jaxb2_commons.javaforkmlapi.documentation;

/**
 * represents a javadoc element, that could be added to a produced class by jaxb.
 */
public class JaxbJavaDoc {
	private String className;

	private String javaDoc;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getJavaDoc() {
		return javaDoc;
	}

	public void setJavaDoc(String javaDoc) {
		this.javaDoc = javaDoc;
	}

}
