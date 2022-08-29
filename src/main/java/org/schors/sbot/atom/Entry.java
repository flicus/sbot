package org.schors.sbot.atom;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlNs;

import java.util.List;

public class Entry {
    private String updated;
    private String title;
    private Author author;
    private List<Link> link;
    private List<Category> category;
    private String language;
    private String format;
    private String issued;
    private Content content;
    private String id;

    public Entry() {
    }

    public String getUpdated() {
        return updated;
    }

    @XmlElement
    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getTitle() {
        return title;
    }

    @XmlElement
    public void setTitle(String title) {
        this.title = title;
    }

    public Author getAuthor() {
        return author;
    }

    @XmlElement
    public void setAuthor(Author author) {
        this.author = author;
    }

    public List<Link> getLink() {
        return link;
    }

    @XmlElement
    public void setLink(List<Link> link) {
        this.link = link;
    }

    public List<Category> getCategory() {
        return category;
    }

    @XmlElement
    public void setCategory(List<Category> category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    @XmlElement(namespace = "http://purl.org/dc/terms/")
    public void setLanguage(String language) {
        this.language = language;
    }

    public String getFormat() {
        return format;
    }

    @XmlElement(namespace = "http://purl.org/dc/terms/")
    public void setFormat(String format) {
        this.format = format;
    }

    public String getIssued() {
        return issued;
    }

    @XmlElement(namespace = "http://purl.org/dc/terms/")
    public void setIssued(String issued) {
        this.issued = issued;
    }

    public Content getContent() {
        return content;
    }

    @XmlElement
    public void setContent(Content content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    @XmlElement
    public void setId(String id) {
        this.id = id;
    }
}
