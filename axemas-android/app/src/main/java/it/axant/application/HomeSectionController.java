package it.axant.application;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import it.axant.axemas.AXMActivity;
import it.axant.axemas.AXMSectionController;
import it.axant.axemas.JavascriptBridge;
import it.axant.axemas.NavigationSectionsManager;
import it.axant.axemas.SectionFragment;


public class HomeSectionController extends AXMSectionController {

    public HomeSectionController(SectionFragment section) {
        super(section);
    }

    @Override
    public void sectionWillLoad() {

    this.section.getJSBridge().registerHandler("openMap", new JavascriptBridge.Handler() {
        @Override
        public void call(Object data, JavascriptBridge.Callback callback) {
            String uri = "https://maps.google.com/maps";
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            section.startActivity(i);
        }
    });

        this.section.getJSBridge().registerHandler("openNativeController", new JavascriptBridge.Handler() {
            @Override
            public void call(Object data, JavascriptBridge.Callback callback) {
                JSONObject datum = new JSONObject();
                try {
                    datum.put("url", "www/index.html");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                NavigationSectionsManager.goTo(section.getActivity(), datum);
            }
        });
    }
}
