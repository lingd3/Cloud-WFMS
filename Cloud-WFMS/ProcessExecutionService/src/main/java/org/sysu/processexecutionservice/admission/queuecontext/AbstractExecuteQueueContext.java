package org.sysu.processexecutionservice.admission.queuecontext;

import org.sysu.processexecutionservice.admission.requestcontext.IRequestContext;

import java.util.Queue;

public abstract class AbstractExecuteQueueContext implements IQueueContext {
    protected Queue<IRequestContext> executeQueue;

    public Queue<IRequestContext> getExecuteQueue() {
        return executeQueue;
    }

    public void setExecuteQueue(Queue<IRequestContext> executeQueue) {
        this.executeQueue = executeQueue;
    }
}
