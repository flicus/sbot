package org.schors.sbot;

import ch.qos.logback.classic.Logger;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.schors.sbot.atom.*;
import org.schors.sbot.xml.XmlDecoder;
import org.schors.sbot.xml.XmlEncoder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest(properties = "botEnabled=false")
class SbotApplicationTests {

    //flibustaongezhld6dibs2dps6vm4nvqg2kp7vgowbu76tzopgnhazqd.onion/opds/search?searchType=books&searchTerm=%D0%9E%D0%B1%D0%B8%D1%82%D0%B5%D0%BB%D1%8C
    //http://flibustahezeous3.onion/opds//search?searchType=authors&searchTerm=
    //http://flibustahezeous3.onion/search?searchType=books&searchTerm=

    @MockBean
    private StateMachine<String, String> stateMachine;

    @MockBean
    private StateMachinePersister persister;

//    @Test
    void optionTest() {
        Update update = new Update();
        Message msg = new Message();
        Chat chat = new Chat();
        chat.setId(Long.valueOf(123));
        msg.setChat(chat);
        update.setMessage(msg);

        Update update2 = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(msg);
        update2.setCallbackQuery(callbackQuery);


        Long chatId = Optional.ofNullable(update.getMessage())
                .map(message -> message.getChatId())
                .orElseGet(() -> update.getCallbackQuery().getMessage().getChatId());

        Long chatId2 = Optional.ofNullable(update2.getMessage())
                .map(message -> message.getChatId())
                .orElseGet(() -> update2.getCallbackQuery().getMessage().getChatId());

    }

//    @Test
    void contextLoads() {

        HttpClient httpClient = HttpClient.create()
                .proxy(proxy -> proxy.type(ProxyProvider.Proxy.SOCKS4).host("127.0.0.1").port(9050))
                .responseTimeout(Duration.ofMillis(120000))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120000)
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(120000, TimeUnit.MILLISECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(120000, TimeUnit.MILLISECONDS));
                })
                .compress(true);

        WebClient webClient = WebClient.builder()
                .baseUrl("flibustaongezhld6dibs2dps6vm4nvqg2kp7vgowbu76tzopgnhazqd.onion")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> {
                    configurer.customCodecs().register(new XmlDecoder());
                    configurer.customCodecs().register(new XmlEncoder());
                })
//                .codecs(clientCodecConfigurer -> {
//                    clientCodecConfigurer.customCodecs().register();
//                })
                .build();

//        org.springframework.http.codec.xml.Jaxb2XmlDecoder

        Feed res = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("opds", "search")
                        .queryParam("searchType", "books")
                        .queryParam("searchTerm", "Обитель")
                        .build())
                .accept(MediaType.APPLICATION_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchangeToMono(clientResponse -> {
                    clientResponse.headers();
                    return clientResponse.bodyToMono(Feed.class);
                })
                .block();
        log.debug(res.toString());

    }

//    @Test
    void serializationTest() {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(Feed.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Marshaller marshaller = jaxbContext.createMarshaller();
            Feed feed = new Feed();
            feed.setIcon("aaa");
            feed.setId("asdfdf");
            feed.setTitle("sdgsfgfdew");
            feed.setUpdated("sdfsdf");

            List<Link> links = new ArrayList<>();
            Link link1 = new Link();
            link1.setHref("adasd");
            link1.setRel("asdaf");
            link1.setTitle("afsdf");
            link1.setType("sdfdsf");
            links.add(link1);

            link1 = new Link();
            link1.setHref("gdfg");
            link1.setRel("gjhdgfg");
            link1.setTitle("fgbsdg");
            link1.setType("shhhh");
            links.add(link1);

            List<Entry> entries = new ArrayList<>();
            Entry entry = new Entry();

            entry.setFormat("sdfdf");
            entry.setId("rtge");
            entry.setIssued("sfgfg");
            entry.setUpdated("sdfsdf");
            Author author = new Author();
            author.setName("asfdf");
            author.setUri("asdfsdf");
            entry.setAuthor(author);

            Content content = new Content();
            content.setType("asd");
            entry.setContent(content);


            entries.add(entry);
            feed.setEntry(entries);

            Mono.just(feed)
                    .flatMapMany(feed1 -> Flux.just(
                                    Flux.just(feed1.getTitle()),
                                    Flux.fromStream(feed1.getEntry().stream()).map(Entry::getTitle))
                            .flatMap(stringFlux -> stringFlux))
                    .reduceWith(() -> new StringBuilder(), StringBuilder::append)
                    .subscribe(System.out::println);

            marshaller.marshal(feed, new File("test.xml"));
        } catch (JAXBException ex) {
            log.error("Error - ", ex);
        }
    }

}


