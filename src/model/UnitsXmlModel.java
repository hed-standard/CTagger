package model;

import model.UnitXmlModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * This class is an XML model corresponding to a tag in the HED hierarchy.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
@XmlType(propOrder = {}, name = "units")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnitsXmlModel {

	private List<UnitXmlModel> unit;

	public void setUnits(List<UnitXmlModel> units) {
		this.unit = units;
	}

	public List<UnitXmlModel> getUnits() {
		return unit;
	}
}
