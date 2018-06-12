package com.abupdate.sota.engine.thread;

import com.abupdate.sota.network.RequestStack;
import com.abupdate.sota.utils.Utils;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public abstract class NamedRunnable implements Runnable {
    protected final String name;

    public NamedRunnable(String format, Object... args) {
        this.name = Utils.format(format, args);
    }

    @Override public final void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(name);
        try {
            execute();
        } finally {
            Thread.currentThread().setName(oldName);
            RequestStack.getInstance().getDispatcher().finished(this);
        }
    }

    protected abstract void execute();
}