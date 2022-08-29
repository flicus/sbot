package org.schors.sbot.atom;


import jakarta.xml.bind.annotation.XmlElement;

public class Author {
    private String name;
    private String uri;

    public Author() {
    }

    public String getName() {
        return name;
    }

    @XmlElement
    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    @XmlElement
    public void setUri(String uri) {
        this.uri = uri;
    }
}
