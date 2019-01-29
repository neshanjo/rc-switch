package de.neshanjo.rcswitch.server.background;

import org.junit.Ignore;
import org.junit.Test;

import de.neshanjo.rcswitch.server.gpio.DummySwitchControl;

@Ignore("Only for manual testing")
public class SqsPollingWorkerTest {

    @Test
    public void run() {
        final SqsPollingWorker pollingWorker = new SqsPollingWorker(new DummySwitchControl());

        //pollingWorker.pollAndHandleMessages();
        pollingWorker.run();
    }
}