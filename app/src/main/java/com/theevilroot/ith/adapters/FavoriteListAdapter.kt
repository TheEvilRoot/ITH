package com.theevilroot.ith.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.theevilroot.ith.R
import com.theevilroot.ithapi.Story

class FavoriteListAdapter(ctx: Context, stories: Array<Story>) : ArrayAdapter<Story>(ctx, R.layout.favorite_list_item, stories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = getItem(position)
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.favorite_list_item, parent, false)
        }
        val id = view!!.findViewById<TextView>(R.id.favorite_item_id)
        val title = view.findViewById<TextView>(R.id.favorite_item_title)

        if (item.id > 0)
            id.text = String.format("#%05d", item.id)
        else
            id.visibility = View.INVISIBLE
        title.text = item.title

        return view

    }

}