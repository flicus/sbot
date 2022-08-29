package org.schors.sbot.sm.events;

import org.schors.telegram.sm.SMEventBase;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Predicate;

@Component
public class SearchTypeEvent extends SMEventBase {

    @Override
    public String name() {
        return "searchType";
    }

    @Override
    public Predicate<Update> matcher() {
        return update ->
                update.hasCallbackQuery()
                && update.getCallbackQuery().getData() != null
                && (update.getCallbackQuery().getData().startsWith("authors")
                        || update.getCallbackQuery().getData().startsWith("books")
                );
    }
}
