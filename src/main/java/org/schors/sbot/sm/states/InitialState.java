package org.schors.sbot.sm.states;

import org.schors.telegram.sm.SMStateBase;
import org.springframework.stereotype.Component;

@Component
public class InitialState extends SMStateBase {
    @Override
    public String getName() {
        return "initial";
    }

    @Override
    public boolean isInitial() {
        return true;
    }

}
