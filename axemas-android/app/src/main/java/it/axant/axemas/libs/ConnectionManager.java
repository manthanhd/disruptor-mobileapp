package it.axant.axemas.libs;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import it.axant.axemas.NavigationSectionsManager;

public class ConnectionManager extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Bundle params = new Bundle();
        params.putBoolean("isConnectionAvailable", checkConnection(context));
        sendLocalBroadcast(context, "connection-manager-status-change", params);
    }

    private void sendLocalBroadcast(Context context, String eventName, Bundle params) {
        Intent notifications = new Intent(eventName)
                .putExtras(params);
        LocalBroadcastManager.getInstance(context).sendBroadcast(notifications);
    }

    private static AlertDialog dialog = null;

    public static void showConnectivityDialog(Context context) {
        if (dialog == null)
            dialog = new AlertDialog.Builder(context)
                    .setTitle("No Available Connection")
                    .setMessage("The application requires a working internet connection," +
                            " the following dialog will disappear when connection is available")
                    .setCancelable(false)
                    .create();
        dialog.show();
    }

    public static void hideConnectivityDialog(Context context) {
        if (dialog != null) {
            NavigationSectionsManager.hideProgressDialog(context);
            dialog.hide();
        }
    }

    private static boolean checkConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.isAvailable();
    }

    public static boolean isNetworkAvailable(Context context) {
        return checkConnection(context);
    }
}