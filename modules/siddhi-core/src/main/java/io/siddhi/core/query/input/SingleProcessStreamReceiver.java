/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.core.query.input;

import io.siddhi.core.config.SiddhiQueryContext;
import io.siddhi.core.event.ComplexEventChunk;
import io.siddhi.core.event.state.StateEvent;
import io.siddhi.core.event.stream.StreamEvent;
import io.siddhi.core.query.input.stream.state.StreamPreStateProcessor;
import io.siddhi.core.query.processor.Processor;
import io.siddhi.core.query.selector.QuerySelector;

/**
 * Implementation of {StreamJunction.Receiver} to receive events to be fed into
 * single stream stateless queries(Filter).
 */
public class SingleProcessStreamReceiver extends ProcessStreamReceiver {

    protected final String lockKey;
    protected ComplexEventChunk<StreamEvent> currentStreamEventChunk = new ComplexEventChunk<StreamEvent>
            (batchProcessingAllowed);
    private QuerySelector querySelector;

    public SingleProcessStreamReceiver(String streamId, String lockKey, SiddhiQueryContext siddhiQueryContext) {
        super(streamId, siddhiQueryContext);
        this.lockKey = lockKey;
    }

    public void setNext(Processor next) {
        super.setNext(next);
        this.querySelector = (QuerySelector) ((StreamPreStateProcessor) next).getThisLastProcessor().getNextProcessor();
    }

    public SingleProcessStreamReceiver clone(String key) {
        return new SingleProcessStreamReceiver(streamId + key, key,
                siddhiQueryContext);
    }

    protected void processAndClear(ComplexEventChunk<StreamEvent> streamEventChunk) {
        ComplexEventChunk<StateEvent> retEventChunk = new ComplexEventChunk<StateEvent>(false);
        synchronized (lockKey) {
            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = streamEventChunk.next();
                streamEventChunk.remove();
                stabilizeStates();
                currentStreamEventChunk.add(streamEvent);
                ComplexEventChunk<StateEvent> eventChunk = ((StreamPreStateProcessor) next).processAndReturn
                        (currentStreamEventChunk);
                if (eventChunk.getFirst() != null) {
                    retEventChunk.add(eventChunk.getFirst());
                }

                eventChunk.clear();
                currentStreamEventChunk.clear();
            }
        }
        while (retEventChunk.hasNext()) {
            StateEvent stateEvent = retEventChunk.next();
            retEventChunk.remove();
            querySelector.process(new ComplexEventChunk<StateEvent>(stateEvent, stateEvent, false));
        }
    }

    protected void stabilizeStates() {

    }
}
