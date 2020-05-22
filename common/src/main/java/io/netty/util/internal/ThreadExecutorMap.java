/*
 * Copyright 2019 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.internal;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.FastThreadLocal;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * Allow to retrieve the {@link EventExecutor} for the calling {@link Thread}.
 */
public final class ThreadExecutorMap {
    //保存所有的EventExecutor的映射关系
    private static final FastThreadLocal<EventExecutor> mappings = new FastThreadLocal<EventExecutor>();

    private ThreadExecutorMap() { }

    /**
     * Returns the current {@link EventExecutor} that uses the {@link Thread}, or {@code null} if none / unknown.
     */
    public static EventExecutor currentExecutor() {
        return mappings.get();
    }

    /**
     * Set the current {@link EventExecutor} that is used by the {@link Thread}.
     */
    private static void setCurrentEventExecutor(EventExecutor executor) {
        mappings.set(executor);
    }

    /**
     * Decorate the given {@link Executor} and ensure {@link #currentExecutor()} will return {@code eventExecutor}
     * when called from within the {@link Runnable} during execution.
     */
    public static Executor apply(final Executor executor, final EventExecutor eventExecutor) {
        //executor为MultithreadEventExecutorGroup中默认定义或或者是new时传入的，作用就是开启一个线程
        //并且对把Runnable封装了的
        ObjectUtil.checkNotNull(executor, "executor");
        ObjectUtil.checkNotNull(eventExecutor, "eventExecutor");
        return new Executor() {
            @Override
            public void execute(final Runnable command) {
                executor.execute(apply(command, eventExecutor));
            }
        };
    }

    /**
     * Decorate the given {@link Runnable} and ensure {@link #currentExecutor()} will return {@code eventExecutor}
     * when called from within the {@link Runnable} during execution.
     */
    public static Runnable apply(final Runnable command, final EventExecutor eventExecutor) {
        ObjectUtil.checkNotNull(command, "command");
        ObjectUtil.checkNotNull(eventExecutor, "eventExecutor");
        return new Runnable() {
            @Override
            public void run() {
                //设置ThreadLocal
                //以EventExecutor为key的map
                //并不是真正的ThreadLocal
                setCurrentEventExecutor(eventExecutor);
                try {
                    command.run();
                } finally {
                    setCurrentEventExecutor(null);
                }
            }
        };
    }

    /**
     * Decorate the given {@link ThreadFactory} and ensure {@link #currentExecutor()} will return {@code eventExecutor}
     * when called from within the {@link Runnable} during execution.
     */
    public static ThreadFactory apply(final ThreadFactory threadFactory, final EventExecutor eventExecutor) {
        ObjectUtil.checkNotNull(threadFactory, "command");
        ObjectUtil.checkNotNull(eventExecutor, "eventExecutor");
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return threadFactory.newThread(apply(r, eventExecutor));
            }
        };
    }
}
