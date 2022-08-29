package org.schors.sbot.sm.states;

import org.schors.telegram.sm.SMStateBase;
import org.springframework.stereotype.Component;

@Component
public class TextEnteredState extends SMStateBase {
    @Override
    public String getName() {
        return "textEntered";
    }
}
