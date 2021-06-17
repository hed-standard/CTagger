package model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is an XML model corresponding to a tag in the HED hierarchy.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
@XmlType(name = "node")
@XmlAccessorType(XmlAccessType.FIELD)
public class TagXmlModel {

	// //////////////////
	// xml elements //
	// //////////////////
	private Boolean extensionAllowed = false;
	@XmlElement
	private String name = "(new tag)";
	@XmlElement
	private String description = "";
	@XmlElement
	private List<AttributeXmlModel> attribute = new ArrayList<AttributeXmlModel>();
	@XmlElement
	private Set<TagXmlModel> node = new LinkedHashSet<TagXmlModel>();

	public void addChild(TagXmlModel child) {
		node.add(child);
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public Set<TagXmlModel> getTags() {
		return node;
	}

	public List<AttributeXmlModel> getAttribute() { return attribute; }

	public void setName(String name) {
		this.name = name;
	}
}
