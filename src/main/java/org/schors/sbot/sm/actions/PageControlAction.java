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
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@AllArgsConstructor
@SMAction(source = "search", target = "page", event = "control")
@SMAction(source = "page", target = "page", event = "control")
public class PageControlAction extends SMActionBase {

    private FlibustaOPDSService flibusta;
    private FeedService feedService;
    private Cache<String, String> urlCache;

    @SneakyThrows
    @Override
    public void execute(StateContext<String, String> context) {
        context(context)
                .confirmCallback()
                .sendBusy("typing");

        Integer msgId = get("msgId", context);
        String cmd = update(context).getCallbackQuery().getData();
        String url = urlCache.getIfPresent(cmd);

        flibusta.searchOpds(url)
                .flatMapMany(feed -> feedService.editMessage(msgId, feed, builder -> builder.update(chatId(context))))
                .doOnNext(botApiMethodSerializable -> {
                    try {
                        bot(context).execute(botApiMethodSerializable);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }).blockLast();

    }
}
