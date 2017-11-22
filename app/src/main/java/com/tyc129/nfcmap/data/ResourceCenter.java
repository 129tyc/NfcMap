package com.tyc129.nfcmap.data;

import android.content.Context;
import com.tyc129.vectormap.MapFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by Code on 2017/10/23 0023.
 *
 * @author 谈永成
 * @version 1.0
 */
public interface ResourceCenter {

    MapFactory loadMapRecourse(MapFactory factory);

    Map<String, DisplayItem> acquireItems(Context context);
}
