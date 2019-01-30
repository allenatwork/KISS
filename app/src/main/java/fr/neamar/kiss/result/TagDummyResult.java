package fr.neamar.kiss.result;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.R;
import fr.neamar.kiss.pojo.TagDummyPojo;
import fr.neamar.kiss.utils.FuzzyScore;

public class TagDummyResult extends Result {
    TagDummyResult(@NonNull TagDummyPojo pojo) {
        super(pojo);
    }

    @Override
    public Drawable getDrawable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(R.drawable.ic_launcher_white);
        }
        return context.getResources().getDrawable(R.drawable.ic_launcher_white);
    }

    @Override
    public View display(Context context, int position, View convertView, FuzzyScore fuzzyScore) {
        return null;
    }

    @Override
    protected void doLaunch(Context context, View v) {
        if (context instanceof MainActivity) {
            ((MainActivity) context).showMatchingTags(pojo.getName());
        }
    }
}
