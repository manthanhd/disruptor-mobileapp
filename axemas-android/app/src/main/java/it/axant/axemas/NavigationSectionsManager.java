package it.axant.axemas;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;

import org.json.JSONObject;

public class NavigationSectionsManager {

    public static void  registerController(Context context,Class controllerClass, String route) {
        ((AXMActivity) context).registerController(controllerClass, route);
    }

    public static void makeApplicationRootController(Context context, JSONObject data, String sidebarUrl) {
        ((AXMActivity) context).makeApplicationRootController(data, sidebarUrl);
    }

    public static void setSideBarIcon(Context context, String resourceName) {
        ((AXMActivity) context).setSideBarIcon(resourceName);
    }

    public static void makeApplicationRootController(Context context, Fragment fragment, String sidebarUrl) {
        ((AXMActivity) context).makeApplicationRootController(fragment, sidebarUrl);
    }

    public static void makeApplicationRootController(Context context, JSONObject data) {
        ((AXMActivity) context).makeApplicationRootController(data);
    }

    public static void goTo(Context context, JSONObject data) {
        ((AXMActivity) context).loadContent(data);
    }

    public static void pushFragment(Context context, Fragment fragment) {
        ((AXMActivity) context).loadContent(fragment);
    }

    public static void sidebarButtonVisibility(Context context, boolean visible) {
        ((AXMActivity) context).sidebarButtonVisibility(visible);
    }

    public static void toggleSidebar(Context context, boolean visible) {
        ((AXMActivity) context).toggleSidebar(visible);
    }

    public static void showProgressDialog(Context context) {
        ((AXMActivity) context).showProgressDialog();
    }

    public static void hideProgressDialog(Context context) {
        ((AXMActivity) context).hideProgressDialog();
    }

    public static void showDismissibleAlertDialog(Context context, String title, String message) {
        ((AXMActivity) context).showDismissibleAlertDialog(title, message);
    }

    public static void showDismissibleAlertDialog(Context context, AlertDialog.Builder builder) {
        ((AXMActivity) context).showCustomAlertDialog(builder);
    }

    public static void enableBackButton(Context context, boolean toggle){
        ((AXMActivity) context).enableBackButton(toggle);
    }

}
