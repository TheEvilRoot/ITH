package com.theevilroot.ith.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.theevilroot.ith.ITHMenuItem
import com.theevilroot.ith.R


val items: Array<ITHMenuItem> = arrayOf(
        ITHMenuItem(R.string.ith_menu_open_in_browser_title, R.drawable.web, { di, activity, app ->
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://ithappens.me/story/${app.session!!.story}"))
            activity.startActivity(browserIntent)
        }),
        ITHMenuItem(R.string.ith_menu_copy_link_title, R.drawable.content_copy, { di, activity, app ->
            val clipboard = activity.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ith_link", "http://ithappens.me/story/${app.session!!.story}")
            clipboard.primaryClip = clip
        }),
        ITHMenuItem(R.string.ith_menu_copy_story_title, R.drawable.note_multiple_outline, { di, activity, app ->
            val clipboard = activity.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ith_story", "#${app.currentStory!!.id}: ${app.currentStory!!.title}\n\n${app.currentStory!!.content}")
            clipboard.primaryClip = clip
        }),
        ITHMenuItem(R.string.ith_menu_share_link_title, R.drawable.share, { di, activity, app ->
            activity.shareStory(app.session!!.story)
        }),
        ITHMenuItem(R.string.ith_menu_goto_story_title, R.drawable.chevron_right, { di, activity, app ->
            activity.showGotoDialog()
        }),
        ITHMenuItem(R.string.ith_menu_show_favorite_title, R.drawable.star_outline, { di, activity, app ->
            activity.showFavorites()
        }),
        ITHMenuItem(R.string.ith_menu_logout_title, R.drawable.logout, { di, activity, app ->
            app.userFile.delete()
            activity.recreate()
        }),
        ITHMenuItem(R.string.ith_menu_about_title, R.drawable.information_outline, { di, activity, app ->
            activity.showAbout()
        })
);


class ITHMenuItemAdapter(ctx: Context) : ArrayAdapter<ITHMenuItem>(ctx, R.layout.ith_menu_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.ith_menu_item, parent, false)
        }
        val title = view!!.findViewById<TextView>(R.id.ith_menu_item_title)
        val icon = view.findViewById<ImageView>(R.id.ith_menu_item_icon)

        title.text = context.getString(item.title)
        icon.setImageDrawable(context.getDrawable(item.icon))


        return view
    }

}