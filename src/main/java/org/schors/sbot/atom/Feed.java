package org.schors.sbot.atom;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement
public class Feed {
    private String id;
    private String title;
    private String updated;
    private String icon;
    private List<Link> link;
    private List<Entry> entry;

    public Feed() {
    }

    public String getId() {
        return id;
    }

    @XmlElement
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    @XmlElement
    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdated() {
        return updated;
    }

    @XmlElement
    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getIcon() {
        return icon;
    }

    @XmlElement
    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<Link> getLink() {
        return link;
    }

    @XmlElement
    public void setLink(List<Link> link) {
        this.link = link;
    }

    public List<Entry> getEntry() {
        return entry;
    }

    @XmlElement
    public void setEntry(List<Entry> entry) {
        this.entry = entry;
    }
}
