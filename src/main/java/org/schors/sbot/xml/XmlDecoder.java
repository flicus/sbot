package org.schors.sbot.xml;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchema;
import jakarta.xml.bind.annotation.XmlType;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractDecoder;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.xml.XmlEventDecoder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.xml.StaxUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

public class XmlDecoder extends AbstractDecoder<Object> {
    private static final String JAXB_DEFAULT_ANNOTATION_VALUE = "##default";
    private static final XMLInputFactory inputFactory = StaxUtils.createDefensiveInputFactory();
    private final XmlEventDecoder xmlEventDecoder = new XmlEventDecoder();
    private final ContextContainer jaxbContexts = new ContextContainer();
    private Function<Unmarshaller, Unmarshaller> unmarshallerProcessor = Function.identity();
    private int maxInMemorySize = 262144;

    public XmlDecoder() {
        super(new MimeType[]{MimeTypeUtils.APPLICATION_XML, MimeTypeUtils.TEXT_XML, new MediaType("application", "*+xml")});
    }

    public XmlDecoder(MimeType... supportedMimeTypes) {
        super(supportedMimeTypes);
    }

    public void setUnmarshallerProcessor(Function<Unmarshaller, Unmarshaller> processor) {
        this.unmarshallerProcessor = this.unmarshallerProcessor.andThen(processor);
    }

    public Function<Unmarshaller, Unmarshaller> getUnmarshallerProcessor() {
        return this.unmarshallerProcessor;
    }

    public void setMaxInMemorySize(int byteCount) {
        this.maxInMemorySize = byteCount;
        this.xmlEventDecoder.setMaxInMemorySize(byteCount);
    }

    public int getMaxInMemorySize() {
        return this.maxInMemorySize;
    }

    public boolean canDecode(ResolvableType elementType, @Nullable MimeType mimeType) {
        Class<?> outputClass = elementType.toClass();
        return (outputClass.isAnnotationPresent(XmlRootElement.class) || outputClass.isAnnotationPresent(XmlType.class)) && super.canDecode(elementType, mimeType);
    }

    public Flux<Object> decode(Publisher<DataBuffer> inputStream, ResolvableType elementType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {
        Flux<XMLEvent> xmlEventFlux = this.xmlEventDecoder.decode(inputStream, ResolvableType.forClass(XMLEvent.class), mimeType, hints);
        Class<?> outputClass = elementType.toClass();
        QName typeName = this.toQName(outputClass);
        Flux<List<XMLEvent>> splitEvents = this.split(xmlEventFlux, typeName);
        return splitEvents.map((events) -> {
            Object value = this.unmarshal(events, outputClass);
            LogFormatUtils.traceDebug(this.logger, (traceOn) -> {
                String formatted = LogFormatUtils.formatValue(value, !traceOn);
                return Hints.getLogPrefix(hints) + "Decoded [" + formatted + "]";
            });
            return value;
        });
    }

    public Mono<Object> decodeToMono(Publisher<DataBuffer> input, ResolvableType elementType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {
        return DataBufferUtils.join(input, this.maxInMemorySize).map((dataBuffer) -> {
            return this.decode(dataBuffer, elementType, mimeType, hints);
        });
    }

    public Object decode(DataBuffer dataBuffer, ResolvableType targetType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) throws DecodingException {
        Object var7;
        try {
            Iterator eventReader = inputFactory.createXMLEventReader(dataBuffer.asInputStream(), encoding(mimeType));
            List<XMLEvent> events = new ArrayList();
            eventReader.forEachRemaining((event) -> {
                events.add((XMLEvent)event);
            });
            var7 = this.unmarshal(events, targetType.toClass());
        } catch (XMLStreamException var12) {
            throw new DecodingException(var12.getMessage(), var12);
        } catch (Throwable var13) {
            Throwable cause = var13.getCause();
            if (cause instanceof XMLStreamException) {
                throw new DecodingException(cause.getMessage(), cause);
            }

            throw Exceptions.propagate(var13);
        } finally {
            DataBufferUtils.release(dataBuffer);
        }

        return var7;
    }

    @Nullable
    private static String encoding(@Nullable MimeType mimeType) {
        if (mimeType == null) {
            return null;
        } else {
            Charset charset = mimeType.getCharset();
            return charset == null ? null : charset.name();
        }
    }

    private Object unmarshal(List<XMLEvent> events, Class<?> outputClass) {
        try {
            Unmarshaller unmarshaller = this.initUnmarshaller(outputClass);
            XMLEventReader eventReader = StaxUtils.createXMLEventReader(events);
            if (outputClass.isAnnotationPresent(XmlRootElement.class)) {
                return unmarshaller.unmarshal(eventReader);
            } else {
                JAXBElement<?> jaxbElement = unmarshaller.unmarshal(eventReader, outputClass);
                return jaxbElement.getValue();
            }
        } catch (UnmarshalException var6) {
            throw new DecodingException("Could not unmarshal XML to " + outputClass, var6);
        } catch (JAXBException var7) {
            throw new CodecException("Invalid JAXB configuration", var7);
        }
    }

    private Unmarshaller initUnmarshaller(Class<?> outputClass) throws CodecException, JAXBException {
        Unmarshaller unmarshaller = this.jaxbContexts.createUnmarshaller(outputClass);
        return (Unmarshaller)this.unmarshallerProcessor.apply(unmarshaller);
    }

    QName toQName(Class<?> outputClass) {
        String localPart;
        String namespaceUri;
        if (outputClass.isAnnotationPresent(XmlRootElement.class)) {
            XmlRootElement annotation = (XmlRootElement)outputClass.getAnnotation(XmlRootElement.class);
            localPart = annotation.name();
            namespaceUri = annotation.namespace();
        } else {
            if (!outputClass.isAnnotationPresent(XmlType.class)) {
                throw new IllegalArgumentException("Output class [" + outputClass.getName() + "] is neither annotated with @XmlRootElement nor @XmlType");
            }

            XmlType annotation = (XmlType)outputClass.getAnnotation(XmlType.class);
            localPart = annotation.name();
            namespaceUri = annotation.namespace();
        }

        if ("##default".equals(localPart)) {
            localPart = ClassUtils.getShortNameAsProperty(outputClass);
        }

        if ("##default".equals(namespaceUri)) {
            Package outputClassPackage = outputClass.getPackage();
            if (outputClassPackage != null && outputClassPackage.isAnnotationPresent(XmlSchema.class)) {
                XmlSchema annotation = (XmlSchema)outputClassPackage.getAnnotation(XmlSchema.class);
                namespaceUri = annotation.namespace();
            } else {
                namespaceUri = "";
            }
        }

        return new QName(namespaceUri, localPart);
    }

    Flux<List<XMLEvent>> split(Flux<XMLEvent> xmlEventFlux, QName desiredName) {
        return xmlEventFlux.handle(new XmlDecoder.SplitHandler(desiredName));
    }

    private static class SplitHandler implements BiConsumer<XMLEvent, SynchronousSink<List<XMLEvent>>> {
        private final QName desiredName;
        @Nullable
        private List<XMLEvent> events;
        private int elementDepth = 0;
        private int barrier = 2147483647;

        public SplitHandler(QName desiredName) {
            this.desiredName = desiredName;
        }

        public void accept(XMLEvent event, SynchronousSink<List<XMLEvent>> sink) {
            if (event.isStartElement()) {
                if (this.barrier == 2147483647) {
                    QName startElementName = event.asStartElement().getName();
                    if (this.desiredName.equals(startElementName)) {
                        this.events = new ArrayList();
                        this.barrier = this.elementDepth;
                    }
                }

                ++this.elementDepth;
            }

            if (this.elementDepth > this.barrier) {
                Assert.state(this.events != null, "No XMLEvent List");
                this.events.add(event);
            }

            if (event.isEndElement()) {
                --this.elementDepth;
                if (this.elementDepth == this.barrier) {
                    this.barrier = 2147483647;
                    Assert.state(this.events != null, "No XMLEvent List");
                    sink.next(this.events);
                }
            }

        }
    }
}
