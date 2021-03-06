package com.dyanote.android.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.BulletSpan;
import android.text.style.ClickableSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dyanote.android.activity.BrowseNotesActivity;
import com.dyanote.android.Note;
import com.dyanote.android.NoteConversionTools;
import com.dyanote.android.R;

public class NoteFragment extends Fragment {

    public static NoteFragment newInstance(Note note) {
        NoteFragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        if(note == null) {
            Log.e("BrowseNotesActivity", "Can't create note: null");
            return null;
        }
        args.putParcelable("note", note);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View rootView = inflater.inflate(R.layout.fragment_view, container, false);
        update(rootView);
        return rootView;
    }

    // Update fragment content with note.
    public void reload(Note note) {
        Log.i("NoteFragment", "Reloading " + note.toString());
        getArguments().putParcelable("note", note);
        update(getView());
    }

    private void update(View rootView) {
        if(rootView == null) {
            Log.w("NoteFragment", "null rootView");
            return;
        }
        TextView textView = (TextView) rootView.findViewById(R.id.note_text_view);
        final Note note = getArguments().getParcelable("note");

        final BrowseNotesActivity activity = (BrowseNotesActivity) getActivity();
        final Context c = activity.getApplicationContext();

        SpannableNoteConverter converter = new SpannableNoteConverter() {
            @Override
            public void setBold(int start, int end) {
                setSpan(new TextAppearanceSpan(c, R.style.BoldText), start, end, 0);
            }

            @Override
            public void setItalic(int start, int end) {
                setSpan(new TextAppearanceSpan(c, R.style.ItalicText), start, end, 0);
            }

            @Override
            public void setHeader(int start, int end) {
                setSpan(new TextAppearanceSpan(c, R.style.HeaderText), start, end, 0);
                append('\n');
            }

            @Override
            public void setLink(int start, int end, final Long id) {
                setSpan(new TextAppearanceSpan(c, R.style.LinkText), start, end, 0);
                setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        Log.i("Click!", "Open note " + id);
                        activity.onLinkClicked(id);
                    }
                }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                // TODO: Save span coordinates.
            }

            @Override
            public void addNewline() {
                append("\n");
            }

            @Override
            public void setBullet(int start, int end) {
                setSpan(new BulletSpan(10), start, end, 0);
                append('\n');
            }
        };
        NoteConversionTools.convert(note, converter);
        textView.setText(converter);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // SpannableNoteConverter is a NoteConverter which allows to add special formatting.
    // This is used to convert a Note xml to a representation compatible with a TextView.
    abstract static class SpannableNoteConverter extends SpannableStringBuilder implements NoteConversionTools.NoteConverter {

        public abstract void setBold(int start, int end);

        public abstract void setItalic(int start, int end);

        public abstract void setHeader(int start, int end);

        public abstract void setLink(int start, int end, Long href);
    }
}
