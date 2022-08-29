package org.schors.sbot;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.schors.sbot.xml.XmlDecoder;
import org.schors.sbot.xml.XmlEncoder;
import org.schors.telegram.sm.SMConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@Import(SMConfiguration.class)
public class SBotConfig {

    private static final String opdsV3 = "flibustahezeous3.onion";
    private static final String opdsV4 = "flibustahezeous3.onion";
    private static final String rootOPDShttp = "http://flibusta.is";
    private static final String authorSearch = "/search?searchType=authors&searchTerm=%s";
    private static final String bookSearch = "/search?searchType=books&searchTerm=%s";

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    @Value("${sbot.timeout:120000}")
    private int timeout;

    @Bean
    public WebClient webClientWithTimeout() {
        HttpClient httpClient = HttpClient.create()
                .proxy(proxy -> proxy.type(ProxyProvider.Proxy.SOCKS4).host("127.0.0.1").port(9050).connectTimeoutMillis(timeout))
                .responseTimeout(Duration.ofMillis(timeout))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS));
                })
                .compress(true);

        return WebClient.builder()
                .baseUrl("flibustaongezhld6dibs2dps6vm4nvqg2kp7vgowbu76tzopgnhazqd.onion")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> {
                    configurer.customCodecs().register(new XmlDecoder());
                    configurer.customCodecs().register(new XmlEncoder());
                })
                .build();
    }

    @Bean
    public Cache<String, String> urlCache() {
        return CacheBuilder.newBuilder().maximumSize(1000).build();
    }
}
