package com.eveningoutpost.dexdrip.tidepool;

// jamorham

import com.eveningoutpost.dexdrip.Models.JoH;
import com.eveningoutpost.dexdrip.UtilityModels.StatusItem;
import com.eveningoutpost.dexdrip.store.FastStore;
import com.eveningoutpost.dexdrip.store.KeyStore;

import java.util.ArrayList;
import java.util.List;

import static com.eveningoutpost.dexdrip.Models.JoH.msSince;
import static com.eveningoutpost.dexdrip.Models.JoH.niceTimeScalar;

public class TidepoolStatus {

    // data for MegaStatus
    public static List<StatusItem> megaStatus() {

        final KeyStore keyStore = FastStore.getInstance();
        final List<StatusItem> l = new ArrayList<>();

        l.add(new StatusItem("Tidepool Synced to", niceTimeScalar(msSince(UploadChunk.getLastEnd())) + " ago")); // TODO needs generic message format string
        final String status = keyStore.getS(TidepoolUploader.STATUS_KEY);
        if (!isEmpty(status)) {
            l.add(new StatusItem("Tidepool Status", status));
        }
        return l;
    }
}
