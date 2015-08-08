package org.romainkolb.randroid2.app.navigationdrawer;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import org.romainkolb.randroid2.app.R;

import java.util.*;

/**
 * Created by romain on 25/07/15.
 */
public class NavigationDrawerAdapter extends BaseExpandableListAdapter {
    private final Context context;

    private List<String> listDataHeader; // header titles

    // child data in format of header title, child title
    private Map<String, List<String>> listDataChild;

    private Map<Integer,Boolean> wasExpanded;

    private final Drawable CHEVRON_DOWN;

    private final int HEADER_COLOR;

    private final int CHILD_COLOR;

    public NavigationDrawerAdapter(Context context){
        this.context=context;
        listDataHeader = Arrays.asList(this.context.getResources().getStringArray(R.array.navDrawerHeaders));
        List<String> randos = new ArrayList<>();
        randos.add("25/07/2015");
        randos.add("18/07/2015");
        randos.add("11/07/2015");

        listDataChild = new HashMap<>();

        listDataChild.put(listDataHeader.get(0), randos);

        listDataChild.put(listDataHeader.get(1),new ArrayList<String>());

        wasExpanded = new HashMap<>();
        for(int i=0;i<listDataHeader.size();i++){
            wasExpanded.put(i,false);
        }

        CHEVRON_DOWN = buildToggleExpandIcon();

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.color.icons, typedValue, true);

        HEADER_COLOR = typedValue.data;

        context.getTheme().resolveAttribute(R.color.icons,typedValue,true);
        CHILD_COLOR = typedValue.data;

    }

    @Override
    public int getGroupCount() {
        return listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listDataChild.get(getGroup(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listDataChild.get(getGroup(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        ExpandableListView elw = (ExpandableListView) parent;

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setTextColor(HEADER_COLOR);
        lblListHeader.setText(headerTitle);

        ImageView toggleExpand = (ImageView) convertView.findViewById(R.id.toggle_expand);
        toggleExpand.setImageDrawable(CHEVRON_DOWN);

        boolean wasExpanded = this.wasExpanded.get(groupPosition);

        if(isExpanded && !wasExpanded){
            toggleExpand.animate().setDuration(200).rotationBy(180);
        }else if(!isExpanded && wasExpanded){
            toggleExpand.animate().setDuration(200).rotationBy(-180);
        }

        this.wasExpanded.put(groupPosition, isExpanded);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
        txtListChild.setTextColor(CHILD_COLOR);

        txtListChild.setText(childText);

        return convertView;
    }


    private Drawable buildToggleExpandIcon(){

        // The method returns a MaterialDrawable, but as it is private to the builder you'll have to store it as a regular Drawable ;)
        Drawable yourDrawable = MaterialDrawableBuilder.with(context) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.CHEVRON_DOWN) // provide an icon
                .setColor(HEADER_COLOR) // set the icon color
                .setToActionbarSize() // set the icon size
                .build(); // Finally call build

        return yourDrawable;
    }
}
