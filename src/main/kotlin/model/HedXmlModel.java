package model;

import javax.xml.bind.annotation.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class is an XML model corresponding to the HED hierarchy XML format.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
@XmlRootElement(name = "HED")
@XmlAccessorType(XmlAccessType.FIELD)
public class HedXmlModel {

	@XmlAttribute
	private String version;
	@XmlElement
	private SchemaXmlModel schema;
	@XmlElement
	private String epilogue;
	@XmlElement
	private UnitClassesXmlModel unitClasses;
	@XmlElement
	private UnitModifiersXmlModel unitModifiers;

	public HedXmlModel() {
		version = new String();
		schema = new SchemaXmlModel();
		epilogue = new String();
		unitClasses = new UnitClassesXmlModel();
		unitModifiers = new UnitModifiersXmlModel();
	}

	public Set<TagXmlModel> getTags() {
		return schema.getTags();
	}

	public String getVersion() {
		return version;
	}

	public UnitClassesXmlModel getUnitClasses() {
		return unitClasses;
	}

	public UnitModifiersXmlModel getUnitModifiers() {return unitModifiers;}

	public void setTags(Set<TagXmlModel> tags) {
		this.schema.setTags(tags);
	}

	public void setUnitClasses(UnitClassesXmlModel unitClasses) {
		this.unitClasses = unitClasses;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return schema.toString();
	}
}
