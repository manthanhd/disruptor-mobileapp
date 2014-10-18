package it.axant.axemas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


@SuppressLint("ValidFragment")
public class SectionFragment extends Fragment {
    private static final String URL_PARAM = "url";
    private static final String URL_EXTRA_PARAMETERS = "url_extra_parameters";
    private String fullUrl;
    private String url;
    private JavascriptBridge jsbridge = null;
    private AXMSectionController controller = null;

    private SectionFragmentActivity mListener;
    // retain values
    private String fragmentTitle = null;
    private boolean showSidebarMenu = false;

    public static SectionFragment newInstance(String url, boolean showSidebarMenu) {
        SectionFragment fragment = new SectionFragment();
        //decouple url and parameters
        Bundle args = new Bundle();
        if (url.indexOf("?") != -1) {
            args.putString(URL_PARAM, url.substring(0, url.indexOf("?")));
            args.putString(URL_EXTRA_PARAMETERS, url.substring(url.indexOf("?"), url.length()));
        } else {
            args.putString(URL_PARAM, url);
            args.putString(URL_EXTRA_PARAMETERS, "");
        }
        args.putBoolean("showSidebarMenu", showSidebarMenu);
        fragment.setArguments(args);
        return fragment;
    }


    public SectionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //attatch parameters
        if (getArguments() != null) {
            url = getArguments().getString(URL_PARAM);
            showSidebarMenu = getArguments().getBoolean("showSidebarMenu");
            String extra_params = getArguments().getString(URL_EXTRA_PARAMETERS);

            if (!url.contains("://"))
                fullUrl = "file:///android_asset/" + url + extra_params;
            else
                fullUrl = url + extra_params;
        }
    }

    public static class Holder {

        private static final Holder INSTANCE = new Holder();

        private Holder() {
        }

        public WebView webView;
        public WebView sideBarWebView;

        public WebView getWebView(String tag) {
            if (tag.equals("sidebar_fragment"))
                return sideBarWebView;
            else
                return webView;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FrameLayout V = new FrameLayout(getActivity());

        WebView webView = null;
        if (Holder.INSTANCE.webView == null)
            webView = new WebView(getActivity());
        else
            webView = Holder.INSTANCE.getWebView(getTag());

        V.addView(webView);

        this.jsbridge = new JavascriptBridge(webView);

        AXMActivity activity = (AXMActivity) this.getActivity();
        try {
            Class controllerClass = activity.getControllerForRoute(url);
            if (controllerClass != null) {
                Constructor c = controllerClass.getConstructor(SectionFragment.class);
                this.controller = (AXMSectionController) c.newInstance(this);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Log.d("axemas", "Controller " + this.controller + " for " + url);
        webView.setWebChromeClient(new SectionChromeClient());
        webView.setWebViewClient(new SectionWebClient(this.jsbridge, this.controller));
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDatabasePath("/data/data/" + getActivity().getPackageName() + "/databases/");
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        webView.addJavascriptInterface(this.jsbridge, "AndroidNativeJS");

        this.jsbridge.registerHandler("showProgressHUD", new JavascriptBridge.Handler() {
            @Override
            public void call(Object data, JavascriptBridge.Callback callback) {
                Log.d("axemas", "showing progress HUD");
                if (getActivity() != null)
                    ((AXMActivity) getActivity()).showProgressDialog();
                callback.call();
            }
        });

        this.jsbridge.registerHandler("hideProgressHUD", new JavascriptBridge.Handler() {
            @Override
            public void call(Object data, JavascriptBridge.Callback callback) {
                Log.d("axemas", "hiding progress HUD");
                if (getActivity() != null)
                    ((AXMActivity) getActivity()).hideProgressDialog();
                callback.call();
            }
        });

        this.jsbridge.registerHandler("goto", new JavascriptBridge.Handler() {
            @Override
            public void call(Object data, JavascriptBridge.Callback callback) {
                JSONObject jsonData = (JSONObject) data;
                Log.d("axemas", jsonData.toString());
                if (mListener != null)
                    mListener.activityLoadContent(jsonData);
                callback.call();
            }
        });

        this.jsbridge.registerHandler("gotoFromSidebar", new JavascriptBridge.Handler() {
            @Override
            public void call(Object data, JavascriptBridge.Callback callback) {
                JSONObject jsonData = (JSONObject) data;
                Log.d("axemas", jsonData.toString());
                if (mListener != null)
                    mListener.activityLoadContent(jsonData);
                if (getActivity() != null)
                    ((AXMActivity) getActivity()).toggleSidebar(false);
                callback.call();
            }
        });

        this.jsbridge.registerHandler("dialog", new JavascriptBridge.Handler() {
            void addDialogButton(AlertDialog.Builder builder, JSONArray buttons,
                                 final int buttonIdx,
                                 final JavascriptBridge.Callback callback) {
                if (buttons == null)
                    return;

                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        JSONObject data = new JSONObject();
                        try {
                            data.put("button", buttonIdx);
                            dialogInterface.dismiss();
                            ((AXMActivity) getActivity()).hideProgressDialog();
                        } catch (JSONException e) {
                            Log.d("axemas", "Failed to create dialog button response");
                            e.printStackTrace();
                        }
                        callback.call(data);
                    }
                };

                String buttonName = buttons.optString(buttonIdx, "");
                if ((buttonName != null) && (!buttonName.equals(""))) {
                    if (buttonIdx == 0 && buttons.length() >= 1)
                        builder.setNegativeButton(buttonName, listener);
                    else if (buttonIdx == 1 && buttons.length() > 2)
                        builder.setNeutralButton(buttonName, listener);
                    else if (buttonIdx == 1 && buttons.length() == 2)
                        builder.setPositiveButton(buttonName, listener);
                    else if (buttonIdx == 2 && buttons.length() > 2)
                        builder.setPositiveButton(buttonName, listener);

                }
            }

            @Override
            public void call(Object data, final JavascriptBridge.Callback callback) {
                JSONObject args = (JSONObject) data;
                String title = args.optString("title", "");
                String message = args.optString("message", "");
                JSONArray buttons = args.optJSONArray("buttons");

                if (mListener != null) { //activity may not be showing
                    AlertDialog.Builder builder = new AlertDialog.Builder((Activity) mListener);
                    builder.setTitle(title);
                    builder.setMessage(message);

                    addDialogButton(builder, buttons, 0, callback);
                    addDialogButton(builder, buttons, 1, callback);
                    addDialogButton(builder, buttons, 2, callback);

                    builder.show();
                }
            }
        });

        webView.loadUrl(fullUrl);

        return V;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.controller.fragmentOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SectionFragmentActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AXMActivity) getActivity()).hideProgressDialog();
        if (fragmentTitle != null)
            ((AXMActivity) getActivity()).setTitle(fragmentTitle);
        if (this.controller != null)
            this.controller.sectionFragmentWillResume();
        if (this.getTag() != null && this.getTag().equals("web_fragment")) {
            if (showSidebarMenu)
                ((AXMActivity) getActivity()).sidebarButtonVisibility(true);
            else
                ((AXMActivity) getActivity()).sidebarButtonVisibility(false);
        }
    }

    @Override
    public void onPause() {
        if (this.controller != null)
            this.controller.sectionFragmentWillPause();
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public JavascriptBridge getJSBridge() {
        return this.jsbridge;
    }

    public interface SectionFragmentActivity {
        public void activityLoadContent(JSONObject dataObject);
    }


    private class SectionWebClient extends WebViewClient {
        private JavascriptBridge bridge;
        private AXMSectionController controller;

        SectionWebClient(JavascriptBridge bridge, AXMSectionController controller) {
            this.bridge = bridge;
            this.controller = controller;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if ( getActivity() != null)
                ((AXMActivity) getActivity()).showProgressDialog();
            if (this.controller != null && getActivity() != null)
                this.controller.sectionWillLoad();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (this.controller != null)
                this.controller.sectionDidLoad();

            if (view != null && view.getTitle() != null && !view.getTitle().contains(".html"))
                if (getActivity() != null) {
                    fragmentTitle = view.getTitle();
                    ((AXMActivity) getActivity()).setTitle(fragmentTitle);
                }

            JSONObject data = new JSONObject();
            try {
                data.put("url", url);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("axemas", "Unable to encode url in ready event");
            }

            this.bridge.callJS("ready", data, new JavascriptBridge.AndroidCallback() {
                public void call(JSONObject data) {
                    Log.d("axemas", "Page handled ready event with: " + data.toString());
                }
            });

            if (getActivity() != null)
                ((AXMActivity) getActivity()).hideProgressDialog();
        }
    }

    private class SectionChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            Log.i("axemas", cm.sourceId() + ":" + cm.lineNumber() + " -> " + cm.message());
            return true;
        }
    }
}

