/*
 * Decompiled with CFR 0.152.
 */
package com.superasync.dto;

public enum Priority {
    CRITICAL(1),
    HIGH(3),
    NORMAL(5),
    LOW(7),
    BACKGROUND(10);

    private final int value;

    private Priority(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}

