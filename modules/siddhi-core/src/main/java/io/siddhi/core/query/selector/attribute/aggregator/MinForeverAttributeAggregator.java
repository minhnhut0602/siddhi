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
package io.siddhi.core.query.selector.attribute.aggregator;

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.ReturnAttribute;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiQueryContext;
import io.siddhi.core.exception.OperationNotSupportedException;
import io.siddhi.core.executor.ExpressionExecutor;
import io.siddhi.core.query.processor.ProcessingMode;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.query.api.definition.Attribute;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link AttributeAggregator} to calculate min value for life time based on an event attribute.
 */
@Extension(
        name = "minForever",
        namespace = "",
        description = "This is the attribute aggregator to store the minimum value for a given attribute throughout " +
                "the lifetime of the query regardless of any windows in-front.",
        parameters = {
                @Parameter(name = "arg",
                        description = "The value that needs to be compared to find the minimum value.",
                        type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT})
        },
        returnAttributes = @ReturnAttribute(
                description = "Returns the minimum value in the same data type as the input.",
                type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT}),
        examples = @Example(
                syntax = "from inputStream\n" +
                        "select minForever(temp) as max\n" +
                        "insert into outputStream;",
                description = "minForever(temp) returns the minimum temp value recorded for all the events throughout" +
                        "the lifetime of the query."
        )
)
public class MinForeverAttributeAggregator extends AttributeAggregator {

    private MinForeverAttributeAggregator minForeverAttributeAggregator;

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param processingMode               query processing mode
     * @param outputExpectsExpiredEvents   is expired events sent as output
     * @param configReader                 this hold the {@link MinForeverAttributeAggregator} configuration reader.
     * @param siddhiQueryContext           Siddhi query runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ProcessingMode processingMode,
                        boolean outputExpectsExpiredEvents, ConfigReader configReader,
                        SiddhiQueryContext siddhiQueryContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new OperationNotSupportedException("MinForever aggregator has to have exactly 1 parameter, " +
                    "currently " + attributeExpressionExecutors.length + " parameters provided");
        }
        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
        switch (type) {
            case FLOAT:
                minForeverAttributeAggregator = new MinForeverAttributeAggregatorFloat();
                break;
            case INT:
                minForeverAttributeAggregator = new MinForeverAttributeAggregatorInt();
                break;
            case LONG:
                minForeverAttributeAggregator = new MinForeverAttributeAggregatorLong();
                break;
            case DOUBLE:
                minForeverAttributeAggregator = new MinForeverAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("MinForever not supported for " + type);
        }
    }

    public Attribute.Type getReturnType() {
        return minForeverAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object data) {
        if (data == null) {
            return minForeverAttributeAggregator.currentValue();
        }
        return minForeverAttributeAggregator.processAdd(data);
    }

    @Override
    public Object processAdd(Object[] data) {
        // will not occur
        return new IllegalStateException("MinForever cannot process data array, but found " +
                Arrays.deepToString(data));
    }

    @Override
    public Object processRemove(Object data) {
        if (data == null) {
            return minForeverAttributeAggregator.currentValue();
        }
        return minForeverAttributeAggregator.processRemove(data);
    }

    @Override
    public Object processRemove(Object[] data) {
        // will not occur
        return new IllegalStateException("MinForever cannot process data array, but found " +
                Arrays.deepToString(data));
    }

    protected Object currentValue() {
        return null;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public Object reset() {
        return minForeverAttributeAggregator.reset();
    }

    @Override
    public Map<String, Object> currentState() {
        return minForeverAttributeAggregator.currentState();
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        minForeverAttributeAggregator.restoreState(state);
    }

    class MinForeverAttributeAggregatorDouble extends MinForeverAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private volatile Double minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Double value = (Double) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            Double value = (Double) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Object reset() {
            return minValue;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("MinValue", minValue);
            return state;
        }

        @Override
        public void restoreState(Map<String, Object> state) {
            minValue = (Double) state.get("MinValue");
        }

        protected Object currentValue() {
            return minValue;
        }

    }

    class MinForeverAttributeAggregatorFloat extends MinForeverAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.FLOAT;
        private volatile Float minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Float value = (Float) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            Float value = (Float) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Object reset() {
            return minValue;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("MinValue", minValue);
            return state;
        }

        @Override
        public void restoreState(Map<String, Object> state) {
            minValue = (Float) state.get("MinValue");
        }

        protected Object currentValue() {
            return minValue;
        }
    }

    class MinForeverAttributeAggregatorInt extends MinForeverAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.INT;
        private volatile Integer minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Integer value = (Integer) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Object reset() {
            return minValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            Integer value = (Integer) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("MinValue", minValue);
            return state;
        }

        @Override
        public void restoreState(Map<String, Object> state) {
            minValue = (Integer) state.get("MinValue");
        }

        protected Object currentValue() {
            return minValue;
        }
    }

    class MinForeverAttributeAggregatorLong extends MinForeverAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.LONG;
        private volatile Long minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Long value = (Long) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Object reset() {
            return minValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            Long value = (Long) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("MinValue", minValue);
            return state;
        }

        @Override
        public void restoreState(Map<String, Object> state) {
            minValue = (Long) state.get("MinValue");
        }

        protected Object currentValue() {
            return minValue;
        }
    }

}
