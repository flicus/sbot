package org.schors.sbot.sm.events;

import org.schors.telegram.sm.SMEventBase;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Predicate;

@Component
public class TextEvent extends SMEventBase {

    @Override
    public int order() {
        return 100;
    }

    @Override
    public String name() {
        return "text";
    }

    @Override
    public Predicate<Update> matcher() {
        return update ->
                update.hasMessage()
                        && update.getMessage().hasText();
    }
}
