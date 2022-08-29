package org.schors.sbot.sm.actions;

import com.google.common.cache.Cache;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.schors.sbot.services.FeedService;
import org.schors.sbot.services.FlibustaOPDSService;
import org.schors.telegram.sm.SMAction;
import org.schors.telegram.sm.SMActionBase;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@AllArgsConstructor
@SMAction(source = "search", target = "page", event = "link")
@SMAction(source = "page", target = "page", event = "link")
public class PageLinkAction extends SMActionBase {

    private FlibustaOPDSService flibusta;
    private FeedService feedService;
    private Cache<String, String> urlCache;

    @SneakyThrows
    @Override
    public void execute(StateContext<String, String> context) {
        context(context)
                .sendBusy("typing");

        Integer msgId = get("msgId", context);
        String cmd = update(context).getMessage().getText();
        String url = urlCache.getIfPresent(cmd.substring(2));

        flibusta.searchOpds(url)
                .flatMapMany(feed -> feedService.editMessage(msgId, feed, updater -> updater.update(chatId(context))))
                .doOnNext(botApiMethodSerializable -> {
                    try {
                        bot(context).executeAsync(botApiMethodSerializable);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                })
                .blockLast();
    }
}
