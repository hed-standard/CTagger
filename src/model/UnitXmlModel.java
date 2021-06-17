package model;

import javax.xml.bind.annotation.*;

/**
 * This class is an XML model corresponding to a <unit> in the <unitClasses> definition of the HED Schema
 *
 * @author Dung Truong
 */
@XmlType(propOrder = {}, name = "unit")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnitXmlModel {
    @XmlAttribute(name="SIUnit")
	private boolean sIUnit = false;
    @XmlAttribute
	private boolean unitSymbol = false;
    @XmlValue
	private String name; // name of the unit class, e.g. Time, PhysicalLength...

    public UnitXmlModel() {

	}
	public boolean isSIUnit() {
		return sIUnit;
	}
	public boolean isUnitSymbol() {
		return unitSymbol;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public void setsIUnit(boolean si) {this.sIUnit = si;}
	public void setUnitSymbol(boolean unitSymbol) {this.unitSymbol = unitSymbol;}

}
