/*
 * Copyright (C) 2016 Johannes C. Schneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.neshanjo.rcswitch.server.background;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.glassfish.jersey.internal.util.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

import de.neshanjo.rcswitch.server.data.Configuration;
import de.neshanjo.rcswitch.server.data.SqsConfig;
import de.neshanjo.rcswitch.server.data.Switch;
import de.neshanjo.rcswitch.server.gpio.SwitchControl;

/**
 * @author Johannes Schneider
 */
public class SqsPollingWorker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SqsPollingWorker.class);

    private final SwitchControl switchControl;
    private final SqsConfig sqsConfig;
    private final AmazonSQS sqs;
    private final boolean active;

    public SqsPollingWorker(SwitchControl switchControl) {
        this.switchControl = switchControl;
        this.sqsConfig = Configuration.getInstance().getSqs();
        this.active = sqsConfig != null;
        if (!active) {
            sqs = null;
            return;
        }
        this.sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(sqsConfig.getAccessKey(), sqsConfig.getSecretKey())))
                .withRegion(Regions.EU_WEST_1).build();
    }

    @Override
    public void run() {

        if (!active) {
            LOG.info("No SQS polling config found. Polling disabled.");
            return;
        }

        LOG.info("Started SQS polling worker thread");
        long lastRun = System.currentTimeMillis();
        while (true) {
            pollAndHandleMessages();

            if (System.currentTimeMillis() - lastRun > 30_000) {
                LOG.debug("SQS polling worker still active");
                lastRun = System.currentTimeMillis();
            }
        }
    }

    void pollAndHandleMessages() {
        final ReceiveMessageResult messageResult;
        //TODO handle timeout (due to missing internet connection) here! Which exception is thrown then?
        messageResult = sqs.receiveMessage(new ReceiveMessageRequest()
                .withQueueUrl(sqsConfig.getQueueUrl())
                .withMessageAttributeNames("All")
                .withWaitTimeSeconds(20)
                .withMaxNumberOfMessages(1));


        final List<Message> messages = messageResult.getMessages();
        try {
            for (Message message : messages) {
                String switchName =
                        nullPointerSafe(() -> message.getMessageAttributes().get("switchName").getStringValue());
                String operation = nullPointerSafe(
                        () -> message.getMessageAttributes().get("position").getStringValue());

                final Optional<Switch> optionalSwitch
                        = Configuration.getInstance().getSwitches()
                        .stream()
                        .filter(sw -> sw.getName().equals(switchName))
                        .findFirst();
                if (!optionalSwitch.isPresent()) {
                    LOG.error("Could not find switch with name " + switchName);
                    continue;
                } else {
                    final Switch sw = optionalSwitch.get();
                    if (operation.toLowerCase().equals("on")) {
                        LOG.info("Turning on " + switchName);
                        switchControl.turnOn(sw.getGroup(), sw.getCode());
                    } else {
                        LOG.info("Turning off " + switchName);
                        switchControl.turnOff(sw.getGroup(), sw.getCode());
                    }
                }
            }
        } finally {
            if (!messages.isEmpty()) {
                try {
                    sqs.deleteMessageBatch(new DeleteMessageBatchRequest()
                            .withQueueUrl(sqsConfig.getQueueUrl())
                            .withEntries(messages
                                    .stream()
                                    .map(message -> new DeleteMessageBatchRequestEntry()
                                            .withId(UUID.randomUUID().toString())
                                            .withReceiptHandle(message.getReceiptHandle()))
                                    .collect(Collectors.toList())));
                } catch (Exception e) {
                    LOG.warn("Message deletion failed.");
                }
            }
        }
    }

    private String nullPointerSafe(Producer<String> producer) {
        try {
            return producer.call();
        } catch (NullPointerException ex) {
            return null;
        }
    }
}
