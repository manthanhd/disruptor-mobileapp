package it.axant.axemas;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Stack;

import it.axant.axemas.libs.AnimationLayout;
import it.axant.axemas.libs.ConnectionManager;


public class AXMActivity extends Activity implements SectionFragment.SectionFragmentActivity, AnimationLayout.Listener {

    // Sidebar -----------
    protected AnimationLayout animationLayout;

    @Override
    public void onSidebarOpened() {
        // do something after opening the sidebar
    }

    @Override
    public void onSidebarClosed() {
        // do something after closing the sidebar
    }

    @Override
    public boolean onContentTouchedWhenOpening() {
        // sidebar is going to be closed, do something with the data here
        animationLayout.closeSidebar();
        return false;
    }

    protected void toggleSidebar(boolean visible) {
        if (visible)
            animationLayout.openSidebar();
        else
            animationLayout.closeSidebar();
    }
    // ------------------------

    // Action Bar -------
    private TextView actionBarTitle = null;
    private ImageButton actionBarButton = null;

    private void replaceActionBar(String title) {
        ActionBar mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        View view = LayoutInflater.from(this).inflate(R.layout.axemas_action_bar, null);
        actionBarTitle = (TextView) view.findViewById(R.id.action_bar_title);
        actionBarTitle.setText(title);

        actionBarButton = (ImageButton) view.findViewById(R.id.action_bar_button);
        actionBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animationLayout.toggleSidebar();
            }
        });

        mActionBar.setCustomView(view);
        mActionBar.setDisplayShowCustomEnabled(true);
    }

    protected void setSideBarIcon(String resourceName) {
        actionBarButton.setImageResource(getResources().getIdentifier(resourceName, "drawable", getPackageName()));
    }

    protected void setTitle(String title) {
        actionBarTitle.setText(title);
    }

    protected void sidebarButtonVisibility(boolean visible) {
        if (visible)
            actionBarButton.setVisibility(View.VISIBLE);
        else
            actionBarButton.setVisibility(View.GONE);
    }
    // ----------------------------

    private HashMap<String, Class> registeredControllers = null;
    private boolean backButtonEnabled = true;


    // Connectivity Manger -------------
    private boolean showConnectivityDialog;

    private void triggerConnectivityDialog() {
        if (showConnectivityDialog)
            ConnectionManager.showConnectivityDialog(this);
        else
            ConnectionManager.hideConnectivityDialog(this);
    }

    private BroadcastReceiver connectionManagerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showConnectivityDialog = !intent.getExtras().getBoolean("isConnectionAvailable");
            triggerConnectivityDialog();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        isActivityShowing = true;
        LocalBroadcastManager.getInstance(this).registerReceiver(connectionManagerReceiver, new IntentFilter("connection-manager-status-change"));
        showConnectivityDialog = !ConnectionManager.isNetworkAvailable(this);
        triggerConnectivityDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityShowing = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(connectionManagerReceiver);
    }
    //--------------------------------------

    // Progress dialog ---------------------
    private ProgressDialog progressDialog = null;

    protected void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this, R.style.ProgressDialogTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        }
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    protected void hideProgressDialog() {
        if (progressDialog != null)
            progressDialog.cancel();
    }

    @Override
    protected void onStop() {
        //prevent memory leaks when closing with dialog opened
        hideProgressDialog();
        if (alertDialog != null)
            alertDialog.cancel();
        super.onStop();
    }
    //----------------------------------------

    // Alert dialog --------------------------
    private boolean isActivityShowing = false;
    private AlertDialog alertDialog = null;

    private void makeAndShowAlertDialog(AlertDialog.Builder builder) {
        if (isActivityShowing) {
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    protected void showDismissibleAlertDialog(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(
                this, R.style.CustomAlertDialogStyle));
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(getString(R.string.dialog_close_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        hideProgressDialog(); // hide it when displaying an alert!
                    }
                });
        makeAndShowAlertDialog(alertDialogBuilder);
    }

    protected void showCustomAlertDialog(AlertDialog.Builder builder) {
        makeAndShowAlertDialog(builder);
    }
    //----------------------------------------

    // Fragment Stack ------------------------
    private Stack<Fragment> fragmentStack;

    private void animateTransaction(FragmentTransaction transaction) {
        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                android.R.animator.fade_in, android.R.animator.fade_out);
    }

    private void pushFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        animateTransaction(transaction);

        transaction.add(R.id.currentSection, fragment, tag);
        if (fragmentStack.size() > 0) {
            fragmentStack.lastElement().onPause();
            transaction.hide(fragmentStack.lastElement());
        }
        fragmentStack.push(fragment);

        transaction.commit();
    }

    private void popFragments(int fragmentsToPop) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        if (fragmentStack.size() > 0) { // pause last showing element
            fragmentStack.lastElement().onPause();
            transaction.hide(fragmentStack.lastElement());
        }

        int limit = Math.min(Math.max(0, fragmentsToPop), fragmentStack.size());
        for (int i = 0; i < limit; i++)
            transaction.remove(fragmentStack.pop());

        transaction.commit();
    }

    private void popFragmentsAndMaintain(int maintainedFragments) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        if (fragmentStack.size() > 0) { // pause last showing element
            fragmentStack.lastElement().onPause();
            transaction.hide(fragmentStack.lastElement());
        }

        while (fragmentStack.size() > maintainedFragments)
            transaction.remove(fragmentStack.pop());

        transaction.commit();
    }
    //----------------------------------------

    protected void enableBackButton(boolean toggle) {
        backButtonEnabled = toggle;
    }

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {

        if (backButtonEnabled) {
            if (animationLayout.isOpening()) {
                animationLayout.closeSidebar();
            } else {
                if (fragmentStack.size() > 1) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    animateTransaction(transaction);

                    fragmentStack.lastElement().onPause();
                    transaction.remove(fragmentStack.pop());
                    fragmentStack.lastElement().onResume();
                    transaction.show(fragmentStack.lastElement());

                    transaction.commit();
                } else {
                    if (doubleBackToExitPressedOnce) {
                        super.onBackPressed();
                    }
                    this.doubleBackToExitPressedOnce = true;
                    Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doubleBackToExitPressedOnce = false;
                        }
                    }, 2000);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replaceActionBar("");
        setContentView(R.layout.activity_with_sidebar_axm);

        fragmentStack = new Stack<Fragment>();  // this is retained just check it

        this.registeredControllers = new HashMap<String, Class>();
        animationLayout = (AnimationLayout) findViewById(R.id.animation_layout);   // this is retained just check it
        animationLayout.setListener(this);
    }

    public void activityLoadContent(JSONObject dataObject) {
        loadContent(dataObject);
    }

    private void setupWithActions(JSONObject jsonData) throws JSONException {
        if (!jsonData.isNull("title"))
            setTitle(jsonData.getString("title"));
        else
            setTitle("");

        if (jsonData.isNull("toggleSidebarIcon"))
            sidebarButtonVisibility(false);
        else {
            setSideBarIcon(jsonData.getString("toggleSidebarIcon"));
            sidebarButtonVisibility(true);
        }

        if (!jsonData.isNull("stackMaintainedElements")) {
            popMaintainingOnly(jsonData.getInt("stackMaintainedElements"));
        }
        if (!jsonData.isNull("stackPopElements")) {
            popOnly(jsonData.getInt("stackPopElements"));
        }
    }

    protected void makeApplicationRootController(JSONObject dataObject, String sidebarUrl) {
        SectionFragment sectionFragment = null;
        try {
            setupWithActions(dataObject);
            sectionFragment = SectionFragment.newInstance(dataObject.getString("url"), !dataObject.isNull("toggleSidebarIcon"));

            pushFragment(sectionFragment, "web_fragment");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        SectionFragment sidebarFragment = SectionFragment.newInstance(sidebarUrl, !dataObject.isNull("toggleSidebarIcon"));
        FragmentTransaction sidebarTransaction = getFragmentManager().beginTransaction();
        sidebarTransaction.replace(R.id.sidebarSection, sidebarFragment, "sidebar_fragment");
        sidebarTransaction.commit();
    }

    protected void makeApplicationRootController(JSONObject dataObject) {
        SectionFragment sectionFragment = null;
        try {
            setupWithActions(dataObject);
            sectionFragment = SectionFragment.newInstance(dataObject.getString("url"), !dataObject.isNull("toggleSidebarIcon"));

            pushFragment(sectionFragment, "web_fragment");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void loadContent(JSONObject dataObject) {
        SectionFragment sectionFragment = null;
        try {
            setupWithActions(dataObject);
            sectionFragment = SectionFragment.newInstance(dataObject.getString("url"), !dataObject.isNull("toggleSidebarIcon"));
            pushFragment(sectionFragment, "web_fragment");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void loadContent(Fragment fragment) {
        pushFragment(fragment, "native_fragment");
    }

    protected void makeApplicationRootController(Fragment fragment, String sidebarUrl) {
        pushFragment(fragment, "native_fragment");

        SectionFragment sidebarFragment = SectionFragment.newInstance(sidebarUrl, true);
        FragmentTransaction sidebarTransaction = getFragmentManager().beginTransaction();
        sidebarTransaction.replace(R.id.sidebarSection, sidebarFragment, "sidebar_fragment");
        sidebarTransaction.commit();
    }

    private void popOnly(int fragmentsToPop) {
        setTitle("");
        popFragments(fragmentsToPop);
    }

    private void popMaintainingOnly(int maintainedFragments) {
        setTitle("");
        popFragmentsAndMaintain(maintainedFragments);
    }

    protected void registerController(Class controllerClass, String route) {
        registeredControllers.put(route, controllerClass);
    }

    public Class getControllerForRoute(String route) {
        try {
            return registeredControllers.get(route);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

}
