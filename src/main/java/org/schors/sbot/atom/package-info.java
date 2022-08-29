@XmlSchema(
        namespace = "http://www.w3.org/2005/Atom",
        elementFormDefault = XmlNsForm.QUALIFIED,
        xmlns = {
                @XmlNs(prefix = "dc", namespaceURI = "http://purl.org/dc/terms/"),
                @XmlNs(prefix = "os", namespaceURI = "http://a9.com/-/spec/opensearch/1.1/"),
                @XmlNs(prefix = "opds", namespaceURI = "http://opds-spec.org/2010/catalog"),
        }
)
package org.schors.sbot.atom;

import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;