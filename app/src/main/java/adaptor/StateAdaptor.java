package adaptor;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import com.aryan.anyservice.R;
import com.transferwise.sequencelayout.SequenceAdapter;
import com.transferwise.sequencelayout.SequenceStep;

import java.util.List;

import helper.OrderStates;

public class StateAdaptor extends SequenceAdapter<OrderStates> {
    List<OrderStates> states;
    public StateAdaptor(List<OrderStates> states){
        this.states = states;
    }
    @Override
    public void bindView(final SequenceStep sequenceStep, final OrderStates o) {
            sequenceStep.setActive(o.isActive());
            sequenceStep.setAnchor(o.getAnchor());
            sequenceStep.setTitle(o.getTitle());
            sequenceStep.setSubtitle(o.getSubTitle());
            if(o.isActive()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sequenceStep.setTitleTextAppearance(android.R.style.TextAppearance_Material_Title);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sequenceStep.setSubtitleTextAppearance(android.R.style.TextAppearance_Material_SearchResult_Subtitle);
                }
            }
            sequenceStep.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) sequenceStep.getContext().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(o.getTitle(),o.getSubTitle());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(sequenceStep.getContext(), "Text copied to clipboard.", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        sequenceStep.setAnchorTextAppearance(R.style.verysmall);
    }

    @Override
    public int getCount() {
        return states.size();
    }

    @Override
    public OrderStates getItem(int i) {
        return states.get(i);
    }
}
