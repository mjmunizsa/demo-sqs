package com.example.demosqs;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.example.demosqs.model.EventData;
import com.example.demosqs.model.SampleEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
@Slf4j
public class ControllerSQS {


    private final AmazonSQSAsync amazonSQSAsync;

    private final QueueMessagingTemplate queueMessagingTemplate;

    private final ObjectMapper mapper;


    public ControllerSQS(AmazonSQSAsync amazonSQSAsync, QueueMessagingTemplate queueMessagingTemplate, ObjectMapper mapper) {
        this.amazonSQSAsync = amazonSQSAsync;
        this.queueMessagingTemplate = queueMessagingTemplate;
        this.mapper = mapper;
    }


    @Value("${cloud.aws.sqs.outgoing-queue.url}")
    private String outgoingQueueUrl;



    @SqsListener(value = "${cloud.aws.sqs.incoming-queue.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    private void consumeFromSQS(SampleEvent sampleEvent) {
        log.info("Receive message {}", sampleEvent);
        //do some processing
        sampleEvent.setEventTime(ZonedDateTime.now());
        sampleEvent.getData().setEventType(EventData.EventType.PROCESSED);
//    amazonSQSAsync.sendMessage(outgoingQueueUrl, mapper.writeValueAsString(sampleEvent));
        queueMessagingTemplate.convertAndSend(outgoingQueueUrl, sampleEvent);
        log.info("Forwarded message {} to outgoing queue", sampleEvent);
    }

    @SqsListener(value = "${cloud.aws.sqs.outgoing-queue.url}", deletionPolicy = SqsMessageDeletionPolicy.NO_REDRIVE)
    private void eventStoreForOutgoing(SampleEvent sampleEvent) {
        log.info("Receive message {}", sampleEvent);
    }
}
