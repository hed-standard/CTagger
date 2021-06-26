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
@XmlType(name = "schema")
@XmlAccessorType(XmlAccessType.FIELD)
public class SchemaXmlModel {

	@XmlElement
	private Set<TagXmlModel> node;

	public SchemaXmlModel() {
		node = new LinkedHashSet<TagXmlModel>();
	}

	public Set<TagXmlModel> getTags() {
		return node;
	}

	public void setTags(Set<TagXmlModel> tags) {
		this.node = tags;
	}

	@Override
	public String toString() {
		String s = "";
		for (TagXmlModel tagModel : node) {
			s += tagModel.toString() + "\n";
		}
		return s;
	}
}
