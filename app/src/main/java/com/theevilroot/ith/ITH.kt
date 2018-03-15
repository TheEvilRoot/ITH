package com.theevilroot.ith

import android.app.Application
import android.content.Intent
import com.google.gson.JsonParser
import com.theevilroot.ithapi.ITHApi
import com.theevilroot.ithapi.Story
import org.jsoup.Jsoup
import java.io.File

class ITH : Application() {

    lateinit var userFile: File
    lateinit var username: String
    lateinit var favorites: Array<Int>
    var session: ITHApi? = null
    var currentStory: Story? = null
    override fun onCreate() {
        super.onCreate()
        userFile = File(filesDir, "ith.user")
    }

    fun isLogged(): Boolean {
        return session != null && session!!.isLogged
    }

    fun loadFavorites() {
        if (!isLogged())
            return
        favorites = emptyArray()
        val json = JsonParser().parse(Jsoup.connect("http://52.48.142.75/backend/ITH.php").data("task", "getFavorites").data("name", username).post().text()).asJsonObject
        favorites = json.get("stories").asJsonArray.map {
            it.asJsonObject.get("story_id").asInt
        }.toTypedArray()

    }


    fun shareStory(id: Int) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, "http://ithappens.me/story/$id")
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

}