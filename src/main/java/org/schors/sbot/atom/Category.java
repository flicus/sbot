package org.schors.sbot.atom;

import jakarta.xml.bind.annotation.XmlAttribute;

public class Category {
    private String term;
    private String label;

    public Category() {
    }

    public String getTerm() {
        return term;
    }

    @XmlAttribute
    public void setTerm(String term) {
        this.term = term;
    }

    public String getLabel() {
        return label;
    }

    @XmlAttribute
    public void setLabel(String label) {
        this.label = label;
    }
}
