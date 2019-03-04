package fr.neamar.kiss.result;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;

import androidx.annotation.NonNull;
import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.R;
import fr.neamar.kiss.pojo.TagDummyPojo;
import fr.neamar.kiss.utils.FuzzyScore;

public class TagDummyResult extends Result {
    private BitmapDrawable mDrawable = null;

    TagDummyResult(@NonNull TagDummyPojo pojo) {
        super(pojo);
    }

    @Override
    public Drawable getDrawable(Context context) {
        if (mDrawable != null)
            return mDrawable;

        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            drawable = context.getDrawable(R.drawable.ic_launcher_white);
        else
            drawable = context.getResources().getDrawable(R.drawable.ic_launcher_white);

        // the drawable should be the same size as the launcher icon
        int width = 10;
        int height = 10;
        if (drawable != null) {
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            width = intrinsicWidth >= 0 ? intrinsicWidth : width;
            height = intrinsicHeight >= 0 ? intrinsicHeight : height;
        }

        // create a canvas from a bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // use StaticLayout to draw the text centered
        TextPaint paint = new TextPaint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(.6f * height);
        StaticLayout staticLayout;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            staticLayout = StaticLayout.Builder
                    .obtain(pojo.getName(), 0, 1, paint, canvas.getWidth())
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .build();
        } else {
            staticLayout = new StaticLayout(pojo.getName(), 0, 1, paint, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1f, 0f, true);
        }

        // draw a white rounded background
        paint.setColor(0xFFffffff);
        canvas.drawRoundRect(new RectF(0, 0, width, height), width / 2.4f, height / 2.4f, paint);

        // write text with "transparent" (create a hole in the background)
        paint.setColor(0);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        staticLayout.draw(canvas);

        // keep a reference to the drawable in case we need it again
        mDrawable = new BitmapDrawable(bitmap);
        return mDrawable;
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
