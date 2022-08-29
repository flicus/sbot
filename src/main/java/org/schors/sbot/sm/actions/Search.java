package org.schors.sbot.sm.actions;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.schors.sbot.services.FeedService;
import org.schors.sbot.services.FlibustaOPDSService;
import org.schors.telegram.sm.SMAction;
import org.schors.telegram.sm.SMActionBase;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@AllArgsConstructor
@Component
@SMAction(source = "textEntered", target = "search", event = "searchType")
public class Search extends SMActionBase {

    private FlibustaOPDSService flibusta;
    private FeedService feedService;

    @SneakyThrows
    @Override
    public void execute(StateContext<String, String> context) {
        context(context)
                .confirmCallback()
                .sendBusy("typing");

        Integer msgId = get("msgId", context);
        String msg = get("msg", context);
        String cmd = update(context).getCallbackQuery().getData();

        flibusta
                .searchOpds(msg, cmd)
                .flatMapMany(feed -> feedService.editMessage(msgId, feed, builder -> builder.update(chatId(context))))
                .doOnNext(botApiMethodMessage -> {
                    try {
                        bot(context).execute(botApiMethodMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                })
                .blockLast();
    }
}
