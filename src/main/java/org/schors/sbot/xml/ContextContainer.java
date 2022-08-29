package org.schors.sbot.xml;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.core.codec.CodecException;

final class ContextContainer {
    private final ConcurrentMap<Class<?>, JAXBContext> jaxbContexts = new ConcurrentHashMap(64);

    ContextContainer() {
    }

    public Marshaller createMarshaller(Class<?> clazz) throws CodecException, JAXBException {
        JAXBContext jaxbContext = this.getJaxbContext(clazz);
        return jaxbContext.createMarshaller();
    }

    public Unmarshaller createUnmarshaller(Class<?> clazz) throws CodecException, JAXBException {
        JAXBContext jaxbContext = this.getJaxbContext(clazz);
        return jaxbContext.createUnmarshaller();
    }

    private JAXBContext getJaxbContext(Class<?> clazz) throws CodecException {
        return (JAXBContext)this.jaxbContexts.computeIfAbsent(clazz, (key) -> {
            try {
                return JAXBContext.newInstance(new Class[]{clazz});
            } catch (JAXBException var3) {
                throw new CodecException("Could not create JAXBContext for class [" + clazz + "]: " + var3.getMessage(), var3);
            }
        });
    }
}
