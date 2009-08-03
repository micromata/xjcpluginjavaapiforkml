package de.micromata.javaapiforkml;

import java.util.ArrayList;

/**
 * Helper class. Only needed to save the obtained informations, while parsing the kml-reference.
 */
public class KMLReferenceField {
	private String name;

	private String nameClean;

	private ArrayList<String> syntax;

	private ArrayList<String> elementsSpecificTo;

	private ArrayList<String> description;

	private ArrayList<String> extend;

	private ArrayList<String> extendedBy;

	private ArrayList<String> contains;

	private ArrayList<String> example;

	private ArrayList<String> containedBy;

	private ArrayList<String> seealso;

	private boolean gxExtension = false;

	public KMLReferenceField() {
		this.syntax = new ArrayList<String>();
		this.description = new ArrayList<String>();
		this.elementsSpecificTo = new ArrayList<String>();
		this.example = new ArrayList<String>();
		this.extend = new ArrayList<String>();
		this.extendedBy = new ArrayList<String>();
		this.contains = new ArrayList<String>();
		this.containedBy = new ArrayList<String>();
		this.seealso = new ArrayList<String>();
	}

	public ArrayList<String> getSyntax() {
		return this.syntax;
	}

	public void addToSyntax(String syntax) {
		this.syntax.add(syntax);
	}

	public ArrayList<String> getElementsSpecificTo() {
		return this.elementsSpecificTo;
	}

	public void addToElementsSpecificTo(String elementsSpecificTo) {
		this.elementsSpecificTo.add(elementsSpecificTo);
	}

	public ArrayList<String> getDescription() {
		return this.description;
	}

	public void addToDescription(String description) {
		this.description.add(description);
	}

	public ArrayList<String> getExtend() {
		return this.extend;
	}

	public void addToExtend(String extend) {
		this.extend.add(extend);
	}

	public ArrayList<String> getExtendedBy() {
		return this.extendedBy;
	}

	public void addToExtendedBy(String extendedBy) {
		this.extendedBy.add(extendedBy);
	}

	public ArrayList<String> getContains() {
		return this.contains;
	}

	public void addToContains(String contains) {
		this.contains.add(contains);
	}

	public ArrayList<String> getExample() {
		return this.example;
	}

	public void addToExample(String example) {
		this.example.add(example);
	}

	public ArrayList<String> getContainedBy() {
		return this.containedBy;
	}

	public void addToContainedBy(String containedBy) {
		this.containedBy.add(containedBy);
	}

	public ArrayList<String> getSeealso() {
		return this.seealso;
	}

	public void addToSeealso(String seealso) {
		this.seealso.add(seealso);
	}

	public void setSyntax(ArrayList<String> syntax) {
		this.syntax = syntax;
	}

	public void setElementsSpecificTo(ArrayList<String> elementsSpecificTo) {
		this.elementsSpecificTo = elementsSpecificTo;
	}

	public void setDescription(ArrayList<String> description) {
		this.description = description;
	}

	public void setExtend(ArrayList<String> extend) {
		this.extend = extend;
	}

	public void setExtendedBy(ArrayList<String> extendedBy) {
		this.extendedBy = extendedBy;
	}

	public void setContains(ArrayList<String> contains) {
		this.contains = contains;
	}

	public void setExample(ArrayList<String> example) {
		this.example = example;
	}

	public void setContainedBy(ArrayList<String> containedBy) {
		this.containedBy = containedBy;
	}

	public void setSeealso(ArrayList<String> seealso) {
		this.seealso = seealso;
	}

	public void setGxExtension(boolean gxExtension) {
		this.gxExtension = gxExtension;
	}

	public boolean isGxExtension() {
		return this.gxExtension;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameClean() {
		return this.nameClean;
	}

	public void setNameClean(String nameClean) {
		this.nameClean = nameClean;
	}
}
