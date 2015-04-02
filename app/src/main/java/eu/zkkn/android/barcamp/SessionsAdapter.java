package eu.zkkn.android.barcamp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import eu.zkkn.android.barcamp.model.Session;

/**
 *
 */
public class SessionsAdapter extends ArrayAdapter<Session> {

    public SessionsAdapter(Context context, List<Session> objects) {
        super(context, R.layout.row_session, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_session, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.tv_name)).setText(getItem(position).name);
        ((TextView) convertView.findViewById(R.id.tv_speaker)).setText(getItem(position).speaker);
        return convertView;
    }
}
