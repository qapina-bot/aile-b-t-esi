package com.ailebutcesi.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Aile Bütçesi: uygulamanın kendisi assets/index.html içindedir; bu sınıf onu tam ekran bir WebView'da açar.
 * Veriler WebView'un yerel deposunda (localStorage) telefonda kalır. İnternet izni yoktur.
 * - Yedek indirme: sayfa AndroidBridge.saveFile(...) çağırır, telefonun "Kaydet" ekranı açılır.
 * - Yedekten geri yükleme: sayfadaki dosya seçici, telefonun "Dosya seç" ekranını açar.
 * - Geri tuşu: önce sayfadaki açık pencere/formu kapatır.
 */
public class MainActivity extends Activity {
    private static final int REQ_SAVE = 41;
    private static final int REQ_OPEN = 42;

    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private String pendingData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setTextZoom(100);
        s.setAllowContentAccess(true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if ("file".equals(u.getScheme())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) { }
                return true;
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                try {
                    startActivityForResult(i, REQ_OPEN);
                } catch (Exception e) {
                    fileCallback = null;
                    return false;
                }
                return true;
            }
        });

        web.addJavascriptInterface(new Bridge(), "AndroidBridge");
        web.loadUrl("file:///android_asset/index.html");
        setContentView(web);
    }

    /** Sayfadan çağrılır (JavaScript tarafında window.AndroidBridge). */
    private class Bridge {
        @JavascriptInterface
        public void saveFile(final String name, final String mime, final String data) {
            runOnUiThread(() -> {
                pendingData = data;
                Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType(mime);
                i.putExtra(Intent.EXTRA_TITLE, name);
                try {
                    startActivityForResult(i, REQ_SAVE);
                } catch (Exception e) {
                    pendingData = null;
                    reportSaved("fail");
                }
            });
        }
    }

    private void reportSaved(String result) {
        web.evaluateJavascript("window.__androidSaved && window.__androidSaved('" + result + "')", null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_OPEN) {
            if (fileCallback != null) {
                Uri[] result = null;
                if (resultCode == RESULT_OK && data != null && data.getData() != null) result = new Uri[]{data.getData()};
                fileCallback.onReceiveValue(result);
                fileCallback = null;
            }
        } else if (requestCode == REQ_SAVE) {
            String d = pendingData;
            pendingData = null;
            if (resultCode != RESULT_OK || data == null || data.getData() == null || d == null) {
                reportSaved("declined");
                return;
            }
            try (OutputStream os = getContentResolver().openOutputStream(data.getData(), "wt")) {
                if (os == null) throw new java.io.IOException("no stream");
                os.write(d.getBytes(StandardCharsets.UTF_8));
                reportSaved("ok");
            } catch (Exception e) {
                reportSaved("fail");
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        web.evaluateJavascript("window.__androidBack ? window.__androidBack() : false", value -> {
            if (!"true".equals(value)) exitApp();
        });
    }

    @SuppressWarnings("deprecation")
    private void exitApp() { super.onBackPressed(); }

    @Override
    protected void onPause() {
        super.onPause();
        // localStorage'ın diske yazıldığından emin ol
        android.webkit.CookieManager.getInstance().flush();
    }
}
