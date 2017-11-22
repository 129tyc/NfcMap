package com.tyc129.nfcmap;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.ArrayMap;
import android.util.Log;
import android.view.*;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tyc129.nfcmap.data.DisplayItem;
import com.tyc129.nfcmap.data.ResourceCenterImpl;
import com.tyc129.nfcwrapper.NfcActivity;
import com.tyc129.nfcwrapper.NfcCommander;
import com.tyc129.nfcwrapper.NfcWrapListener;
import com.tyc129.nfcwrapper.TagInfo;
import com.tyc129.vectormap.MapFactory;
import com.tyc129.vectormap.NaviAnalyzer;
import com.tyc129.vectormap.VectorMap;
import com.tyc129.vectormap.struct.Interest;
import com.tyc129.vectormap.struct.MapSrc;
import com.tyc129.vectormap.struct.Path;
import com.tyc129.vectormap.view.MapRender;
import com.tyc129.vectormap.view.RenderUnit;
import com.tyc129.vectormap.view.VectorMapView;

import java.util.*;

import static com.tyc129.nfcmap.ScanQRActivity.NAME_INTENT_EXTRA;

/**
 * Created by Code on 2017/10/21 0021.
 *
 * @author 谈永成
 * @version 1.0
 */
public class MainActivity extends NfcActivity {
    @BindView(R.id.button_list)
    Button openList;
    @BindView(R.id.button_navi)
    Button openNavi;
    @BindView(R.id.button_explore)
    Button openExplore;
    @BindView(R.id.button_locate)
    ImageButton locate;
    @BindView(R.id.btn_qr_code)
    ImageButton scan;
    @BindView(R.id.vector_map_main)
    VectorMapView mainView;
    @BindView(R.id.image_view_decoration)
    ImageView decoration;
    @BindView(R.id.image_view_reset)
    ImageView reset;
    @BindView(R.id.view_root_main)
    ViewGroup rootView;
    @BindView(R.id.list_view_inner)
    ListView innerList;

    @BindView(R.id.card_popup)
    CardView popupCard;
    @BindView(R.id.image_view_popup)
    ImageView popupHeader;
    @BindView(R.id.text_view_popup_title)
    TextView popupTitle;
    @BindView(R.id.btn_inner)
    Button popupInner;
    @BindView(R.id.btn_more)
    Button popupMore;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int DURATION_ANIMATION = 500;
    private static final int REQUEST_CODE_SCAN_QR = 0;
    private static final long DELAY_FINISH = 2000;
    private boolean supportNfc;
    private boolean hiddenUI;
    private boolean isNavigating;
    private boolean allowFinish;
    private int innerSelect;
    private Bitmap arrowIcon;
    private Bitmap holderIcon;
    private NfcCommander commander;
    private VectorMap currMap;
    private MapFactory factory;
    private NaviAnalyzer analyzer;
    private MapRender render;
    private VectorMapView.CompassStyle compassStyle;
    private String locationId;
    private float[] locationPos;
    private float[] selectionPos;
    private String selectId;
    private List<String> mapNames;
    private List<MapSrc> maps;
    private MapSrc currMapSrc;
    private List<List<RenderUnit>> mapsUnits;
    private Map<String, String> tagIds;
    private List<String> tags;
    private List<String> ids;
    private NaviDialog naviDialog;
    private ListDialog listDialog;
    private ArrayAdapter<String> adapter;
    private Map<String, DisplayItem> popupInfoMap;
    private GestureDetector gestureDetector;
    private String popupId;
    private FinishHandler finishHandler;


    @Override
    protected NfcWrapListener onSetNfcWrapListener() {
        return new WrapListener();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initVariables();
        initVectorMap();
        initPopupInfo();
        initListener();
        initNfc();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLocating();
        if (supportNfc || commander != null)
            commander.acquireReadTag();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mainView != null)
            mainView.mapRefresh();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocating();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE_SCAN_QR)
            return;
        if (resultCode == RESULT_OK) {
            String result = data.getStringExtra(NAME_INTENT_EXTRA);
            processScanData(result);
        } else if (resultCode == RESULT_CANCELED) {
            showToast("取消扫描", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onBackPressed() {
        if (allowFinish)
            super.onBackPressed();
        else {
            showToast("再按一次退出地图", Toast.LENGTH_SHORT);
            allowFinish = true;
            if (finishHandler != null)
                finishHandler.sendEmptyMessageDelayed(0, DELAY_FINISH);
        }
    }

    private void destroy() {
        if (currMap != null)
            currMap.destroy();
        if (arrowIcon != null && !arrowIcon.isRecycled())
            arrowIcon.recycle();
        if (holderIcon != null && !holderIcon.isRecycled())
            holderIcon.recycle();
        if (maps != null && !maps.isEmpty())
            maps.clear();
        if (mapsUnits != null && !mapsUnits.isEmpty())
            mapsUnits.clear();
        if (tagIds != null && !tagIds.isEmpty())
            tagIds.clear();
        if (mapNames != null && !mapNames.isEmpty())
            mapNames.clear();
        if (render != null && render.getTempUnits() != null)
            render.getTempUnits().clear();
        if (tags != null && !tags.isEmpty())
            tags.clear();
        if (ids != null && !ids.isEmpty())
            ids.clear();
        if (tagIds != null && !tagIds.isEmpty())
            tagIds.clear();
    }

    private void processScanData(String data) {
        if (data != null && !data.equals("")) {
            String[] temp = data.split(",");
            if (temp.length == 2 && checkLocationLegal(temp[0], temp[1])) {
                setCurrPos(temp[1]);
                return;
            }
        }
        showToast("非正确二维码", Toast.LENGTH_SHORT);
    }

    private boolean checkLocationLegal(String map, String id) {
        return true;
    }

    private void startLocating() {
        if (mainView == null ||
                locationPos.length < 2 ||
                locationPos[0] < 0 || locationPos[1] < 0)
            return;
        mainView.locateToCenter(locationPos[0], locationPos[1],
                mainView.getMaxScaleActually(),
                mainView.getCurrRotation(), DURATION_ANIMATION);
        mainView.startDirectionIndicate(compassStyle, locationPos[0], locationPos[1]);
        setLocateStyle(true);
    }

    private void stopLocating() {
        if (mainView == null)
            return;
        mainView.stopDirectionIndicate();
        clearLocation();
    }

    private void startNfc() {
        if (commander != null && supportNfc)
            commander.acquireReadTag();
    }

    private void initVariables() {
        innerSelect = 0;
        supportNfc = false;
        isNavigating = false;
        hiddenUI = false;
        allowFinish = false;
        locationPos = new float[3];
        selectionPos = new float[3];
        resetPosition(locationPos);
        compassStyle = VectorMapView.CompassStyle.INDEPENDENCE;
        factory = new MapFactory(this);
        render = new MapRender();
        analyzer = new NaviAnalyzer();
        currMap = null;
        arrowIcon = null;
        holderIcon = null;
        locationId = null;
        selectId = null;
        naviDialog = new NaviDialog(this, R.style.NaviDialogTheme);
        listDialog = new ListDialog(this, R.style.NaviDialogTheme);
        mapNames = new ArrayList<>();
        maps = new ArrayList<>();
        mapsUnits = new ArrayList<>();
        tagIds = new ArrayMap<>();
        tags = new ArrayList<>();
        ids = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                R.layout.item_inner, R.id.content);
        innerList.setAdapter(adapter);
        innerList.setVisibility(View.INVISIBLE);
        popupInfoMap = new ArrayMap<>();
        finishHandler = new FinishHandler();
        gestureDetector = new GestureDetector(this, new PopupGestureListener());
    }

    private void initPopupInfo() {
        popupInfoMap = ResourceCenterImpl.getInstance().acquireItems(this);
    }

    private void resetPosition(float[] pos) {
        if (pos == null)
            return;
        for (int i = 0; i < pos.length; i++)
            pos[i] = -1;
    }

    private void initNfc() {
        if (commander != null)
            commander.acquireNfcInit();
    }

    private void initNaviArrow() {
        arrowIcon = BitmapFactory.decodeStream(getResources()
                .openRawResource(R.raw.map_icon_arrow));
        if (arrowIcon == null) {
            Log.e(LOG_TAG, "can not create arrow");
            return;
        }
        if (mainView != null)
            mainView.setDirectionNarrow(arrowIcon);
    }

    private void initPlaceHolder() {
        holderIcon = BitmapFactory.decodeStream(getResources()
                .openRawResource(R.raw.map_icon_placeholder));
        if (holderIcon == null) {
            Log.e(LOG_TAG, "can not create holder");
            return;
        }
        if (mainView != null)
            mainView.setPlaceHolder(holderIcon);

    }

    private void initListener() {
        if (mainView != null)
            mainView.setOnMapActionOccurListener(new MapActionListener());
        if (naviDialog != null)
            naviDialog.setCallback(new NaviDialogCallback());
        if (listDialog != null)
            listDialog.setCallback(new ListDialogCallback());
        if (openNavi != null)
            openNavi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNavigating) {
                        stopNavigate();
                    } else {
                        openNaviDialog();
                    }

                }
            });
        if (locate != null)
            locate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mainView == null || locationId == null || locationId.equals("") ||
                            locationPos[0] < 0 || locationPos[1] < 0) {
                        showToast("未获得过位置", Toast.LENGTH_LONG);
                        return;
                    }
                    mainView.locateToCenter(locationPos[0], locationPos[1],
                            mainView.getMaxScaleActually(),
                            mainView.getCurrRotation(), DURATION_ANIMATION);
                }
            });
        if (openList != null)
            openList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listDialog == null)
                        return;
                    if (!listDialog.isShowing())
                        listDialog.show();
                }
            });
        if (reset != null) {
            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setCompassTransition(0, View.INVISIBLE);
                    if (mainView != null)
                        mainView.locateTo(0, 0, 0, 0,
                                mainView.getMinScaleActually(), 0, DURATION_ANIMATION);
                }
            });
        }
        if (innerList != null)
            innerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mainView == null || render == null || maps == null || mapsUnits == null)
                        return;
                    if (i == 0 && innerSelect == i) {
                        setViewTransition(innerList, rootView, View.INVISIBLE, new Fade());
                        return;
                    }
                    if (i != innerSelect) {
                        Log.v(LOG_TAG, "switch map--->" + i);
                        cancelSelection();
                        stopNavigate();
                        stopLocating();
                        closePopupCard();
                        if (reset != null)
                            reset.setRotation(-45);
                        innerSelect = i;
                        currMapSrc = maps.get(i);
                        mainView.setMapRecourse(currMapSrc);
                        initTagsList();
                        if (naviDialog != null) {
                            naviDialog.setTags(tags);
                            naviDialog.setIds(ids);
                        }
                        if (analyzer != null) {
                            analyzer.setSource(maps.get(i));
                            analyzer.initialize();
                        }
                        render.setCurrentUnits(mapsUnits.get(i));
                        if (i != 0)
                            mainView.locateToCenter(currMapSrc.getWidth() / 2,
                                    currMapSrc.getHeight() / 2, mainView.getMaxScaleActually(), 0, DURATION_ANIMATION);
                        else {
                            mainView.locateTo(0, 0, 0, 0,
                                    mainView.getMinScaleActually(), 0, DURATION_ANIMATION);
                        }
                        if (i == 0) {
                            setViewTransition(innerList, rootView, View.INVISIBLE, new Fade());
                        }
                    }
                }
            });
        if (openExplore != null)
            openExplore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, ExploreActivity.class),
                            ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                }
            });
        if (popupCard != null) {
            popupCard.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return gestureDetector != null && gestureDetector.onTouchEvent(motionEvent);
                }
            });
            popupCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, DetailActivity.class)
                                    .putExtra("id", popupId),
                            ActivityOptions
                                    .makeSceneTransitionAnimation(MainActivity.this,
                                            popupHeader, "shared_header")
                                    .toBundle());
                }
            });
        }
        if (popupMore != null)
            popupMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (popupId != null)
                        startActivity(new Intent(MainActivity.this, DetailActivity.class)
                                        .putExtra("id", popupId),
                                ActivityOptions
                                        .makeSceneTransitionAnimation(MainActivity.this,
                                                popupHeader, "shared_header")
                                        .toBundle());
                }
            });
        if (popupInner != null)
            popupInner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInnerList();
                }
            });
        if (scan != null)
            scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivityForResult(new Intent(MainActivity.this, ScanQRActivity.class),
                            REQUEST_CODE_SCAN_QR);
                }
            });

    }

    private void initVectorMap() {
        ResourceCenterImpl.getInstance().loadMapRecourse(factory);
        factory.setAsync(false);
        factory.setCallBack(new BuildCallback());
        factory.build();
    }

    private void setCompassTransition(float deg, int visibility) {
        if (reset == null)
            return;
        reset.setRotation(-45 + deg);
        setViewTransition(reset, rootView, visibility, new Fade());
    }

    private void setViewTransition(View view, ViewGroup viewGroup, int visibility, Transition transition) {
        if (view == null || viewGroup == null ||
                view.getVisibility() == visibility)
            return;
        if (rootView != null && transition != null)
            TransitionManager.beginDelayedTransition(viewGroup, transition);
        view.setVisibility(visibility);
    }

    private void clearLocation() {
        locationId = null;
        locationPos[0] = -1;
        locationPos[1] = -1;
        setLocateStyle(false);
    }

    private void clearSelection() {
        selectId = null;
        selectionPos[0] = -1;
        selectionPos[1] = -1;
    }

    private void setupMap() {
        tagIds.putAll(currMap.getTags());
        maps.add(currMap.acquireMainMap());
        currMapSrc = maps.get(0);
        mapsUnits.add(currMap.getMainRenderMap());
        mapNames.add(currMapSrc.getName());
        mapNames.set(0, "回到" + mapNames.get(0));
        analyzer.setSource(maps.get(0));
        analyzer.initialize();
        render.setCurrentUnits(mapsUnits.get(0));
        mainView.setMapRecourse(maps.get(0));
        mainView.setMapRender(render);
        initNaviArrow();
        initPlaceHolder();
        initTagsList();
        naviDialog.setIds(ids);
        naviDialog.setTags(tags);
        listDialog.setIds(ids);
        listDialog.setTags(tags);
    }

    private void initTagsList() {
        if (tagIds == null)
            return;
        tags.clear();
        ids.clear();
        if (currMapSrc == null) {
            for (Map.Entry<String, String> e :
                    tagIds.entrySet()) {
                tags.add(e.getValue());
                ids.add(e.getKey());
            }
        } else {
            List<Interest> interests = currMapSrc.getInterests();
            if (interests != null) {
                for (Interest e :
                        interests) {
                    if (tagIds.containsKey(e.getId())) {
                        tags.add(tagIds.get(e.getId()));
                        ids.add(e.getId());
                    }
                }
            }
        }
    }

    private void showToast(String text, int duration) {
        Toast.makeText(this, text, duration)
                .show();
    }

    private void closePopupCard() {
        if (popupCard == null || rootView == null)
            return;
        setViewTransition(popupCard, rootView, View.INVISIBLE, new Slide());
        popupId = null;
    }

    private void cancelSelection() {
        if (mainView != null)
            mainView.cancelSelection();
        clearSelection();
    }

    private boolean selectPlace(String id) {
        selectId = id;
        if (currMap == null || currMapSrc == null ||
                selectionPos == null || id == null)
            return false;
        boolean result = currMap.acquirePosition(id, selectionPos, currMapSrc);
        if (!result || mainView == null) {
            return false;
        }
        Log.v(LOG_TAG, "select place--->" + selectionPos[0] + "," + selectionPos[1]);
        mainView.selectPosition(selectionPos[0], selectionPos[1]);
        return true;
    }

    private void openNaviDialog() {
        if (naviDialog != null && !naviDialog.isShowing()) {
            String sId = null;
            String eId = null;
            if (locationId != null && !locationId.equals("") &&
                    selectId != null && !selectId.equals("")) {
                sId = locationId;
                eId = selectId;
            } else if (locationId != null && !locationId.equals("")) {
                sId = locationId;
                eId = null;
            } else if (selectId != null && !selectId.equals("")) {
                sId = selectId;
                eId = null;
            }
            naviDialog.setLocation(sId, eId);

            naviDialog.show();
        }
    }

    private void setLocateStyle(boolean isLocated) {
        if (locate != null) {
            if (isLocated) {
                locate.setClickable(true);
                locate.setEnabled(true);
                locate.setImageDrawable(getDrawable(R.drawable.ic_gps_fixed_black_48dp));
            } else {
                if (supportNfc) {
                    locate.setClickable(true);
                    locate.setEnabled(true);
                    locate.setImageDrawable(getDrawable(R.drawable.ic_gps_not_fixed_black_48dp));
                } else {
                    locate.setClickable(false);
                    locate.setEnabled(false);
                    locate.setImageDrawable(getDrawable(R.drawable.ic_gps_off_black_48dp));
                }
            }
        }
    }

    private void startNavigate(String sId, String eId) {
        if (!isNavigating)
            isNavigating = true;
        if (openNavi != null) {
            openNavi.setBackground(getDrawable(R.drawable.btn_style_rc_red_selector));
            openNavi.setText("停止导航");
        }
        if (mainView == null) {
            return;
        }
        if (currMap != null) {
            float[] pos1 = new float[3];
            float[] pos2 = new float[3];
            boolean result1 = currMap.acquirePosition(sId, pos1, currMapSrc);
            boolean result2 = currMap.acquirePosition(eId, pos2, currMapSrc);
            if (result1 && result2) {
                pos2[0] = pos2[0] - pos1[0];
                pos2[1] = pos2[1] - pos1[1];
                float deg = (float) (Math.atan2(pos2[1], pos2[0]) * 180 / Math.PI) + 90;
                Log.v(LOG_TAG, String.valueOf(deg));
                mainView.locateToCenter(pos1[0], pos1[1],
                        mainView.getMaxScaleActually(), -deg,
                        DURATION_ANIMATION);
                setCompassTransition(-deg, View.VISIBLE);
            } else if (result1) {
                mainView.locateToCenter(pos1[0], pos1[1],
                        mainView.getMaxScaleActually(), mainView.getCurrRotation(),
                        DURATION_ANIMATION);
            } else if (result2) {
                mainView.locateToCenter(pos2[0], pos2[1],
                        mainView.getMaxScaleActually(), mainView.getCurrRotation(),
                        DURATION_ANIMATION);
            } else {
                mainView.mapRefresh();
            }
        } else {
            mainView.mapRefresh();
        }
        Log.v(LOG_TAG, "start navigation");
    }

    private void stopNavigate() {
        if (isNavigating) {
            isNavigating = false;
        }
        if (openNavi != null) {
            openNavi.setBackground(getDrawable(R.drawable.btn_style_rc_selector));
            openNavi.setText("路线");
        }
        if (render != null && mainView != null) {
            render.getTempUnits().clear();
            mainView.mapRefresh();
        }
        Log.v(LOG_TAG, "stop navigation");
    }

    private void hideUI() {
        hiddenUI = true;
        if (rootView != null)
            TransitionManager.beginDelayedTransition(rootView, new Fade());
        openNavi.setVisibility(View.INVISIBLE);
        openExplore.setVisibility(View.INVISIBLE);
        openList.setVisibility(View.INVISIBLE);
        decoration.setVisibility(View.INVISIBLE);
        locate.setVisibility(View.INVISIBLE);
        reset.setVisibility(View.INVISIBLE);
        scan.setVisibility(View.INVISIBLE);
    }

    private void showUI() {
        hiddenUI = false;
        if (rootView != null)
            TransitionManager.beginDelayedTransition(rootView, new Fade());
        openNavi.setVisibility(View.VISIBLE);
        openExplore.setVisibility(View.VISIBLE);
        openList.setVisibility(View.VISIBLE);
        decoration.setVisibility(View.VISIBLE);
        locate.setVisibility(View.VISIBLE);
        scan.setVisibility(View.VISIBLE);
    }

    private void showInnerList() {
        if (mapNames == null || adapter == null)
            return;
        clearLocation();
        clearSelection();
        adapter.clear();
        adapter.addAll(mapNames);
        adapter.notifyDataSetChanged();
        if (innerList != null)
            setViewTransition(innerList, rootView, View.VISIBLE, new Fade());
    }

    private void setInnerMapsView(boolean hasInnerMaps) {
        if (popupInner == null)
            return;
        if (hasInnerMaps) {
            popupInner.setEnabled(true);
            popupInner.setText("室内地图");
        } else {
            popupInner.setEnabled(false);
            popupInner.setText("没有室内地图");
        }

    }

    private void clearExcludeFirst(List lists) {
        if (lists == null || lists.size() <= 1)
            return;
        Object o = lists.get(0);
        lists.clear();
        lists.add(o);
    }

    private boolean checkInnerMaps(String id) {
        if (currMap == null || id == null || maps.size() <= 0) {
            Log.e(LOG_TAG, "checkInnerMaps failed");
            return false;
        }
        String innerId = currMap.getInnerId(id, maps.get(0));
        if (innerId == null || innerId.equals("")) {
            Log.e(LOG_TAG, "inner not exist--->" + id);
            return false;
        }
        Log.v(LOG_TAG, "inner exist--->" + id);
        Map<MapSrc, List<RenderUnit>> innerMaps = currMap.getMaps(innerId);
        clearExcludeFirst(mapNames);
        clearExcludeFirst(maps);
        clearExcludeFirst(mapsUnits);
        for (Map.Entry<MapSrc, List<RenderUnit>> e :
                innerMaps.entrySet()) {
            mapNames.add(e.getKey().getName());
            maps.add(e.getKey());
            mapsUnits.add(e.getValue());
        }
        for (int i = maps.size() - 1; i > 1; i--)
            for (int j = 1; j < i; j++) {
                int a = Integer.valueOf(maps.get(j).getId().substring(9, 10));
                int b = Integer.valueOf(maps.get(j + 1).getId().substring(9, 10));
                if (a > b) {
                    Collections.swap(mapNames, j, j + 1);
                    Collections.swap(maps, j, j + 1);
                    Collections.swap(mapsUnits, j, j + 1);
                }
            }
        return true;
    }

    private void trySearch(String sId, String eId, NaviAnalyzer.SearchType type) {
        if (sId == null || eId == null || type == null ||
                analyzer == null || factory == null)
            return;
        List<Path> paths = analyzer.searchRoute(type, sId, eId);
        if (paths == null || paths.size() == 0) {
            Log.v(LOG_TAG, "find nothing for navigation");
            showToast("未搜索到可达路径", Toast.LENGTH_SHORT);
            return;
        }
        List<RenderUnit> units = factory.buildRenderPaths(paths);
        if (mainView == null || render == null)
            return;
        render.getTempUnits().clear();
        render.getTempUnits().addAll(units);
        Log.v(LOG_TAG, "find way for navigation");
        startNavigate(sId, eId);
    }

    private void acquireInfo(String id) {
        if (popupInfoMap == null || id == null || id.equals("") ||
                popupCard == null)
            return;
        if (popupInfoMap.containsKey(id)) {
            Log.v(LOG_TAG, "get info--->" + id);
            popupId = id;
            setViewTransition(popupCard, popupCard, View.VISIBLE, new Slide());
            DisplayItem item = popupInfoMap.get(id);
            if (item == null)
                return;
            if (popupHeader != null)
                popupHeader.setImageBitmap(item.getHeader());
            if (popupTitle != null)
                popupTitle.setText(item.getTag());
        }
    }

    private void setCurrPos(@NonNull String id) {
        if (currMap == null || currMapSrc == null)
            return;
        boolean result = currMap.acquirePosition(id, locationPos, currMapSrc);
        if (result) {
            Log.v(LOG_TAG, "locate success--->" + id);
            locationId = id;
            if (selectId != null && locationId.equals(selectId))
                cancelSelection();
            acquireInfo(id);
            if (mainView != null) {
                showToast("定位到" + tagIds.get(id), Toast.LENGTH_SHORT);
                startLocating();
            }
            setInnerMapsView(checkInnerMaps(id));
        } else {
            Log.e(LOG_TAG, "location failed--->" + id);
            showToast("无法找到该位置", Toast.LENGTH_LONG);
        }

    }

    private class ListDialogCallback implements ListDialog.SelectCallback {

        @Override
        public void onResult(String id) {
            if (listDialog != null)
                listDialog.dismiss();
            acquireInfo(id);
            setInnerMapsView(checkInnerMaps(id));
            if (selectPlace(id) && mainView != null) {
                setViewTransition(reset, rootView, View.VISIBLE, new Fade());
                mainView.locateToCenter(selectionPos[0], selectionPos[1],
                        mainView.getMaxScaleActually(), mainView.getCurrRotation(), DURATION_ANIMATION);
            }
        }
    }

    private class NaviDialogCallback implements NaviDialog.ResultCallback {

        @Override
        public void onResult(String sid, String eid, NaviAnalyzer.SearchType type) {
            if (naviDialog != null)
                naviDialog.dismiss();
            trySearch(sid, eid, type);
        }
    }

    private class MapActionListener implements VectorMapView.OnMapActionOccurListener {

        @Override
        public void onClickPoint(String s, float v, float v1) {
            Log.v(LOG_TAG, "click point--->" + s);
            acquireInfo(s);
            setInnerMapsView(checkInnerMaps(s));
            selectPlace(s);
            showUI();
        }

        @Override
        public void onClickMap(float v, float v1) {
            Log.v(LOG_TAG, "click map--->" + v + "," + v1);
            if (popupCard != null && popupCard.getVisibility() == View.VISIBLE)
                setViewTransition(popupCard, rootView, View.INVISIBLE, new Slide());
            else {
                cancelSelection();
                if (innerList != null && innerSelect == 0)
                    setViewTransition(innerList, rootView, View.INVISIBLE, new Fade());
                if (hiddenUI)
                    showUI();
                else
                    hideUI();
            }

        }

        @Override
        public void onDoubleTapMap() {
        }

        @Override
        public void onTranslateMap(float v, float v1) {
        }

        @Override
        public void onRotateMap(float v, float v1, float v2) {
            //Log.v(LOG_TAG, "map rotate--->" + v);
            if (reset != null && mainView != null) {
                if (reset.getVisibility() != View.VISIBLE)
                    setCompassTransition(mainView.getCurrRotation(), View.VISIBLE);
                reset.setRotation(mainView.getCurrRotation() - 45);
            }
        }

        @Override
        public void onScaleMap(float v, float v1, float v2) {
            if (reset != null && mainView != null) {
                if (reset.getVisibility() != View.VISIBLE)
                    setCompassTransition(mainView.getCurrRotation(), View.VISIBLE);
                reset.setRotation(mainView.getCurrRotation() - 45);
            }
        }

        @Override
        public void onTransformStart() {
        }

        @Override
        public void onTransformEnd() {
        }
    }

    private class BuildCallback implements MapFactory.CallBack {

        @Override
        public void buildFailed(String s) {
            Log.e(LOG_TAG, "build failed--->" + s);
        }

        @Override
        public void buildSuccess(VectorMap vectorMap) {
            currMap = vectorMap;
            setupMap();
        }
    }

    private class WrapListener implements NfcWrapListener {

        @Override
        public void setNfcCommander(NfcCommander commander) {
            Log.v(LOG_TAG, "nfc--->setCommander");
            MainActivity.this.commander = commander;
        }

        @Override
        public void onNfcFuncDetected(boolean supportNfc) {
            Log.v(LOG_TAG, "nfc--->" + supportNfc);
            MainActivity.this.supportNfc = supportNfc;
            if (supportNfc) {
                showToast("NFC功能正常", Toast.LENGTH_SHORT);
                startNfc();
            } else {
                showToast("未检测到NFC功能", Toast.LENGTH_LONG);
            }
            setLocateStyle(false);
        }

        @Override
        public void onNfcTagDetected(boolean tagValidity, TagInfo info) {
            Log.v(LOG_TAG, "card--->" + tagValidity);
            if (tagValidity)
                Log.v(LOG_TAG, "info--->" +
                        info.getTagID() +
                        "," +
                        info.getTagName());
        }

        @Override
        public void onNfcReadStart() {
            Log.v(LOG_TAG, "Nfc--->Read Start");
        }

        @Override
        public void onNfcReadDone(List<String> contents) {
            Log.v(LOG_TAG, "Nfc--->Read Done" + contents.toString());
            if (contents.size() > 0) {
                setCurrPos(contents.get(0));
            }
            if (commander != null)
                commander.acquireReadTag();
        }

        @Override
        public void onNfcWrittenStart() {
        }

        @Override
        public void onNfcWrittenDone() {
        }
    }

    private class FinishHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            allowFinish = false;
        }
    }

    private class PopupGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) < Math.abs(velocityY) && velocityY > 0) {
                closePopupCard();
                return true;
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
