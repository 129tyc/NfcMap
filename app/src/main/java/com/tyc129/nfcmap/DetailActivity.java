package com.tyc129.nfcmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tyc129.nfcmap.data.DisplayItem;
import com.tyc129.nfcmap.data.ResourceCenterImpl;

import java.util.List;
import java.util.Map;

/**
 * Created by Code on 2017/10/21 0021.
 *
 * @author 谈永成
 * @version 1.0
 */
public class DetailActivity extends AppCompatActivity {
    @BindView(R.id.image_view_detail_header)
    ImageView imageView;
    @BindView(R.id.text_view_detail_title)
    TextView title;
    @BindView(R.id.text_view_detail_content)
    TextView content;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_detail_card);
        ButterKnife.bind(this);
        String id = getIntent().getStringExtra("id");
        Map<String, DisplayItem> itemMap = ResourceCenterImpl
                .getInstance()
                .acquireItems(this);
        if (itemMap == null || id == null)
            return;
        if (itemMap.containsKey(id)) {
            DisplayItem item = itemMap.get(id);
            if (item != null) {
                imageView.setImageBitmap(item.getHeader());
                title.setText(item.getTag());
                content.setText(item.getContent());
            }
        }

    }
}
