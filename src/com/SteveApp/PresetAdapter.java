package com.SteveApp;

import java.util.List;

import android.content.Context;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PresetAdapter extends BaseAdapter
{
	private Context context;
	private List<Spanned> PresetList;

	public PresetAdapter(Context context, List<Spanned> fileList)
	{
		this.context = context;
		this.PresetList = fileList;
	}

	public int getCount()
	{
		return PresetList.size();
	}

	public Object getItem(int position)
	{
		return PresetList.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public static class ViewHolder
    {
        public TextView item1;
    }
    
    public View getView(int position, View convertView, ViewGroup parent)
	{
    	View v = convertView;
        ViewHolder holder;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.preset_item, null);
            holder = new ViewHolder();
            holder.item1 = (TextView) v.findViewById(R.id.preset);
            v.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)v.getTag();
        }
 
        final Spanned file = PresetList.get(position);
        if (file != null)
        {
            holder.item1.setText(file);
        }
        return v;
    }
}
