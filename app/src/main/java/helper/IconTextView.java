package helper;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class IconTextView extends androidx.appcompat.widget.AppCompatButton {

    private Context context;

    public IconTextView(Context context) {
        super(context);
        this.context = context;
        createView();
    }

    public IconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        createView();
    }

    private void createView(){
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "FontAwesome.ttf");
        setTypeface(font);
    }
}