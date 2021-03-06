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
package io.siddhi.core.query.selector.attribute.processor.executor;

import io.siddhi.core.config.SiddhiQueryContext;
import io.siddhi.core.executor.ExpressionExecutor;
import io.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
import io.siddhi.core.util.snapshot.Snapshotable;
import io.siddhi.query.api.definition.Attribute;

/**
 * Abstract class to represent attribute aggregations.
 */
public abstract class AbstractAggregationAttributeExecutor implements ExpressionExecutor, Snapshotable {
    protected AttributeAggregator attributeAggregator;
    protected ExpressionExecutor[] attributeExpressionExecutors;
    protected SiddhiQueryContext siddhiQueryContext;
    protected int size;
    private String elementId = null;

    public AbstractAggregationAttributeExecutor(AttributeAggregator attributeAggregator,
                                                ExpressionExecutor[] attributeExpressionExecutors,
                                                SiddhiQueryContext siddhiQueryContext) {
        this.siddhiQueryContext = siddhiQueryContext;
        this.attributeExpressionExecutors = attributeExpressionExecutors;
        this.attributeAggregator = attributeAggregator;
        this.size = attributeExpressionExecutors.length;
        if (elementId == null) {
            elementId = "AbstractAggregationAttributeExecutor-" +
                    this.siddhiQueryContext.getSiddhiAppContext().getElementIdGenerator().createNewId();
        }
        this.siddhiQueryContext.getSiddhiAppContext().getSnapshotService().addSnapshotable(
                siddhiQueryContext.getName(), this);
    }

    @Override
    public Attribute.Type getReturnType() {
        return attributeAggregator.getReturnType();
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public void clean() {
        for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
            expressionExecutor.clean();
        }
        attributeAggregator.clean();
        siddhiQueryContext.getSiddhiAppContext().getSnapshotService().removeSnapshotable(
                siddhiQueryContext.getName(), this);
    }
}

