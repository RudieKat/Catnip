package com.eveningoutpost.dexdrip.watch.thinjam;

import com.eveningoutpost.dexdrip.Models.JoH;
import com.eveningoutpost.dexdrip.Models.UserError;
import com.eveningoutpost.dexdrip.UtilityModels.Constants;
import com.eveningoutpost.dexdrip.UtilityModels.Inevitable;
import com.eveningoutpost.dexdrip.UtilityModels.Pref;
import com.eveningoutpost.dexdrip.utils.bt.ReplyProcessor;
import com.eveningoutpost.dexdrip.watch.thinjam.messages.BaseTx;
import com.eveningoutpost.dexdrip.watch.thinjam.messages.DefineWindowTx;

import java.util.concurrent.LinkedBlockingDeque;

import lombok.val;

import static com.eveningoutpost.dexdrip.Models.JoH.bytesToHex;
import static com.eveningoutpost.dexdrip.utils.validation.StringTools.isEmpty;
import static com.eveningoutpost.dexdrip.watch.thinjam.assets.AssetDownload.getAsset;

// jamorham

class BlueJayAsset {

    private static final String TAG = BlueJayAsset.class.getSimpleName();
    private static final LinkedBlockingDeque<Integer> assetQueue = new LinkedBlockingDeque<>();
    private static final boolean D = true;

    private static long lastAssetBegin = 0L;

    static boolean queueBusy() {
        return JoH.msSince(lastAssetBegin) < Constants.MINUTE_IN_MS * 5;
    }

    static void queueAssetRequest(int assetId) {
        synchronized (assetQueue) {
            if (!assetQueue.contains(assetId)) {
                UserError.Log.d(TAG, "Queuing asset request: " + assetId);
                assetQueue.add(assetId);
            } else {
                UserError.Log.d(TAG, "Duplicate asset request: " + assetId);
            }
        }
    }

    static boolean processAssetQueue(final BlueJayService service) {
        UserError.Log.d(TAG, "process asset queue called");
        synchronized (assetQueue) {
            val next = assetQueue.poll();
            if (next != null) {
                service.setPostQueueRunnable(() -> {
                    if (BlueJayAsset.processAssetQueue(service)) {
                        service.changeNextState();
                    }
                });
                new Thread(() -> processAssetRequest(service, next)).start();
                lastAssetBegin = JoH.tsl();
                return false;
            } else {
                UserError.Log.d(TAG, "Asset queue empty");
                return true;
            }
        }
    }

    private static void processAssetRequest(final BlueJayService service, final int assetid) {

        UserError.Log.d(TAG, "Processing: " + assetid);
        final byte[] bytes = getAssetBytes(assetid);
        if (bytes == null) {
            UserError.Log.d(TAG, "Cannot get asset bytes");
            Inevitable.task("try next asset", 1000, () -> BlueJayAsset.processAssetQueue(service));
            return;
        }

        int assetlength = bytes.length;

        final BaseTx packet = new DefineWindowTx((byte) 0x80, (byte) 0, (byte) (assetid >> 8), (byte) (assetlength >> 8), (byte) (assetid & 0xff), (byte) (assetlength & 0xff), (byte) 0, (byte) 0);
        val queueMe = service.queueGenericCommand(packet.getBytes(), "asset window: (assetid: " + assetid + " len: " + assetlength + ")", null, service.getARInstance(new ReplyProcessor(service.getI().connection) {
            @Override
            public void process(byte[] response) {
                if (D)
                    UserError.Log.d(TAG, "Wrote asset request request: " + bytesToHex(response));
                if (packet.responseOk(response)) {
                    UserError.Log.d(TAG, "Request success - sending bulk");
                    service.queueBufferForAssetStorage(assetid, bytes);
                    service.doThinJamQueue();
                } else {
                    UserError.Log.d(TAG, "Define Window failed: " + packet.responseText(response));
                }
            }
        }));

        UserError.Log.d(TAG, "Queued define window: " + queueMe.toString());
    }

    static byte[] getAssetForLocale(final int id, final String locale) {
        return getAsset(BlueJay.getMac() + "-" + JoH.tsl(), id, locale);
    }

    static byte[] getAssetBytes(int id) {
        val locale = Pref.getString("bluejay_asset_locale", "");
        byte[] assetBytes = getAssetForLocale(id, locale);
        if (assetBytes == null && !isEmpty(locale)) {
            assetBytes = getAssetForLocale(id, "");
        }
        if (assetBytes != null) {
            UserError.Log.d(TAG, "Got asset bytes: " + id + " " + assetBytes.length);
            return assetBytes;
        } else {
            UserError.Log.d(TAG, "Got null asset bytes: " + id);
        }
        return null;
    }
}
