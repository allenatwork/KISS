package fr.neamar.kiss.adapter;

import android.app.DialogFragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neamar.kiss.R;
import fr.neamar.kiss.normalizer.StringNormalizer;
import fr.neamar.kiss.result.AppResult;
import fr.neamar.kiss.result.ContactsResult;
import fr.neamar.kiss.result.PhoneResult;
import fr.neamar.kiss.result.Result;
import fr.neamar.kiss.result.SearchResult;
import fr.neamar.kiss.result.SettingsResult;
import fr.neamar.kiss.result.ShortcutsResult;
import fr.neamar.kiss.searcher.QueryInterface;
import fr.neamar.kiss.ui.ListPopup;
import fr.neamar.kiss.utils.FuzzyScore;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ResultViewHolder> implements SectionIndexer {
    private final QueryInterface parent;
    private FuzzyScore fuzzyScore;

    /**
     * Array list containing all the results currently displayed
     */
    private List<Result> results;

    // Mapping from letter to a position (only used for fast scroll, when viewing app list)
    private HashMap<String, Integer> alphaIndexer = new HashMap<>();
    // List of available sections (only used for fast scroll)
    private String[] sections = new String[0];

    public RecordAdapter(QueryInterface parent, ArrayList<Result> results) {
        this.parent = parent;
        this.results = results;
        this.fuzzyScore = null;
        setHasStableIds(true);
    }


    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case 0:
                view = inflater.inflate(R.layout.item_app, parent,false);
                break;
            case 1:
                view = inflater.inflate(R.layout.item_search, parent,false);
                break;
            case 2:
                view = inflater.inflate(R.layout.item_contact, parent,false);
                break;
            case 3:
                view = inflater.inflate(R.layout.item_setting, parent,false);
                break;
            case 4:
                view = inflater.inflate(R.layout.item_phone, parent,false);
                break;
            case 5:
                view = inflater.inflate(R.layout.item_shortcut, parent,false);
                break;

        }
        if (view != null) return new ResultViewHolder(view);
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        holder.bindData(results.get(position),fuzzyScore);
    }


    @Override
    public int getItemViewType(int position) {
        if (results.get(position) instanceof AppResult)
            return 0;
        else if (results.get(position) instanceof SearchResult)
            return 1;
        else if (results.get(position) instanceof ContactsResult)
            return 2;
        else if (results.get(position) instanceof SettingsResult)
            return 3;
        else if (results.get(position) instanceof PhoneResult)
            return 4;
        else if (results.get(position) instanceof ShortcutsResult)
            return 5;
        else
            return -1;
    }

    @Override
    public long getItemId(int position) {
        // In some situation, Android tries to display an item that does not exist (e.g. item 24 in a list containing 22 items)
        // See https://github.com/Neamar/KISS/issues/890
        return position < results.size() ? results.get(position).getUniqueId() : -1;
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public void onLongClick(final int pos, View v) {
        ListPopup menu = results.get(pos).getPopupMenu(v.getContext(), this, v);

        // check if menu contains elements and if yes show it
        if (menu.getAdapter().getCount() > 0) {
            parent.registerPopup(menu);
            menu.show(v);
        }
    }

    public void onClick(final int position, View v) {
        final Result result;

        try {
            result = results.get(position);
            result.launch(v.getContext(), v, parent);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
    }

    public void removeResult(Context context, Result result) {
        results.remove(result);
        notifyDataSetChanged();
        // Do not reset scroll, we want the remaining items to still be in view
        parent.temporarilyDisableTranscriptMode();
    }

    public void updateResults(List<Result> results, boolean isRefresh, String query) {
        this.results.clear();
        this.results.addAll(results);
        StringNormalizer.Result queryNormalized = StringNormalizer.normalizeWithResult(query, false);

        fuzzyScore = new FuzzyScore(queryNormalized.codePoints, true);
        notifyDataSetChanged();

        if (isRefresh) {
            // We're refreshing an existing dataset, do not reset scroll!
            parent.temporarilyDisableTranscriptMode();
        }
    }

    /**
     * Force set transcript mode on the list.
     * Prefer to use `parent.temporarilyDisableTranscriptMode();`
     */
    public void updateTranscriptMode(int transcriptMode) {
        parent.updateTranscriptMode(transcriptMode);
    }


    public void clear() {
        this.results.clear();
        notifyDataSetChanged();
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sections.length == 0) {
            return 0;
        }

        // In some rare situations, the system will ask for a section
        // that does not exist anymore.
        // It's likely there is a threading issue in our code somewhere,
        // But I was unable to find where, so the following line is a quick and dirty fix.
        sectionIndex = Math.max(0, Math.min(sections.length - 1, sectionIndex));
        return alphaIndexer.get(sections[sectionIndex]);
    }

    @Override
    public int getSectionForPosition(int position) {
        for (int i = 0; i < sections.length; i++) {
            if (alphaIndexer.get(sections[i]) > position) {
                return i - 1;
            }
        }

        // If apps starting with the letter "A" cover more than a full screen,
        // we will never get > position
        // so we just return the before-last section
        // See #1005
        return sections.length - 2;
    }

    public void showDialog(DialogFragment dialog) {
        parent.showDialog(dialog);
    }

    public static class ResultViewHolder extends RecyclerView.ViewHolder {
        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bindData(Result result, FuzzyScore fuzzyScore) {
            result.display(itemView.getContext(), itemView, fuzzyScore);
        }
    }
}
