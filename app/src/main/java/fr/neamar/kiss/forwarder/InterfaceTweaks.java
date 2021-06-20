package fr.neamar.kiss.forwarder;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.List;

import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.R;
import fr.neamar.kiss.UIColors;
import fr.neamar.kiss.utils.ViewGroupUtils;

// Deals with any settings in the "User Interface" setting sub-screen
class InterfaceTweaks extends Forwarder {
    InterfaceTweaks(MainActivity mainActivity) {
        super(mainActivity);

        // Setting the theme needs to be done before setContentView()
        String theme = prefs.getString("theme", "transparent");
        switch (theme) {
            case "dark":
                mainActivity.setTheme(R.style.AppThemeDark);
                break;
            case "transparent":
                mainActivity.setTheme(R.style.AppThemeTransparent);
                break;
            case "semi-transparent":
                mainActivity.setTheme(R.style.AppThemeSemiTransparent);
                break;
            case "semi-transparent-dark":
                mainActivity.setTheme(R.style.AppThemeSemiTransparentDark);
                break;
            case "transparent-dark":
                mainActivity.setTheme(R.style.AppThemeTransparentDark);
                break;
            case "amoled-dark":
                mainActivity.setTheme(R.style.AppThemeAmoledDark);
                break;
        }

        UIColors.applyOverlay(mainActivity, prefs);

        mainActivity.getTheme().applyStyle(prefs.getBoolean("small-results", false) ? R.style.OverlayResultSizeSmall : R.style.OverlayResultSizeStandard, true);
    }

    void onCreate() {
        UIColors.updateThemePrimaryColor(mainActivity);
        applyRoundedCorners(mainActivity);

        // Transparent Search and Favorites bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (prefs.getBoolean("transparent-search", false)) {
                mainActivity.findViewById(R.id.searchEditLayout).setBackgroundResource(android.R.color.transparent);
                mainActivity.searchEditText.setBackgroundResource(android.R.color.transparent);

                // get theme shadow color
                int shadowColor = getSearchBackgroundColor();

                // make shadow color intense
                float[] hsv = new float[3];
                Color.colorToHSV(shadowColor, hsv);
                // if color is close to black, make it black
                hsv[2] = hsv[2] < 0.5f ? 0f : 1f;
                shadowColor = Color.HSVToColor(hsv);
                mainActivity.searchEditText.setShadowLayer(3, 1, 2, shadowColor);
            }
        }

        // Notification drawer icon color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (prefs.getBoolean("black-notification-icons", false)) {
                // Apply the flag to any view, so why not the edittext!
                mainActivity.searchEditText.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        if (prefs.getBoolean("pref-hide-search-bar-hint", false)) {
            mainActivity.searchEditText.setHint("");
        }
    }

    void onResume() {
        boolean largeSearchBar = prefs.getBoolean("large-search-bar", false);
        Resources res = mainActivity.getResources();
        int searchHeight;
        if (largeSearchBar) {
            searchHeight = res.getDimensionPixelSize(R.dimen.large_bar_height);
        } else {
            searchHeight = res.getDimensionPixelSize(R.dimen.bar_height);
        }

        mainActivity.findViewById(R.id.searchEditLayout).getLayoutParams().height = searchHeight;
    }

    private void applyRoundedCorners(MainActivity mainActivity) {
        if (prefs.getBoolean("pref-rounded-bars", true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mainActivity.findViewById(R.id.searchEditLayout).setBackgroundResource(R.drawable.rounded_search_bar);
            } else {
                // Before API21, you can't access values from current theme using ?attr/
                // So we made different drawable for each theme (#931).
                Resources res = mainActivity.getResources();

                if (getSearchBackgroundColor() == Color.WHITE) {
                    mainActivity.findViewById(R.id.searchEditLayout).setBackgroundResource(R.drawable.rounded_search_bar_pre21_light);
                } else if (getSearchBackgroundColor() == res.getColor(R.color.kiss_background_light_transparent)) {
                    mainActivity.findViewById(R.id.searchEditLayout).setBackgroundResource(R.drawable.rounded_search_bar_pre21_semi_trans_light);
                } else if (getSearchBackgroundColor() == res.getColor(R.color.kiss_background_dark_transparent)) {
                    mainActivity.findViewById(R.id.searchEditLayout).setBackgroundResource(R.drawable.rounded_search_bar_pre21_semi_trans_dark);
                } else if (getSearchBackgroundColor() == Color.BLACK) {
                    mainActivity.findViewById(R.id.searchEditLayout).setBackgroundResource(R.drawable.rounded_search_bar_pre21_amoled);
                } else {
                    mainActivity.findViewById(R.id.searchEditLayout).setBackgroundResource(R.drawable.rounded_search_bar_pre21_dark);
                }
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Tinting is not properly applied pre lollipop if there is no solid background, so we need to manually set the background color
        }

        if (prefs.getBoolean("pref-rounded-list", false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mainActivity.findViewById(R.id.resultLayout).setBackgroundResource(R.drawable.rounded_result_layout);
                // clip list content to rounded corners
                mainActivity.listContainer.setClipToOutline(true);
            } else {
                // Before API21, you can't access values from current theme using ?attr/
                // So we made different drawable for each theme (#931).
                Resources res = mainActivity.getResources();

                if (getSearchBackgroundColor() == Color.WHITE)
                    mainActivity.findViewById(R.id.resultLayout).setBackgroundResource(R.drawable.rounded_result_layout_pre21_light);
                else if (getSearchBackgroundColor() == res.getColor(R.color.kiss_background_light_transparent))
                    mainActivity.findViewById(R.id.resultLayout).setBackgroundResource(R.drawable.rounded_result_layout_pre21_semi_trans_light);
                else if (getSearchBackgroundColor() == res.getColor(R.color.kiss_background_dark_transparent))
                    mainActivity.findViewById(R.id.resultLayout).setBackgroundResource(R.drawable.rounded_result_layout_pre21_semi_trans_dark);
                else if (getSearchBackgroundColor() == Color.BLACK)
                    mainActivity.findViewById(R.id.resultLayout).setBackgroundResource(R.drawable.rounded_result_layout_pre21_amoled);
                else
                    mainActivity.findViewById(R.id.resultLayout).setBackgroundResource(R.drawable.rounded_result_layout_pre21_dark);
            }
        }
    }

    private int getSearchBackgroundColor() {
        // get theme shadow color
        int[] attrs = new int[]{R.attr.searchBackgroundColor /* index 0 */};
        TypedArray ta = mainActivity.obtainStyledAttributes(attrs);
        int shadowColor = ta.getColor(0, Color.BLACK);
        ta.recycle();
        return shadowColor;
    }

    private boolean isExternalFavoriteBarEnabled() {
        return prefs.getBoolean("enable-favorites-bar", true);
    }
}
