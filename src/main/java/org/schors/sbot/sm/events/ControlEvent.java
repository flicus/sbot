package org.schors.sbot.sm.events;

import org.schors.telegram.sm.SMEventBase;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Predicate;

@Component
public class ControlEvent extends SMEventBase {

    @Override
    public int order() {
        return 100;
    }

    @Override
    public String name() {
        return "control";
    }

    @Override
    public Predicate<Update> matcher() {
        return update ->
                update.hasCallbackQuery();
    }
}
