package org.schors.sbot.sm.events;

import org.schors.telegram.sm.SMEventBase;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Predicate;

public class DownloadEvent extends SMEventBase {
    @Override
    public String name() {
        return "download";
    }

    @Override
    public Predicate<Update> matcher() {
        return update -> update.hasMessage()
                && update.getMessage().hasText()
                && update.getMessage().getText().startsWith("/d");
    }
}
