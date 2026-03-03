package com.example.administrator.dsgroupware2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.Browser;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.core.content.ContextCompat;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.os.Message;

import android.util.TypedValue;
import android.widget.LinearLayout;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.widget.Toolbar;

import android.app.DownloadManager;
import android.os.Environment;
import android.webkit.URLUtil;

public class MainActivity extends AppCompatActivity {
    /** Called when the activity is first created. */

    private String lastMainPageUrl = null;
    private String lastPopupPageUrl = null;

    private FrameLayout popupContainer;
    private WebView popupWebView;

    //private String URL1 = "http://m.dstoolpia.co.kr:8080?nPno=";
    //private String URL1="http://m.dstoolpia.co.kr:8080/default2.aspx?nPno=";
    //private String URL2 = "http://m.dstoolpia.co.kr:8080/index.aspx";

    private String URL2 = "https://mg.dstoolpia.kr/index.aspx";
    //private String URL2 = "http://localhost:6040/index.aspx";

    private String nPhoneReal;
    private String nDeviceID;
    private int nNowDay;
    private int nTimeNow;

    WebView webview;
    WebView webviewLo;
    LocationManager lm;
    LocationListener ll;

    //* 2021.07.09 멤버변수 추가 IJH
    private WebViewInterface _webViewInterface;

    public final static int FILECHOOSER_NORMAL_REQ_CODE = 2001;
    public final static int FILECHOOSER_LOLLIPOP_REQ_CODE = 2002;

    public ValueCallback<Uri> filePathCallbackNormal;
    public ValueCallback<Uri[]> filePathCallbackLollipop;

    private String _userCode = "";

    // 모바일 그룹웨어 실서버 url
    private String _url = "https://mg.dstoolpia.kr";
    //private String _url = "http://localhost:6040";

    // 테스트 서버 url(실기 테스트시 반드시 와이파이 켜야함)
    //private String _url = "http://210.121.204.81/";

    private Uri cameraImageUri = null;

    public static Context mContext;

    private AlarmManager am;
    private Intent intent;
    private PendingIntent servicePending;
    private Intent serviceIntent;

    private String _userToken = "";
    private static final String TAG = "MainActivity";
    //*

    @Override
    public void onStart() {
        super.onStart();
        CookieSyncManager.createInstance(this);
        
        // 앱의 알림 설정 체크
        // DSToolpia : 푸시 알림
        // receive : 포어그라운드 알림(gps)
        if(NotificationManagerCompat.from(this.getApplicationContext()).areNotificationsEnabled() == false
                //|| NotificationManagerCompat.from(this.getApplicationContext()).getNotificationChannelCompat("fcm_default_channel").getImportance() == 0){
                ){
            new AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("앱 알림설정이 차단되어있습니다.\r\n애플리케이션 정보 > 알림에서\r\n[알림받기]항목 및 하위 항목을 활성화시켜주세요.")
                    .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).setCancelable(false).show();

            return;
        }

        // 안드로이드 버전9(API 28) 미만일 경우 앱 사용 불가 처리
        // 기사분들에게 지급된 핸드폰 기종이 갤럭시S8인데 여기에 설치 가능한 마지막 안드로이드 버전이 9
        //
        // 안드로이드 최소사양 버전8(API 26) Oreo로 변경. 2021.08.26
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            new AlertDialog.Builder(this)
                .setTitle("알림")
                //.setMessage("안드로이드 버전 9.0(API 28) 이상에서만\r\n\r\n이용하실 수 있습니다.")
                .setMessage("안드로이드 버전 8.0(API 26) 이상에서만\r\n\r\n이용하실 수 있습니다.")
                .setPositiveButton("확인", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                }).setCancelable(false)
                .show();
        }

        setUserToken();
    }

    private void setUserToken(){
        // 토큰 값을 가져온다.
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<String> task) {
                        if(!task.isSuccessful()){
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        _userToken = task.getResult();
                        String msg = "FCM registration Token : " + _userToken;
                        Log.d(TAG, msg);

                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String title = "";
        String receiveSeq = "";

        if(intent != null && intent.getExtras() != null){
            if(intent.getExtras().getString("title") != null
                    && intent.getExtras().getString("receiveSeq") != null){
                title = intent.getExtras().getString("title");
                receiveSeq = intent.getExtras().getString("receiveSeq");
                //
                // 실제로 필요한 값은 receiveSeq 하나이다. 나머지는 notification 서비스단에서만 필요하며
                // 여기서는 단지 테스트를 위해서 함께 값을 받도록 하였다. (title과 messageBody는 추후 정리해주면 됨)
                //
                // receiveSeq가 null이 아닐 시 여기서 webView의 url을 변경시켜주면 된다.
                // 단, 읽음 처리가 된 건인지 확인 후 이미 읽음처리가 된 경우 아무 행동도 하지 않는다.
                //
                webview.loadUrl(_url + "/DsttsBoard/Board_Detail.aspx?nSeqNo=" + receiveSeq + "&Section=1");
            }
        }

        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync();

        // ★ 복귀 시, 팝업이 꼬여서 흰 화면 덮는 거 방지
        if (popupContainer != null && popupWebView == null) {
            popupContainer.removeAllViews();
            popupContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CookieSyncManager.getInstance().stopSync();

        // ★ 외부앱(마켓 등)으로 나갈 때 팝업 레이어가 남아 흰 화면 되는 케이스 차단
        if (popupWebView != null) closePopup();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasForegroundServicePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return true; // Android 14 미만에서는 필요 없음
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void startGpsServiceIfPermitted() {
        if (hasLocationPermission() && hasForegroundServicePermission()) {
            Intent serviceIntent = new Intent(MainActivity.this, GpsService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } else {
            // 권한이 없으면 GpsService 실행을 보류 (앱이 충돌하는 것을 방지)
            Log.w("MainActivity", "권한이 없어 GpsService를 시작할 수 없음.");
        }
    }

    private void startGpsService() {
        Intent serviceIntent = new Intent(this, GpsService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // * 코드 추가 2021.07.09.ijh
        mContext = this;
        checkVerify();
        // *

        // default firebase채널은 Importance가 LOW로 세팅되어있어 PUSH가 왔을 때 진동 및 소리가 나지 않음.
        // 중요도를 변경해 보았으나 한 번 생성된 채널에 대한 Importance는 변경할 수 없다고 함.
        // NOTIFICATION을 수신 받기 위한 별도의 채널을 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = "DstMessage";
            CharSequence name = "DstMessage";
            String description = "동신툴피아 메시지 알람";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                    checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION);
            }

            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[0]), 1001);
            } else {
                startGpsService();
            }
        } else {
            startGpsService();
        }

        setContentView(R.layout.activity_main);

        popupContainer = findViewById(R.id.popupContainer);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View root = findViewById(R.id.root);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("동신툴피아 그룹웨어");
        }

        toolbar.setTitleTextColor(android.graphics.Color.WHITE);

        View bottomBar = findViewById(R.id.bottomBar);

        final int actionBarSizePx = getActionBarHeightPx();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // 1) toolbar 높이를 status bar 만큼 늘리고, 내용은 paddingTop으로 내려줌
            ViewGroup.LayoutParams tlp = toolbar.getLayoutParams();
            tlp.height = actionBarSizePx + sysBars.top;
            toolbar.setLayoutParams(tlp);

            toolbar.setPadding(
                    sysBars.left,
                    sysBars.top,
                    sysBars.right,
                    0
            );

            // 2) 하단바는 네비게이션바 높이만큼 위로
            ViewGroup.MarginLayoutParams blp = (ViewGroup.MarginLayoutParams) bottomBar.getLayoutParams();
            blp.bottomMargin = sysBars.bottom;
            bottomBar.setLayoutParams(blp);

            // 3) popupContainer는 status bar 밑으로 내려오게 paddingTop
            popupContainer.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom);

            return insets;
        });

        // 최초 1회 인셋 계산 트리거
        ViewCompat.requestApplyInsets(root);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }


        // * 알람 등록 2021.07.09.ijh
        am = (AlarmManager)getSystemService(ALARM_SERVICE);
        intent = new Intent(this.getApplicationContext(), Alarm.class);
        servicePending = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 인터벌은 5분(300 * 1000) -> 테스트시 10초(10 * 1000)로 변경
        //long repeatInterval = 300 * 1000;
        //
        // 10분  -> 인터벌 10분으로 결정되었으나 현재(2021.07.28)기준 인터벌 5분으로 배포되어있음. 다음 배포때 반영될 예정.
        long repeatInterval = 600 * 1000;
        //
        // 10초(테스트용)
        //long repeatInterval = 10 * 1000;

        long triggerTime = (SystemClock.elapsedRealtime() + repeatInterval);
        am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, repeatInterval, servicePending);
        // *

        Calendar Now_cal = Calendar.getInstance();
        TelephonyManager systemService = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (systemService.getSimState() == TelephonyManager.SIM_STATE_UNKNOWN) {
            // Wifi-only device
            nPhoneReal = "02-803-0003";
        } else if (systemService.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
            // 유심이 없는 경우
            nPhoneReal = "02-803-0003";
        } else {
            // 유심이 존재하는 경우
            //nPhoneReal = systemService.getLine1Number().trim(); //폰번호를 가져오는 겁니다..getSimOperator
            nPhoneReal= "";
        }

        // 권한 문제로 더 이상 사용할 수 없기 때문에 주석처리 2021.07.09.ijh
        //nDeviceID = systemService.getDeviceId();
        nDeviceID = "";

        //폰번호 체크 후 없으면 바로 끝내기
        //if (!PhoneNoCheck(nPhoneReal)) finish();

        webview = (WebView) findViewById(R.id.webview);

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setUseWideViewPort(true);

        //* 2021.07.09 세팅 추가 IJH
        _webViewInterface = new WebViewInterface(MainActivity.this, webview);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            webview.getSettings().setDisplayZoomControls(false);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            webview.getSettings().setTextZoom(100);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webview.getSettings().setSafeBrowsingEnabled(false);
        }

        webview.getSettings().setGeolocationEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setGeolocationDatabasePath(getFilesDir().getPath());

        webview.getSettings().setSupportMultipleWindows(true);

        // 안드로이드 14(API34)에서 사용할 수 없는 메서드. (이미 13에서 폐기됨)
        //webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setDatabaseEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);


        WebView.setWebContentsDebuggingEnabled(true);
        webview.addJavascriptInterface(_webViewInterface, "android");

        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setSupportZoom(true);

        //*

        //-----------------------------------------------------위치저장용 WebViewLo 생성
//        webviewLo = (WebView) findViewById(R.id.webviewLo);
//        webviewLo.getSettings().setJavaScriptEnabled(true);
//        webviewLo.getSettings().setUseWideViewPort(true);
        //------------------------------------------------------


        nNowDay = Now_cal.get(Calendar.DAY_OF_MONTH);

        //if (nPhoneReal.substring(0,2).equals("82")) nPhoneReal="0" + nPhoneReal.substring(2);

        //URL1 = URL1 + getMD5Hash(nNowDay + ":" + nDeviceID) + "DSTTS";
        //URL1 = URL1 + getMD5Hash(nNowDay + ":" + nPhoneReal.replace(" ", "") + ":" + nDeviceID) + "&nPno2=" + (nDeviceID + ":" + nNowDay + ":" + nPhoneReal.replace(" ", "") + "&nPno3=" + getMD5Hash(nNowDay + ":" + nDeviceID));
        //URL1 = URL1 + nNowDay + ":" + nPhoneReal + ":" + nDeviceID;


        //webview.setWebChromeClient(new WebChromeClient());
        //** setWebChromeClient 메서드 호출부 변경 2021.07.09.ijh
        webview.setWebChromeClient(new WebChromeClient(){
            // Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            // Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                filePathCallbackNormal = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
            }

            // Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            // For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                if (filePathCallbackLollipop != null) {
                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }
                filePathCallbackLollipop = filePathCallback;

                boolean isCapture = fileChooserParams.isCaptureEnabled();
                runCamera(isCapture);

                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin,true,false);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                closePopup();

                popupWebView = new WebView(view.getContext());

                popupWebView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
                    startDownload(url, userAgent, contentDisposition, mimeType);

                    popupWebView.post(() -> {
                        try { popupWebView.stopLoading(); } catch (Exception ignored) {}
                        closePopup();
                    });
                });

                WebSettings s = popupWebView.getSettings();

                // 기본
                s.setJavaScriptEnabled(true);
                s.setDomStorageEnabled(true);
                s.setDatabaseEnabled(true);
                s.setUseWideViewPort(true);
                s.setLoadWithOverviewMode(true);

                // window.open 관련
                s.setSupportMultipleWindows(true);
                s.setJavaScriptCanOpenWindowsAutomatically(true);

                // 줌(핀치/더블탭)
                s.setSupportZoom(true);
                s.setBuiltInZoomControls(true);
                s.setDisplayZoomControls(false); // 줌 버튼 숨김(핀치만 사용)

                // (선택) 텍스트 줌 고정 - 메인과 동일하게 맞추고 싶으면
                // s.setTextZoom(100);

                // 접근성/Lint 경고 방지용 (동작엔 큰 영향 없음)
                popupWebView.setOnTouchListener((v, event) -> {
                    if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                        v.performClick();
                    }
                    return false; // WebView 기본 동작(스크롤/줌) 유지
                });

                // 팝업 웹뷰 클라이언트: viewport 확대금지 우회 + 링크는 팝업 안에서 열리게
                popupWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest request){
                        Uri u = request.getUrl();
                        String url = (u != null) ? u.toString() : null;

                        if (handleSpecialSchemes(url)) return true;

                        // 다운로드/첨부는 WebView로 흘려서 DownloadListener가 받게
                        if (isDownloadRedirectUrl(url) || looksLikeAttachment(url)) {
                            return false;
                        }

                        return false;
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, String url) {

                        if (handleSpecialSchemes(url)) return true;

                        if (isDownloadRedirectUrl(url) || looksLikeAttachment(url)) {
                            return false;
                        }
                        return false;
                    }

                    @Override
                    public void onPageFinished(WebView v, String url) {
                        super.onPageFinished(v, url);

                        // ★ viewport가 user-scalable=no 로 막혀있을 때 확대가 안 되는 케이스 우회
                        String js =
                                "(function(){" +
                                        "var meta=document.querySelector('meta[name=viewport]');" +
                                        "if(!meta){" +
                                        "  meta=document.createElement('meta');" +
                                        "  meta.name='viewport';" +
                                        "  document.head.appendChild(meta);" +
                                        "}" +
                                        "meta.setAttribute('content','width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes');" +
                                        "})();";

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            v.evaluateJavascript(js, null);
                        } else {
                            v.loadUrl("javascript:" + js);
                        }
                    }
                });

                // 팝업 WebChromeClient: 팝업 닫기 + (팝업 안에서 또 window.open)도 동일 처리
                popupWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onCloseWindow(WebView window) {
                        closePopup();
                    }

                    @Override
                    public boolean onCreateWindow(WebView v, boolean isDialog2, boolean isUserGesture2, Message resultMsg2) {
                        // 팝업 내부에서 또 window.open 호출되면, 같은 popupWebView에서 열리게 처리
                        WebView.WebViewTransport transport2 = (WebView.WebViewTransport) resultMsg2.obj;
                        transport2.setWebView(popupWebView);
                        resultMsg2.sendToTarget();
                        return true;
                    }
                });

                // ★ 화면에 붙이기
                if (popupContainer != null) {
                    popupContainer.setVisibility(View.VISIBLE);

                    // 혹시 남아있는 뷰가 있으면 정리
                    popupContainer.removeAllViews();

                    popupContainer.addView(
                            popupWebView,
                            new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                            )
                    );
                } else {
                    Log.e("POPUP", "popupContainer is null. activity_main.xml에 popupContainer가 있는지 확인해줘.");
                }

                // ★ 핵심: window.open 결과를 popupWebView로 연결
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popupWebView);
                resultMsg.sendToTarget();

                return true;
            }
        });

        // 웹뷰에서 파일등의 다운로드를 받게될 경우 핸들링해주기 위해 필요
        webview.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            startDownload(url, userAgent, contentDisposition, mimeType);

            webview.post(() -> {
                try { webview.stopLoading(); } catch (Exception ignored) {}

                if (lastMainPageUrl != null && lastMainPageUrl.length() > 0) {
                    webview.loadUrl(lastMainPageUrl);
                } else {
                    webview.loadUrl(_url + "/index.aspx");
                }
            });
        });

        //**

        webviewLo = (WebView)findViewById(R.id.webviewLo);
        webviewLo.getSettings().setJavaScriptEnabled(true);
        webviewLo.setWebViewClient(new WebViewClient());
        webviewLo.setWebChromeClient(new WebChromeClient());

        //webview.loadUrl(URL1);

        // 현재 날짜값
        String currentDate = new SimpleDateFormat("yyyyMMdd")
                .format(new Date(System.currentTimeMillis()));

        // 암호화
        String currentDateMD5 = getMD5Hash("DSMG." + currentDate);

        if(getIntent().getExtras() != null
            && getIntent().getExtras().getString("receiveSeq") != null
            && getIntent().getExtras().getString("receiveSeq").trim() != ""){
            webview.loadUrl(_url + "/DsttsBoard/Board_Detail.aspx?nSeqNo=" + getIntent().getExtras().getString("receiveSeq") + "&Section=1");
        }
        else{
            webview.loadUrl(_url + "/index.aspx");
        }


//        //----------------------------------------------------------위치보기 정의부
//        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        ll = new moistenerLo();
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }

//        if (isGPSEnabled()) {
//        }

//        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60 * 5, 0, ll);   //1000 * 60
        //------------------------------------------------------------
        //

        final Activity activity = this;
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
                Uri u = request.getUrl();
                String url = (u != null) ? u.toString() : null;

                if (handleSpecialSchemes(url)) return true;

                // cd 중계 URL은 OS로 넘겨서 앱 링크 매칭을 OS에 맡기는 게 제일 안정적
                if (isDownloadRedirectUrl(url)) {
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        i.addCategory(Intent.CATEGORY_BROWSABLE);
                        startActivity(i);
                        return true;
                    } catch (Exception e) {
                        Log.e("WV_CD", "Open redirect url failed: " + url, e);
                    }
                }

                return false; // 나머지는 WebView가 로드
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (handleSpecialSchemes(url)) return true;
                if (isDownloadRedirectUrl(url)) {
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        i.addCategory(Intent.CATEGORY_BROWSABLE);
                        startActivity(i);
                        return true;
                    } catch (Exception e) {
                        Log.e("WV_CD", "Open redirect url failed: " + url, e);
                    }
                }

                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                _userCode = getCookieValue(url, "MGUSERID");
                CookieSyncManager.getInstance().sync();

                String js =
                        "(function(){try{ " +
                                "if(typeof setTokenFromMobile==='function'){ setTokenFromMobile('" + _userToken + "'); }" +
                                "}catch(e){} })();";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    view.evaluateJavascript(js, null);
                } else {
                    view.loadUrl("javascript:" + js);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    if (!isDownloadRedirectUrl(url) && !looksLikeAttachment(url)) {
                        lastMainPageUrl = url;
                    }
                }
            }


            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(MainActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton b1 = (ImageButton) findViewById(R.id.Button1);
        ImageButton b2 = (ImageButton) findViewById(R.id.Button2);
        ImageButton b3 = (ImageButton) findViewById(R.id.Button3);
        ImageButton b4 = (ImageButton) findViewById(R.id.Button4);

        ImageButton b9 = (ImageButton) findViewById(R.id.Button9);

        //버튼 1클릭시작
        b1.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                webview.loadUrl(_url+"/index.aspx");
            }
        });

        //버튼 1클릭끝2시작
        b2.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                webview.goBack();
            }
        });

        //2끝 3시작
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                webview.goForward();
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                webview.reload();
            }
        });

        b9.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //finish();
                setDialog();
            }
        });

        // 현재 로케이션 저장
        ((ImageButton)findViewById(R.id.Button4_LOC)).setOnClickListener(view -> {
            saveUserLocation();
        });
    }

    private void saveUserLocation(){
        Alarm alarm = new Alarm();
        LocationCoordinates locationInfo = alarm.getLocation();

        if(_userCode.trim() == ""){
            Toast.makeText(this.getApplicationContext(), "로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(locationInfo.latitude > 0.0 || locationInfo.longitude > 0.0){
            String url = "https://dstoolpia.kr/LocationListener.aspx?type=userlocation&id=" + _userCode + "&latitude="
                    + locationInfo.latitude + "&longitude=" + locationInfo.longitude;
            webviewLo.loadUrl(url);
            Toast.makeText(this.getApplicationContext(), "위치정보를 저장하였습니다.", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this.getApplicationContext(), "위치정보를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 사용자 권한 체크 2021.07.09.ijh
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkVerify() {

        if(
            //위치
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            // 포그라운드로 위치정보가 수신되므로 백그라운드 권한은 필요가 없다.
            //checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            //저장공간
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            // 전화
            checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            // 카메라
            checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            // SMS
            checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
        ){
            requestPermissions(new String[]{
                    // 위치
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    // 포그라운드로 위치정보가 수신되므로 백그라운드 권한은 필요가 없다.
                    //Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    // 저장공간
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    // 전화
                    Manifest.permission.READ_PHONE_STATE,
                    // 카메라
                    Manifest.permission.CAMERA,
                    // SMS
                    Manifest.permission.READ_SMS
            }, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            boolean allGranted = true;

            if(grantResults.length > 0){
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            }else{
                allGranted = false;
            }

            if (allGranted) {
                startGpsService();
            } else {
                Log.w("MainActivity", "필수 권한이 없어 GpsService 실행 불가.");
            }
        }

        if (requestCode == 1) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; ++i) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED
                            && !permissions[i].equals("android.permission.READ_SMS")
                            && !permissions[i].equals("android.permission.WRITE_EXTERNAL_STORAGE")
                            && !permissions[i].equals("android.permission.READ_EXTERNAL_STORAGE")) {
                        new AlertDialog.Builder(this).setTitle("알림").setMessage("권한을 허용(항상허용)으로 해주셔야 앱을 이용할 수 있습니다.")
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).setCancelable(false).show();

                        return;
                    }
                }
            }
        }
    }

    private String getCookieValue(String url, String cookieName) {
        String cookieValue = null;

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(_url);
        String[] temp = null;

        try{
            temp = cookies.split(";");

            for(String ar1 : temp){
                if(ar1.contains(cookieName) == true){
                    String[] innerTemp = ar1.split("=");
                    cookieValue = innerTemp[1];
                    break;
                }
            }
        }
        catch(Exception ex){
            cookieValue = null;
        }

        return cookieValue;
    }

    // 카메라 컨트롤 메서드 2021.07.09.ijh
    private void runCamera(boolean _isCapture) {
        if (!_isCapture)
        {// 갤러리 띄운다.
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            pickIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            String pickTitle = "사진 가져올 방법을 선택하세요.";
            Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);

            startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE);
            return;
        }

        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File path = getFilesDir();
        File file = new File(path, "fokCamera.png");

        // File 객체의 URI 를 얻는다.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            String strpa = this.getApplicationContext().getPackageName();
            cameraImageUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".fileprovider", file);
        }
        else
        {
            cameraImageUri = Uri.fromFile(file);
        }

        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

        if (!_isCapture)
        { // 선택팝업 카메라, 갤러리 둘다 띄우고 싶을 때..
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            pickIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            String pickTitle = "사진 가져올 방법을 선택하세요.";
            Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);

            // 카메라 intent 포함시키기..
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{intentCamera});
            startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE);
        }
        else
        {// 바로 카메라 실행..
            startActivityForResult(intentCamera, FILECHOOSER_LOLLIPOP_REQ_CODE);
        }
    }

    /* public void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.removeUpdates(ll);
    }*/

    ////////////////////////////////////////////////////////////////////////////

    static boolean HttpDown(String Url, String FileName) {
        URL imageurl;
        int Read;
        try {
            imageurl = new URL(Url);
            HttpURLConnection conn= (HttpURLConnection)imageurl.openConnection();
            conn.connect();
            int len = conn.getContentLength();
            byte[] raster = new byte[len];
            InputStream is = conn.getInputStream();
            FileOutputStream fos = new FileOutputStream(FileName);

            for (;;) {
                Read = is.read(raster);
                if (Read <= 0) {
                    break;
                }
                fos.write(raster,0, Read);
            }

            is.close();
            fos.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            // 1) 팝업이 떠 있으면 팝업 우선 처리
            if (popupWebView != null) {
                if (popupWebView.canGoBack()) {
                    popupWebView.goBack();
                } else {
                    closePopup();
                }
                return true;
            }

            // 2) 메인 웹뷰 히스토리 있으면 뒤로
            if (webview != null && webview.canGoBack()) {
                webview.goBack();
                return true;
            }

            // 3) 그 외엔 종료 다이얼로그
            setDialog();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    void stopAllServices() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.service.getPackageName().equals(getPackageName())) {
                stopService(new Intent(this, service.service.getClass()));
            }
        }
    }

    void setDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("프로그램을 종료하시겠습니까?")
            .setCancelable(false)
            .setPositiveButton("예", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id){
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        stopAllServices();
                        finishAndRemoveTask(); // API 21 이상에서는 태스크까지 제거
                        System.exit(0);  // 프로세스 종료
                        android.os.Process.killProcess(android.os.Process.myPid());  // 강제 종료
                    } else {
                        finish(); // API 21 미만에서는 기존 방식 유지
                    }
                }
            })
            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

        builder.create().show();
    }

    public static String getMD5Hash(String s) {
        MessageDigest m = null;
        String hash = null;

        try {
            m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes(),0,s.length());
            hash = new BigInteger(1, m.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hash;
    }

//    //-------------------------------------------------------GPS 분석부
//    class moistenerLo implements LocationListener{
//        @Override
//        public void onLocationChanged(Location location) {
//            if(location != null)
//            {
//                Calendar Now_cal = Calendar.getInstance();
//                nTimeNow = Now_cal.get(Calendar.HOUR_OF_DAY);
//                if (nTimeNow > 7) {
//                    if (nTimeNow < 20) {
//                        double pLong = location.getLongitude();
//                        double pLat = location.getLatitude();
//                        webviewLo.loadUrl("http://m.dstoolpia.co.kr:8080/Location.aspx?ID=" + nDeviceID + "&Lat=" + Double.toString(pLat) + "&Long=" + Double.toString(pLong));
//                        //-------------------sql Conn
//                        //--------------------
//                    }
//                }
//            }
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//
//        }
//    }

//    private boolean isGPSEnabled() {
//        boolean gpsEnabled = false;
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            gpsEnabled = true;
//            return gpsEnabled;
//        } else {
//            showGPSDisabledAlertToUser();
//        }
//        return gpsEnabled;
//    }

//    private void showGPSDisabledAlertToUser() {
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//        alertDialogBuilder
//                .setMessage("GPS가 꺼져있습니다.")
//                .setCancelable(false)
//                .setPositiveButton("설정화면으로 이동",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                Intent callGPSSettingIntent = new Intent(
//                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                                startActivity(callGPSSettingIntent);
//                            }
//                        });
//
//        alertDialogBuilder.setNegativeButton("취소",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//        AlertDialog alert = alertDialogBuilder.create();
//        alert.show();
//    }
    //---------------------------------------------------------


    // 카메라로 촬영된 이미지를 처리하기 위해 onActivityResult 메서드 재정의 2021.07.09.ijh
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case FILECHOOSER_NORMAL_REQ_CODE:
                if (resultCode == RESULT_OK)
                {
                    if (filePathCallbackNormal == null) return;
                    Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
                    filePathCallbackNormal.onReceiveValue(result);
                    filePathCallbackNormal = null;
                }
                break;
            case FILECHOOSER_LOLLIPOP_REQ_CODE:
                if (resultCode == RESULT_OK)
                {
                    if (filePathCallbackLollipop == null) return;
                    if (data == null)
                        data = new Intent();
                    if (data.getData() == null)
                        data.setData(cameraImageUri);

                    filePathCallbackLollipop.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                    filePathCallbackLollipop = null;
                }
                else
                {
                    if (filePathCallbackLollipop != null)
                    {
                        filePathCallbackLollipop.onReceiveValue(null);
                        filePathCallbackLollipop = null;
                    }

                    if (filePathCallbackNormal != null)
                    {
                        filePathCallbackNormal.onReceiveValue(null);
                        filePathCallbackNormal = null;
                    }
                }
                break;
            default:

                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // GPS정보 저장 2021.07.09.ijh
    public void SaveLocationInfo(double lat, double lng)
    {
        String url = "https://dstoolpia.kr/LocationListener.aspx?type=location&id=" + _userCode + "&lat=" + Double.toString(lat) + "&long=" + Double.toString(lng);

        if(_userCode != null && _userCode.trim() != ""){
            Log.d(TAG, url);
            webviewLo.loadUrl(url);
            //webviewLo.loadUrl("http://localhost:58804/LocationListener.aspx?type=location&id=" + _userCode + "&lat=" + Double.toString(lat) + "&long=" + Double.toString(lng));
        }
    }

    // 토큰 정보 저장
    public void SaveTokenInfo()
    {
        String url = "https://dstoolpia.kr/LocationListener.aspx?type=token&id=" + _userCode + "&token=" + _userToken;
        //String url = "http://localhost:58804/LocationListener.aspx?type=token&id=" + _userCode + "&token=" + _userToken;

        if(_userCode != null && _userCode.trim() != ""){
            Log.d(TAG, url);
            webviewLo.loadUrl(url);
        }
    }

    // 앱 종료시 서비스도 종료될 수 있도록 메서드 재정의 2021.07.09.ijh
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(serviceIntent != null){
            stopService(serviceIntent);
            serviceIntent = null;
        }

        if(webview != null){
            webview.destroy();
        }

        if(webviewLo != null){
            webviewLo.destroy();
        }
    }

    // 인터페이스를 위한 inner class 추가. 2021.07.09.IJH
    private class WebViewInterface {
        private WebView _appView;
        private Activity _context;

        public WebViewInterface(Activity activity, WebView webView){
            _context = activity;
            _appView = webView;
        }
    }

    private void closePopup() {
        if (popupWebView == null) return;

        WebView wv = popupWebView;
        popupWebView = null;

        try {
            if (popupContainer != null) {
                popupContainer.removeAllViews();
                popupContainer.setVisibility(View.GONE);
            }

            wv.stopLoading();
            wv.loadUrl("about:blank");
            wv.clearHistory();

            // destroy는 즉시가 아니라 post로 한 박자 늦춰서 크롬 내부 콜백 꼬임을 줄임
            wv.post(() -> {
                try { wv.destroy(); } catch (Exception ignored) {}
            });
        } catch (Exception e) {
            Log.e("POPUP", "closePopup failed", e);
            try { wv.destroy(); } catch (Exception ignored) {}
        }
    }

    private int getActionBarHeightPx() {
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return 0;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private boolean looksLikeAttachment(String url) {
        if (url == null) return false;
        String u = url.toLowerCase();

        // 쿼리스트링/토큰 붙어도 잡히게 contains로 체크
        return u.contains(".pdf") || u.contains(".hwp") || u.contains(".zip")
                || u.contains(".doc") || u.contains(".docx")
                || u.contains(".xls") || u.contains(".xlsx")
                || u.contains(".ppt") || u.contains(".pptx")
                || u.contains(".txt") || u.contains(".csv");
    }

    private boolean isDownloadRedirectUrl(String url) {
        if (url == null) return false;
        String u = url.toLowerCase();
        // 패턴: https://cd.dstoolpia.kr/url/?key=...
        return u.startsWith("https://cd.dstoolpia.kr/url/?key=")
                || u.startsWith("http://cd.dstoolpia.kr/url/?key=")
                || u.contains("://cd.dstoolpia.kr/url/?key=");
    }

    private void startDownload(String url, String userAgent, String contentDisposition, String mimeType) {
        Log.d("WV_DL", "DL url=" + url + " cd=" + contentDisposition + " ct=" + mimeType);
        enqueueDownload(url, userAgent, contentDisposition, mimeType);
    }

    private void enqueueDownload(String url, String userAgent, String contentDisposition, String mimeType) {
        try {
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));

            if (userAgent != null) req.addRequestHeader("User-Agent", userAgent);

            String cookie = CookieManager.getInstance().getCookie(url);
            if (cookie != null) req.addRequestHeader("Cookie", cookie);

            // 파일명: contentDisposition이 없으면 bin이 될 수 있음(일단 다운로드부터 살림)
            String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);

            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            if (mimeType != null) req.setMimeType(mimeType);

            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            long id = dm.enqueue(req);

            Log.d("WV_DL", "enqueue ok id=" + id + " url=" + url + " cd=" + contentDisposition + " ct=" + mimeType);
            Toast.makeText(this, "다운로드 시작", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("WV_DL", "enqueue failed url=" + url + " cd=" + contentDisposition + " ct=" + mimeType, e);
            Toast.makeText(this, "다운로드 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean handleSpecialSchemes(String url) {
        if (url == null || url.length() == 0) return false;

        Uri uri;
        try {
            uri = Uri.parse(url);
        } catch (Exception e) {
            Log.e("WV_SCHEME", "Bad url: " + url, e);
            return false;
        }

        String scheme = (uri.getScheme() == null) ? "" : uri.getScheme().toLowerCase();

        // http/https는 WebView가 처리
        if ("http".equals(scheme) || "https".equals(scheme)) return false;

        // 외부앱으로 나갈 가능성이 큰 순간 → 팝업 덮개 먼저 정리(흰 화면 방지)
        safeDismissPopupOverlay();

        try {
            // =========================
            // 1) intent:// (딥링크 최종 형태)
            // =========================
            if (url.startsWith("intent://")) {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                String pkg = intent.getPackage();
                Log.d("WV_INTENT", "pkg=" + pkg + " url=" + url);

                // ★ resolveActivity 믿지 말고 일단 실행 시도
                try {
                    startActivity(intent);
                    return true;
                } catch (Exception startFail) {
                    Log.w("WV_INTENT", "startActivity(intent) failed. try fallback. " + startFail);

                    // (A) browser_fallback_url 있으면 그걸로
                    String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                    if (fallbackUrl != null && fallbackUrl.startsWith("http")) {
                        try {
                            webview.loadUrl(fallbackUrl);
                            return true;
                        } catch (Exception ignored) {}
                    }

                    // (B) cloudiumlink 스킴으로 한 번 더 재시도 (설치돼 있는데도 마켓 튀는 케이스 방지)
                    // intent://cyberdigm?...#Intent;scheme=cloudiumlink;package=...;end
                    // -> cloudiumlink://cyberdigm?...
                    try {
                        String retryUrl = url.replaceFirst("^intent://", "cloudiumlink://");
                        Intent retry = new Intent(Intent.ACTION_VIEW, Uri.parse(retryUrl));
                        retry.addCategory(Intent.CATEGORY_BROWSABLE);

                        // 패키지가 명시돼 있으면 패키지 고정 (브라우저로 새는 거 방지)
                        if (pkg != null && pkg.length() > 0) {
                            retry.setPackage(pkg);
                        }

                        startActivity(retry);
                        return true;
                    } catch (Exception retryFail) {
                        Log.w("WV_INTENT", "retry cloudiumlink failed. " + retryFail);
                    }

                    // (C) 그래도 안 되면 마켓
                    if (pkg != null && pkg.length() > 0) {
                        openMarket(pkg);
                        return true;
                    }

                    Toast.makeText(this, "이 링크를 열 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }

            // =========================
            // 2) cloudiumlink:// 직접 들어오는 케이스
            // =========================
            if ("cloudiumlink".equals(scheme)) {
                Log.d("WV_CLOUDIUM", "url=" + url);

                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    i.addCategory(Intent.CATEGORY_BROWSABLE);

                    // 클라우디움 패키지로 고정(브라우저/스토어 우회 방지)
                    i.setPackage("kr.co.cyberdigm.cloudium");

                    startActivity(i);
                    return true;
                } catch (Exception e) {
                    Log.w("WV_CLOUDIUM", "cloudiumlink open failed -> market", e);
                    openMarket("kr.co.cyberdigm.cloudium");
                    return true;
                }
            }

            // =========================
            // 3) market://, tel:, mailto:, sms: 등 일반 커스텀 스킴
            // =========================
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            i.addCategory(Intent.CATEGORY_BROWSABLE);
            i.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
            startActivity(i);
            return true;

        } catch (Exception e) {
            Log.e("WV_SCHEME", "No handler for: " + url, e);
            Toast.makeText(this, "이 링크를 열 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT).show();
            return true; // WebView가 더 처리하지 않게
        }
    }


    private void safeDismissPopupOverlay() {
        try {
            if (popupWebView != null) {
                closePopup(); // 너가 만든 개선된 closePopup 사용
            } else if (popupContainer != null) {
                popupContainer.removeAllViews();
                popupContainer.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {}
    }

    private void openMarket(String pkg) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + pkg)));
        }
    }
}
