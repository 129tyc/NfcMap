package com.tyc129.nfcmap.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.ArrayMap;
import com.tyc129.nfcmap.R;
import com.tyc129.nfcmap.Utils;
import com.tyc129.vectormap.MapFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Code on 2017/10/23 0023.
 *
 * @author 谈永成
 * @version 1.0
 */
public class ResourceCenterImpl implements ResourceCenter {
    private static ResourceCenter instance;
    private Map<String, DisplayItem> items;

    private ResourceCenterImpl() {
        items = null;
    }

    public static synchronized ResourceCenter getInstance() {
        if (instance == null)
            instance = new ResourceCenterImpl();
        return instance;
    }

    @Override
    public MapFactory loadMapRecourse(MapFactory factory) {
        if (factory == null)
            return null;
        factory.addBitmapRecourse(R.drawable.map_icon_landscape);
        factory.addBitmapRecourse(R.drawable.map_icon_building);
        factory.addBitmapRecourse(R.drawable.map_icon_toilet);
        factory.addBitmapRecourse(R.drawable.map_icon_restaurant);
        factory.addBitmapRecourse(R.drawable.map_icon_entrance);
        factory.addCoorRecourse(R.raw.map_data_zzu_coord);
        factory.addDrawSrcRecourse(R.raw.map_data_zzu_paints);
        factory.addMapRecourse(R.raw.map_data_zzu_i211f);
        factory.addMapRecourse(R.raw.map_data_zzu_i212f);
        factory.addMapRecourse(R.raw.map_data_zzu_i213f);
        factory.addMapRecourse(R.raw.map_data_zzu_i214f);
        factory.addMapRecourse(R.raw.map_data_zzu_i215f);
        factory.addMapRecourse(R.raw.map_data_zzu_lab_1);
        factory.addMapRecourse(R.raw.map_data_zzu_lab_2);
        factory.addMapRecourse(R.raw.map_data_zzu_lab_3);
        factory.addMapRecourse(R.raw.map_data_zzu_lab_4);
        factory.addMapRecourse(R.raw.map_data_zzu_lab_5);
        factory.addMapRecourse(R.raw.map_data_zzu_lab_6);
        factory.addMapRecourse(R.raw.map_data_zzu_main);
        factory.addTagRecourse(R.raw.map_data_zzu_tags);
        return factory;
    }

    @Override
    public Map<String, DisplayItem> acquireItems(Context context) {
        if (context == null)
            return null;
        if (items == null) {
            int width = 300;
            int height = 300;
            items = new ArrayMap<>();
            Bitmap item1 = Utils.readBitmapFitBound(context, R.drawable.map_detail_office, width, height);
            Bitmap item2 = Utils.readBitmapFitBound(context, R.drawable.map_detail_library, width, height);
            Bitmap item3 = Utils.readBitmapFitBound(context, R.drawable.map_detail_sport, width, height);
            Bitmap item4 = Utils.readBitmapFitBound(context, R.drawable.map_detail_tech, width, height);
            items.put("i2.1", new DisplayItem()
                    .setId("i2.1")
                    .setTag(context.getString(R.string.item_1_title))
                    .setContent(context.getString(R.string.item_1_content))
                    .setHeader(item1));
            items.put("i3.1", new DisplayItem()
                    .setId("i3.1")
                    .setTag(context.getString(R.string.item_2_title))
                    .setContent(context.getString(R.string.item_2_content))
                    .setHeader(item2));
            items.put("i1.5", new DisplayItem()
                    .setId("i1.5")
                    .setTag(context.getString(R.string.item_3_title))
                    .setContent(context.getString(R.string.item_3_content))
                    .setHeader(item3));
            items.put("i3.2", new DisplayItem()
                    .setId("i3.2")
                    .setTag(context.getString(R.string.item_4_title))
                    .setContent(context.getString(R.string.item_4_content))
                    .setHeader(item4));
        }
        return items;
    }
}
