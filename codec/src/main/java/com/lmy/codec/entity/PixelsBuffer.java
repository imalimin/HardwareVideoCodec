/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.entity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by lmyooyo@gmail.com on 2018/5/17.
 */

public class PixelsBuffer {
    public static PixelsBuffer allocate(int capacity) {
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.order(ByteOrder.nativeOrder());
        return wrap(buffer);
    }

    public static PixelsBuffer wrap(ByteBuffer buffer) {
        PixelsBuffer pixelsBuffer = new PixelsBuffer();
        pixelsBuffer.buffer = buffer;
        pixelsBuffer.valid();
        return pixelsBuffer;
    }

    private ByteBuffer buffer;
    private boolean invalid;

    private PixelsBuffer() {
    }

    @Override
    public String toString() {
        return "PixelsBuffer{" +
                "buffer=" + buffer +
                ", invalid=" + invalid +
                '}';
    }

    public ByteBuffer getBuffer() {
        position(0);
        return buffer;
    }

    public boolean isInvalid() {
        return invalid;
    }

    private void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public void valid() {
        setInvalid(false);
    }

    public void invalid() {
        setInvalid(true);
    }

    public void position(int position) {
        if (null != buffer) {
            buffer.position(position);
        }
    }

    public void clear() {
        if (null != buffer) {
            buffer.clear();
        }
    }
}
