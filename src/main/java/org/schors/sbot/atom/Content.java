package org.schors.sbot.atom;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

public class Content {
    private String type;
    private String text;

    public Content() {
    }

    public String getType() {
        return type;
    }

    @XmlAttribute
    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    @XmlValue
    public void setText(String text) {
        this.text = text;
    }
}
