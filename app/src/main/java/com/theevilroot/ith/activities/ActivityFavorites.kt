package com.theevilroot.ith.activities

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.ListView
import android.widget.TextView
import com.theevilroot.ith.ITH
import com.theevilroot.ith.R
import com.theevilroot.ith.adapters.FavoriteListAdapter
import com.theevilroot.ithapi.Story
import kotlin.concurrent.thread

class ActivityFavorites : AppCompatActivity() {

    lateinit var app: ITH
    lateinit var favoriteList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        app = application as ITH
        favoriteList = findViewById(R.id.favorite_list)

        favoriteList.adapter = FavoriteListAdapter(this, arrayOf(Story(-1, getString(R.string.fav_loading_title), "", emptyList(), "")))
        favoriteList.setOnItemClickListener { _, _, position, _ ->
            val item = favoriteList.adapter.getItem(position) as Story
            if (item.id < 0)
                return@setOnItemClickListener
            val view = layoutInflater.inflate(R.layout.layout_favorite_story, null, false)
            val alert = AlertDialog.Builder(this, R.style.AppTheme_Menu).setView(view).setTitle("#${item.id}: ${item.title}")
            val storyContent = view.findViewById<TextView>(R.id.fav_story_content)

            storyContent.text = item.content
            alert.setPositiveButton(R.string.ith_menu_share_link_title, { _, _ ->
                app.shareStory(item.id)
            })
            alert.create().show()
        }

        thread(start = true, block = {
            app.loadFavorites()
            val favStories = app.favorites.map {
                app.session!!.loadStory(it)
            }.toTypedArray()
            runOnUiThread {
                if (favStories.isEmpty()) {
                    favoriteList.adapter = FavoriteListAdapter(this, arrayOf(Story(-1, getString(R.string.no_fav_stories_title), "", emptyList(), "")))
                    return@runOnUiThread
                }
                favoriteList.adapter = FavoriteListAdapter(this, favStories)
            }
        })
    }
}