package org.schors.sbot.atom;

import jakarta.xml.bind.annotation.XmlAttribute;

public class Link {
    private String href;
    private String rel;
    private String type;
    private String title;

    public Link() {
    }

    public String getHref() {
        return href;
    }

    @XmlAttribute
    public void setHref(String href) {
        this.href = href;
    }

    public String getRel() {
        return rel;
    }

    @XmlAttribute
    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getType() {
        return type;
    }

    @XmlAttribute
    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    @XmlAttribute
    public void setTitle(String title) {
        this.title = title;
    }
}
