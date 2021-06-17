package model;

import javax.xml.bind.annotation.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class is an XML model corresponding to an attribute in the HED hierarchy.
 *
 * @author Dung Truong, Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
@XmlType(name = "attribute")
@XmlAccessorType(XmlAccessType.FIELD)
public class AttributeXmlModel {

    // //////////////////
    // xml elements //
    // //////////////////

    @XmlElement
    private String name = "";
    @XmlElement
    private Set<String> value = new LinkedHashSet<String>();

    public String getName() {
        return name.toString();
    }

    public Set<String> getValues() {
        return value;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Set<String> values) {
        this.value = values;
    }

}
