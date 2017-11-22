package com.tyc129.nfcmap;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tyc129.vectormap.NaviAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Code on 2017/10/24 0024.
 *
 * @author 谈永成
 * @version 1.0
 */
public class NaviDialog extends Dialog {

    @BindView(R.id.spinner_start)
    Spinner spinnerStart;
    @BindView(R.id.spinner_end)
    Spinner spinnerEnd;
    @BindView(R.id.spinner_type)
    Spinner spinnerType;
    @BindView(R.id.run_navi)
    Button buttonRunNavi;

    private Context context;
    private List<String> tags;
    private List<String> ids;
    private String sId;
    private String eId;
    private ResultCallback callback;
    private ArrayAdapter<CharSequence> typeAdapter;
    private ArrayAdapter<String> startAdapter;
    private ArrayAdapter<String> endAdapter;
    private NaviAnalyzer.SearchType type;


    public interface ResultCallback {
        void onResult(String sid, String eid, NaviAnalyzer.SearchType type);
    }

    public NaviDialog(Context context) {
        super(context);
        this.context = context;
    }

    public NaviDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    protected NaviDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    public void setCallback(ResultCallback callback) {
        this.callback = callback;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
        if (startAdapter != null) {
            startAdapter.clear();
            startAdapter.addAll(this.tags);
        }
        if (endAdapter != null) {
            endAdapter.clear();
            endAdapter.addAll(this.tags);
        }
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        if (tags == null || ids == null)
            return;
        initAdapter();
        spinnerStart.setAdapter(startAdapter);
        spinnerEnd.setAdapter(endAdapter);
        spinnerType.setAdapter(typeAdapter);
        initListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setOrgSelection();
    }

    public void setLocation(String sId, String eId) {
        this.sId = sId;
        this.eId = eId;
    }

    private void setOrgSelection() {
        int sPos = getPosition(sId);
        int ePos = getPosition(eId);
        if (sPos >= 0)
            spinnerStart.setSelection(sPos, true);
        else {
            spinnerStart.setSelection(0);
            sId = ids.get(0);
        }
        if (ePos >= 0)
            spinnerEnd.setSelection(ePos, true);
        else {
            spinnerEnd.setSelection(0);
            eId = ids.get(0);
        }

    }

    private int getPosition(String id) {
        if (id == null || ids == null)
            return -1;
        int i;
        for (i = 0; i < ids.size(); i++) {
            if (ids.get(i).equals(id))
                return i;
        }
        return -1;
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_navigation, null);
        this.setContentView(layout);
        ButterKnife.bind(NaviDialog.this);
    }

    private void initListener() {
        spinnerStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sId = ids.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                adapterView.setSelection(0);
            }
        });
        spinnerEnd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                eId = ids.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                adapterView.setSelection(0);
            }
        });
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String result = adapterView.getItemAtPosition(i).toString();
                type = parseNaviType(result);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                adapterView.setSelection(0);
            }
        });
        buttonRunNavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null && sId != null && eId != null && context != null)
                    if (sId.equals(eId)) {
                        Toast.makeText(context, "出发地与目的地不要相同", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        callback.onResult(sId, eId, type);
                        sId = null;
                        eId = null;
                        type = NaviAnalyzer.SearchType.NORMAL;
                    }

            }
        });
    }

    private void initAdapter() {
        typeAdapter = ArrayAdapter.createFromResource(context,
                R.array.string_array_spinner_type_navi, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
        startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
        endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startAdapter.addAll(tags);
        endAdapter.addAll(tags);
    }

    private NaviAnalyzer.SearchType parseNaviType(String str) {
        if (str != null) {
            switch (str) {
                case "驾车":
                    return NaviAnalyzer.SearchType.MOTOR;
                case "骑车":
                    return NaviAnalyzer.SearchType.NON_MOTOR;
                case "步行":
                    return NaviAnalyzer.SearchType.WALK;
                default:
                    return NaviAnalyzer.SearchType.NORMAL;
            }
        }
        return NaviAnalyzer.SearchType.NORMAL;
    }
}
