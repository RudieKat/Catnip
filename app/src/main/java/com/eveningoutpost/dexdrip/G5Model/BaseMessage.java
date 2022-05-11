package com.eveningoutpost.dexdrip.G5Model;

import static com.eveningoutpost.dexdrip.G5Model.FastCRC16.check;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.eveningoutpost.dexdrip.Models.JoH;
import com.eveningoutpost.dexdrip.Services.G5CollectionService;
import com.google.gson.annotations.Expose;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

// jamorham

public class BaseMessage {

    protected static final String TAG = G5CollectionService.TAG; // meh
    static final int INVALID_TIME = 0xFFFFFFFF;
    @Expose
    long postExecuteGuardTime = 50;
    @Expose
    public volatile byte[] byteSequence;
    public final static ByteBuffer data = ByteBuffer.allocateDirect(2000).order(ByteOrder.LITTLE_ENDIAN);

    protected BaseMessage() {
        data.reset();
        data.rewind();
    }


    void init(final byte opcode, final int length) {
        data.put(opcode);
        if (length == 1) {
            getByteSequence();
        } else if (length == 3) {
            appendCRC();
        }
    }

    void appendCRC() {
        data.putShort(FastCRC16.calculate(data));
    }

    boolean checkCRC(ByteBuffer data) {
        if (data.position()>0) {
            return check(data);
        }
        return true;
    }

    byte[] getByteSequence() {
        byte[] sequence = new byte[data.position()];
        data.rewind();
        data.get(sequence,0,sequence.length);
        return sequence;
    }

    long guardTime() {
        return postExecuteGuardTime;
    }

    static long getUnsignedInt(ByteBuffer data) {
        return data.getInt();
    }

    static int getUnsignedShort(ByteBuffer data) {
        return Short.toUnsignedInt(data.getShort());
    }

    static int getUnsignedByte(ByteBuffer data) {
        return ((data.get() & 0xff));
    }

    static String dottedStringFromData(ByteBuffer data, int length) {

        final byte[] bytes = new byte[length];
        data.get(bytes);
        final StringBuilder sb = new StringBuilder(100);
        for (byte x : bytes) {
            if (sb.length() > 0) sb.append(".");
            JoH.bytesToHex(bytes);
            sb.append(String.format(Locale.US, "%d", (x & 0xff)));
        }
        return sb.toString();
    }

    static int getUnixTime() {
        return (int) (JoH.tsl() / 1000);
    }
}