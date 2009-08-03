package org.jvnet.jaxb2_commons.javaforkmlapi.xmlrootelement;

/**
 * represents a javadoc element, that could be added to a produced class by jaxb.
 */
public class JaxbXmlRootElement {
	private String lowerCaseClassName;

	private String nameInKmlFile;

	public JaxbXmlRootElement() {
	}

	public JaxbXmlRootElement(String lowerCaseClassName, String nameInKmlFile) {
		this.lowerCaseClassName = lowerCaseClassName;
		this.nameInKmlFile = nameInKmlFile;
	}


	public String getNameInKmlFile() {
		return nameInKmlFile;
	}

	public void setNameInKmlFile(String nameInKmlFile) {
		this.nameInKmlFile = nameInKmlFile;
	}

	public void setLowerCaseClassName(String lowerCaseClassName) {
	  this.lowerCaseClassName = lowerCaseClassName;
  }

	public String getLowerCaseClassName() {
	  return lowerCaseClassName;
  }

}
