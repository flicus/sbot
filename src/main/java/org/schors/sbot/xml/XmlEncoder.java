package org.schors.sbot.xml;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.MarshalException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.schors.sbot.xml.ContextContainer;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractSingleValueEncoder;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.EncodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class XmlEncoder extends AbstractSingleValueEncoder<Object> {
    private final ContextContainer jaxbContexts = new ContextContainer();
    private Function<Marshaller, Marshaller> marshallerProcessor = Function.identity();

    public XmlEncoder() {
        super(new MimeType[]{MimeTypeUtils.APPLICATION_XML, MimeTypeUtils.TEXT_XML, new MediaType("application", "*+xml")});
    }

    public void setMarshallerProcessor(Function<Marshaller, Marshaller> processor) {
        this.marshallerProcessor = this.marshallerProcessor.andThen(processor);
    }

    public Function<Marshaller, Marshaller> getMarshallerProcessor() {
        return this.marshallerProcessor;
    }

    public boolean canEncode(ResolvableType elementType, @Nullable MimeType mimeType) {
        if (!super.canEncode(elementType, mimeType)) {
            return false;
        } else {
            Class<?> outputClass = elementType.toClass();
            return outputClass.isAnnotationPresent(XmlRootElement.class) || outputClass.isAnnotationPresent(XmlType.class);
        }
    }

    protected Flux<DataBuffer> encode(Object value, DataBufferFactory bufferFactory, ResolvableType valueType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {
        return Mono.fromCallable(() -> {
            return this.encodeValue(value, bufferFactory, valueType, mimeType, hints);
        }).flux();
    }

    public DataBuffer encodeValue(Object value, DataBufferFactory bufferFactory, ResolvableType valueType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {
        if (!Hints.isLoggingSuppressed(hints)) {
            LogFormatUtils.traceDebug(this.logger, (traceOn) -> {
                String formatted = LogFormatUtils.formatValue(value, !traceOn);
                return Hints.getLogPrefix(hints) + "Encoding [" + formatted + "]";
            });
        }

        boolean release = true;
        DataBuffer buffer = bufferFactory.allocateBuffer(1024);

        DataBuffer var11;
        try {
            OutputStream outputStream = buffer.asOutputStream();
            Class<?> clazz = ClassUtils.getUserClass(value);
            Marshaller marshaller = this.initMarshaller(clazz);
            marshaller.marshal(value, outputStream);
            release = false;
            var11 = buffer;
        } catch (MarshalException var16) {
            throw new EncodingException("Could not marshal " + value.getClass() + " to XML", var16);
        } catch (JAXBException var17) {
            throw new CodecException("Invalid JAXB configuration", var17);
        } finally {
            if (release) {
                DataBufferUtils.release(buffer);
            }

        }

        return var11;
    }

    private Marshaller initMarshaller(Class<?> clazz) throws CodecException, JAXBException {
        Marshaller marshaller = this.jaxbContexts.createMarshaller(clazz);
        marshaller.setProperty("jaxb.encoding", StandardCharsets.UTF_8.name());
        marshaller = (Marshaller)this.marshallerProcessor.apply(marshaller);
        return marshaller;
    }
}
