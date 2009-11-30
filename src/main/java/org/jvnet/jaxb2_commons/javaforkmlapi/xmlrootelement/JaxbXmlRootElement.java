package org.jvnet.jaxb2_commons.javaforkmlapi.xmlrootelement;

/**
 * represents a javadoc element, that could be added to a produced class by jaxb.
 */
public class JaxbXmlRootElement {
	private String lowerCaseClassName;

	private String nameInKmlFile;

	public JaxbXmlRootElement() {
	}

	public JaxbXmlRootElement(final String lowerCaseClassName, final String nameInKmlFile) {
		this.lowerCaseClassName = lowerCaseClassName;
		this.nameInKmlFile = nameInKmlFile;
	}


	public String getNameInKmlFile() {
		return nameInKmlFile;
	}

	public void setNameInKmlFile(final String nameInKmlFile) {
		this.nameInKmlFile = nameInKmlFile;
	}

	public void setLowerCaseClassName(final String lowerCaseClassName) {
	  this.lowerCaseClassName = lowerCaseClassName;
  }

	public String getLowerCaseClassName() {
	  return lowerCaseClassName;
  }

}
