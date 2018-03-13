package com.theevilroot.ith.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.theevilroot.ith.ITH
import com.theevilroot.ith.OnSwipeTouchListener
import com.theevilroot.ith.R
import com.theevilroot.ithapi.ITHApi
import com.theevilroot.ithapi.Story
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class ActivityStory : AppCompatActivity() {

    lateinit var app: ITH

    lateinit var storyTitle: TextView
    lateinit var storyDate: TextView
    lateinit var storyTags: TextView
    lateinit var storyContent: TextView
    lateinit var ithButton: FloatingActionButton
    lateinit var favoriteButton: FloatingActionButton
    lateinit var scrollView: ScrollView

    lateinit var fadeEnter: Animation
    lateinit var fadeExit: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppDefault)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        app = application as ITH

        initViews()
        initITHMenu()

        fadeExit = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out)
        fadeEnter = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)

        val loadedUser = loadUserFromFile(app.userFile)

        if (loadedUser.first == null || loadedUser.second == null) {
            manualLogin(app.userFile)
        } else {
            createSession(loadedUser.first!!)
            Toast.makeText(this, "User loaded: ${loadedUser.first}. Last login: ${SimpleDateFormat("dd.MM.YYYY HH:mm:ss").format(loadedUser.second!!.toLong())}", android.widget.Toast.LENGTH_LONG).show()
        }

    }

    private fun initViews() {
        storyTitle = findViewById(R.id.story_title)
        storyContent = findViewById(R.id.story_content)
        storyDate = findViewById(R.id.story_date)
        storyTags = findViewById(R.id.story_tags)
        ithButton = findViewById(R.id.ith_button)
        favoriteButton = findViewById(R.id.favorite_button)
        scrollView = findViewById(R.id.story_scroll_view)

        val listener = object : OnSwipeTouchListener(this) {
            override fun onSwipeRight() {
                if (app.isLogged()) {
                    loadPrevStory()
                }
            }

            override fun onSwipeLeft() {
                if (app.isLogged()) {
                    loadNextStory()
                }
            }
        }

        scrollView.setOnTouchListener(listener)
    }

    private fun manualLogin(userFile: File) {
        val view = layoutInflater.inflate(R.layout.layout_login, null, false)
        AlertDialog.Builder(this).setCancelable(false).setView(view).setIcon(R.mipmap.ic_ith_round).setTitle(R.string.loginTitle).setPositiveButton(R.string.loginButtonTitle, { di, _ ->
            try {
                val username = view.findViewById<EditText>(R.id.login_username)
                if (username.text.isBlank()) {
                    di.dismiss()
                    this.manualLogin(userFile)
                }
                saveUser(userFile, username.text.toString())
                createSession(username.text.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).create().show()
    }

    private fun saveUser(userFile: File, username: String) {
        thread(start = true, block = {
            val json = JsonObject()
            json.addProperty("username", username)
            json.addProperty("lastLogin", Date().time)
            userFile.writeText(GsonBuilder().setPrettyPrinting().create().toJson(json))
            runOnUiThread {
                Toast.makeText(this, "Userdata saved successfully", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun createSession(username: String) {
        app.session = ITHApi(username)
        app.username = username
        thread(start = true, block = {
            try {
                app.session!!.auth()
                loadStory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun loadStory() {
        if (!app.isLogged())
            return
        thread(start = true, block = {
            try {
                app.currentStory = app.session!!.loadCurrentStory()
                runOnUiThread {
                    updateUI(app.currentStory!!, true)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun loadPrevStory() {
        if (!app.isLogged())
            return
        thread(start = true, block = {
            try {
                app.currentStory = app.session!!.decreaseAndLoad()
                runOnUiThread {
                    updateUI(app.currentStory!!, true)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun loadNextStory() {
        if (!app.isLogged())
            return
        thread(start = true, block = {
            try {
                app.currentStory = app.session!!.increaseAndLoad()
                runOnUiThread {
                    updateUI(app.currentStory!!, true)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun updateUI(story: Story, needAnim: Boolean) {
        if (needAnim) {
            storyContent.startAnimation(fadeExit)
            storyTitle.startAnimation(fadeExit)
            storyDate.startAnimation(fadeExit)
            storyTags.startAnimation(fadeExit)
        }
        storyTitle.text = "#${story.id}: ${story.title}"
        storyDate.text = story.date
        storyTags.text = story.tags.reduce({ s1, s2 -> "$s1, $s2" })
        storyContent.text = story.content
        setFavorite(Random().nextBoolean())
        if (needAnim) {
            storyContent.startAnimation(fadeEnter)
            storyTitle.startAnimation(fadeEnter)
            storyDate.startAnimation(fadeEnter)
            storyTags.startAnimation(fadeEnter)
        }
    }

    private fun setFavorite(fav: Boolean) = favoriteButton.setImageResource(if (fav) {
        R.drawable.star
    } else {
        R.drawable.star_outline
    })

    private fun loadUserFromFile(userFile: File): Pair<String?, String?> {
        if (!userFile.exists())
            return null to null
        try {
            val json = JsonParser().parse(userFile.readText())
            if (!json.isJsonObject)
                return null to null
            val obj = json.asJsonObject
            if (!obj.has("username") || !obj.has("lastLogin"))
                return null to null
            return obj.get("username").asString to obj.get("lastLogin").asString
        } catch (e: Exception) {
            return null to null
        }
    }

    private fun initITHMenu() {
        ithButton.setOnClickListener {
            if (!app.isLogged())
                return@setOnClickListener
            AlertDialog.Builder(this).setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayOf(
                    "Открыть в браузере",
                    "Копировать ссылку",
                    "Копировать историю 0_0",
                    "Поделиться в...",
                    "Перейти к...",
                    "Избранные",
                    "Сменить пользователя",
                    "О Программе"
            )), { di, index ->
                when (index) {
                    0 -> { // Open in browser
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://ithappens.me/story/${app.session!!.story}"))
                        startActivity(browserIntent)
                    }
                    1 -> { // Copy link
                        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("ith_link", "http://ithappens.me/story/${app.session!!.story}")
                        clipboard.primaryClip = clip
                    }
                    2 -> { // Copy story
                        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("ith_story", "#${app.currentStory!!.id}: ${app.currentStory!!.title}\n\n${app.currentStory!!.content}")
                        clipboard.primaryClip = clip
                    }
                    3 -> { // Share
                        shareStory(app.session!!.story)
                    }
                    4 -> {
                        showGotoDialog()
                    }
                    5 -> {
                        showFavorites()
                    }
                    6 -> {
                        app.userFile.delete()
                        this.recreate()
                    }
                    7 -> {
                        showAbout()
                    }
                }
            }).setTitle("Пользователь: ${app.username}").setIcon(R.drawable.account).create().show()
        }
    }

    private fun showAbout() {
        //AlertDialog.Builder(this).setTitle("О Программе ITH").setIcon(R.mipmap.ic_ith_round).setMessage()
    }

    private fun showFavorites() {

    }

    private fun showGotoDialog() {

    }

    private fun shareStory(id: Int) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, "http://ithappens.me/story/$id")
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

}