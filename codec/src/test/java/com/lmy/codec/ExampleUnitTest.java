package com.lmy.codec;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void parserAACCSD0() throws Exception {
        byte[] data = new byte[2];
        data[0] = 0x12;
        data[1] = 0x10;
        System.out.println("type: " + getType(data));
        System.out.println("sample rate: " + getSampleRate(data));
    }

    private int getType(byte[] data) {
        return data[0] >> 3;
    }

    private int getSampleRate(byte[] data) {
        int h = data[0] & 0x7;
        int l = data[1] & 0x80 >> 7;
        return h << 1 | l;
    }
}