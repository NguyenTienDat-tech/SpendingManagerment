package com.example.spendingmanagerment;

import com.example.spendingmanagerment.core.base.UiEvent;
import org.junit.Test;
import static org.junit.Assert.*;

public class UiEventTest {
    @Test public void reattachingObserverDoesNotRepeatNavigationEffect() {
        UiEvent<String> event = new UiEvent<>("open_editor");
        assertEquals("open_editor", event.consume());
        assertNull(event.consume());
    }
}
