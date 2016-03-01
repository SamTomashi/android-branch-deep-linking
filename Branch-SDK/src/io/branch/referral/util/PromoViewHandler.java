package io.branch.referral.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

import io.branch.referral.Branch;

/**
 * <p>
 * Class for managing promotional views with Branch. Keeps track of app promotions and their states
 * Displays the web view and handle the promotion dialog life cycle.
 * </p>
 * Class for Managing
 */
public class PromoViewHandler {
    private static PromoViewHandler thisInstance_;
    private ConcurrentHashMap<String, AppPromoView> promoViewMap_;
    private boolean isPromoDialogShowing;
    private AppPromoView openOrInstallPendingPromo = null;

    public static final int PROMO_VIEW_ERR_ALREADY_SHOWING = -200;


    private PromoViewHandler() {
        promoViewMap_ = new ConcurrentHashMap<>();
    }

    /**
     * Get the singleton instance for PromoViewHandler
     *
     * @return {@link PromoViewHandler} instance
     */
    public static PromoViewHandler getInstance() {
        if (thisInstance_ == null) {
            thisInstance_ = new PromoViewHandler();
        }
        return thisInstance_;
    }

    /**
     * Returns promo view associated with the action specified. Null if there is no Promo view available for action name
     *
     * @param promoAction action name
     * @return {@link io.branch.referral.util.PromoViewHandler.AppPromoView} associated with specified action name
     */
    public AppPromoView getPromoView(String promoAction) {
        boolean isPromoViewAvailable = false;
        AppPromoView promoView = promoViewMap_.get(promoAction);
        if (promoView != null && promoView.isAvailable()) {
            return promoView;
        } else {
            return null;
        }
    }

    public boolean showPendingPromoView(Activity currentActivity) {
        showPromoView(openOrInstallPendingPromo, currentActivity, null);
        return true;
    }

    public boolean showPromoView(final String action, Activity currentActivity, final IPromoViewEvents callback) {
        if (isPromoDialogShowing) {
            if (callback != null) {
                callback.onPromoViewError(PROMO_VIEW_ERR_ALREADY_SHOWING, "Unable to create a promo view. A promo view is already showing");
            }
            return false;
        }

        isPromoDialogShowing = false;
        AppPromoView promoView = getPromoView(action);

        if (currentActivity != null && promoView != null) {
            showPromoView(promoView, currentActivity, callback);
            isPromoDialogShowing = true;
        }

        return isPromoDialogShowing;
    }

    private void showPromoView(final AppPromoView promoView, Activity currentActivity, final IPromoViewEvents callback) {
        if (currentActivity != null && promoView != null) {
            promoView.updateUsageCount();
            isPromoDialogShowing = true;
            WebView webView = new WebView(currentActivity);

            final RelativeLayout layout = new RelativeLayout(currentActivity);
            layout.setVisibility(View.GONE);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(40, 80, 40, 80);

            layout.addView(webView, layoutParams);
            layout.setBackgroundColor(Color.parseColor("#11FEFEFE"));

            final TextView confirmTxt = new TextView(currentActivity);
            confirmTxt.setVisibility(View.GONE);
            confirmTxt.setBackgroundColor(Color.RED);
            confirmTxt.setText("Confirm");
            confirmTxt.setGravity(Gravity.CENTER);
            confirmTxt.setTextAppearance(currentActivity, android.R.style.TextAppearance_Large);
            RelativeLayout.LayoutParams txtViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            txtViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            txtViewLayoutParams.setMargins(40, 0, 40, 30);
            confirmTxt.setPadding(30, 30, 30, 30);
            layout.addView(confirmTxt, txtViewLayoutParams);

            final Dialog dialog = new Dialog(currentActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(layout);
            dialog.show();
            if (callback != null) {
                callback.onPromoViewVisible(promoView.promoAction_);
            }

            if (Build.VERSION.SDK_INT >= 19) {
                webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }


            webView.loadUrl(promoView.webViewUrl_);
            webView.setBackgroundColor(Color.parseColor("#00FE0000"));


            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    layout.setVisibility(View.VISIBLE);
                    confirmTxt.setVisibility(View.VISIBLE);
                    view.setVisibility(View.VISIBLE);
                    dialog.show();
                    showViewWithAlphaTweening(layout);
                    showViewWithAlphaTweening(confirmTxt);
                    showViewWithAlphaTweening(view);
                }
            });

            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    isPromoDialogShowing = false;
                    if (callback != null) {
                        callback.onPromoViewDismissed(promoView.promoAction_);
                    }
                }
            });

            confirmTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
    }

    public void saveAppPromoViews() {
        JSONArray promoViewArray = Branch.getInstance().getPromoViewData();
        if(promoViewArray != null) {
            for (int i = 0; i < promoViewArray.length(); i++) {
                try {
                    AppPromoView appPromoView = new AppPromoView(promoViewArray.getJSONObject(i));
                    promoViewMap_.put(appPromoView.promoAction_, appPromoView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void showViewWithAlphaTweening(View view) {
        AlphaAnimation animation1 = new AlphaAnimation(0.1f, 1.0f);
        animation1.setDuration(500);
        animation1.setStartOffset(10);
        animation1.setInterpolator(new AccelerateInterpolator());
        animation1.setFillAfter(true);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(animation1);
    }

    public static void hideViewWithAlphaTweening(View view) {
        AlphaAnimation animation1 = new AlphaAnimation(1.0f, 0.0f);
        animation1.setDuration(500);
        animation1.setStartOffset(10);
        animation1.setInterpolator(new DecelerateInterpolator());
        animation1.setFillAfter(true);
        view.setVisibility(View.GONE);
        view.startAnimation(animation1);
    }


    public void markInstallOrOpenPromoViewPending(String action) {
        openOrInstallPendingPromo = getPromoView(action);
    }

    public boolean isInstallOrOpenPromoPending() {
        return openOrInstallPendingPromo != null && openOrInstallPendingPromo.num_of_use_ > 0;
    }

    private class AppPromoView {
        private String promoID_ = "";
        private String promoAction_ = "";
        private int num_of_use_ = 1;
        private long expiry_date_ = 0;
        private String webViewUrl_ = "";
        /* This promo view can be used for any number of times in a session. */
        private static final int USAGE_UNLIMITED = -1;

        private AppPromoView(JSONObject promoViewJson) {
            try {
                if (promoViewJson.has("app_promo_id")) {
                    promoID_ = promoViewJson.getString("app_promo_id");
                }
                if (promoViewJson.has("app_promo_action")) {
                    promoAction_ = promoViewJson.getString("app_promo_action");
                }
                if (promoViewJson.has("num_of_use")) {
                    num_of_use_ = promoViewJson.getInt("num_of_use");
                }
                if (promoViewJson.has("expiry")) {
                    expiry_date_ = promoViewJson.getLong("expiry");
                }
                if (promoViewJson.has("promo_view_url")) {
                    webViewUrl_ = promoViewJson.getString("promo_view_url");
                }
            } catch (Exception ignore) {

            }
        }

        private boolean isAvailable() {
            return (System.currentTimeMillis() > expiry_date_)
                    && ((num_of_use_ > 0) || (num_of_use_ == USAGE_UNLIMITED));
        }

        public void updateUsageCount() {
            if (num_of_use_ > 0) {
                num_of_use_--;
            }
        }
    }

    /**
     * Interface for calling back methods on promo dialog lifecycle events
     */
    public interface IPromoViewEvents {
        /**
         * Called when a promotion view dialog is shown
         *
         * @param action action name associated with the AppPromo item
         */
        void onPromoViewVisible(String action);

        /**
         * Called when a promotion view dialog is dismissed
         *
         * @param action action name associated with the Promo view
         */
        void onPromoViewDismissed(String action);

        /**
         * Called when there is an error on creating or showing Promo view
         *
         * @param errorCode {@link Integer} with error code for the issue
         * @param errorMsg  {@link String} with value error message
         */
        void onPromoViewError(int errorCode, String errorMsg);
    }


}
