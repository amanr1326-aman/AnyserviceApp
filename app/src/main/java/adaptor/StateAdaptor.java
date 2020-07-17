package adaptor;

import android.os.Build;

import com.aryan.anyservice.R;
import com.google.android.material.resources.TextAppearance;
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
    public void bindView(SequenceStep sequenceStep, OrderStates o) {
            sequenceStep.setActive(o.isActive());
            sequenceStep.setAnchor(o.getAnchor());
            sequenceStep.setTitle(o.getTitle());
            sequenceStep.setSubtitle(o.getSubTitle());
//            setAnchorTextAppearance(...)
            if(o.isActive()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sequenceStep.setTitleTextAppearance(android.R.style.TextAppearance_Material_Title);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sequenceStep.setSubtitleTextAppearance(android.R.style.TextAppearance_Material_SearchResult_Subtitle);
                }
            }

        sequenceStep.setAnchorTextAppearance(R.style.verysmall);
//            setTitleTextAppearance()
//            setSubtitle(...)
//            setSubtitleTextAppearance(...)

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
