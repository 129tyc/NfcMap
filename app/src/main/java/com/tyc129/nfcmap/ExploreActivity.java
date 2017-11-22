package com.tyc129.nfcmap;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tyc129.nfcmap.data.DisplayItem;
import com.tyc129.nfcmap.data.ResourceCenterImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Code on 2017/10/21 0021.
 *
 * @author 谈永成
 * @version 1.0
 */
public class ExploreActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.layout_collapsing)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.image_View_header)
    ImageView imageView;
    @BindView(R.id.list_landscape)
    RecyclerView recyclerView;

    private List<DisplayItem> items;
    private RecycleViewAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Slide());
        setContentView(R.layout.activity_explore);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        items = new ArrayList<>();
        Map<String, DisplayItem> itemMap = ResourceCenterImpl.getInstance().acquireItems(this);
        if (itemMap != null && items != null)
            for (Map.Entry<String, DisplayItem> e :
                    itemMap.entrySet()) {
                items.add(e.getValue());
            }
    }

    private void initView() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        if (collapsingToolbarLayout != null)
            collapsingToolbarLayout.setTitle("郑州大学");

        if (imageView != null)
            imageView.setImageBitmap(Utils
                    .readBitmapFitBound(this, R.drawable.map_detail_zzu,
                            500, 500));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        adapter = new RecycleViewAdapter();
        if (recyclerView != null) {
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(adapter);
            recyclerView.setHasFixedSize(true);
        }
    }

    private class RecycleViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater
                    .from(ExploreActivity.this)
                    .inflate(R.layout.item_landscape, null);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ItemViewHolder holder, final int position) {
            holder.title.setText(items.get(position).getTag());
            holder.content.setText(items.get(position).getContent());
            holder.imageItem.setImageBitmap(items.get(position).getHeader());
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(ExploreActivity.this, DetailActivity.class)
                                    .putExtra("id", items.get(position).getId()),
                            ActivityOptions
                                    .makeSceneTransitionAnimation(ExploreActivity.this,
                                            holder.headerPair)
                                    .toBundle());
                }
            });
            holder.open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(ExploreActivity.this, DetailActivity.class)
                                    .putExtra("id", items.get(position).getId()),
                            ActivityOptions
                                    .makeSceneTransitionAnimation(ExploreActivity.this,
                                            holder.headerPair)
                                    .toBundle());
                }
            });
            holder.share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
                    intent.putExtra(Intent.EXTRA_TEXT, items.get(position).getContent());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, items.get(position).getTag()));
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView imageItem;
        private TextView title;
        private TextView content;
        private Button open;
        private Button share;
        private Pair<View, String> headerPair;
//        private Pair<View, String> titlePair;
//        private Pair<View, String> contentPair;

        ItemViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_item);
            imageItem = (ImageView) itemView.findViewById(R.id.image_view_item);
            title = (TextView) itemView.findViewById(R.id.text_view_item_title);
            content = (TextView) itemView.findViewById(R.id.text_view_item_content);
            open = (Button) itemView.findViewById(R.id.btn_more);
            share = (Button) itemView.findViewById(R.id.btn_share);
            headerPair = new Pair<>((View) imageItem, imageItem.getTransitionName());
//            titlePair = new Pair<>((View) title, title.getTransitionName());
//            contentPair = new Pair<>((View) content, title.getTransitionName());
        }
    }
}
