package model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * This class is an XML model corresponding to a tag in the HED hierarchy.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
@XmlType(name = "unitModifier")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnitModifierXmlModel {
    @XmlAttribute(name = "SIUnitModifier")
	private boolean sIUnitModifier = false;
	@XmlAttribute(name = "SIUnitSymbolModifier")
	private boolean sIUnitSymbolModifier = false;
	private String name = ""; // name of the unit modifier

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public boolean isSIUnitModifier() {
		return sIUnitModifier;
	}
	public boolean isSIUnitSymbolModifier() {
		return sIUnitSymbolModifier;
	}
}
