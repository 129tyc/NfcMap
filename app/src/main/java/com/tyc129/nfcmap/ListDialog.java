package com.tyc129.nfcmap;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;

import java.util.List;

/**
 * Created by Code on 2017/10/26 0026.
 *
 * @author 谈永成
 * @version 1.0
 */
public class ListDialog extends Dialog {
    @BindView(R.id.list_location)
    ListView list;

    public interface SelectCallback {
        void onResult(String id);
    }

    private ListAdapter adapter;
    private Context context;
    private SelectCallback callback;
    private List<String> tags;
    private List<String> ids;

    public ListDialog(Context context) {
        super(context);
        this.context = context;
    }

    public ListDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    protected ListDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    public void setCallback(SelectCallback callback) {
        this.callback = callback;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListView();
    }

    private void initListView() {
        if (tags == null)
            return;
        adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, tags);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (ids != null && callback != null) {
                    callback.onResult(ids.get(i));
                }
            }
        });
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_list, null);
        this.setContentView(layout);
        ButterKnife.bind(this);
    }
}
