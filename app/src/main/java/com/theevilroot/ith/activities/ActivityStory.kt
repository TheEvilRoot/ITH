package com.theevilroot.ith.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.theevilroot.ith.ITH
import com.theevilroot.ith.OnSwipeTouchListener
import com.theevilroot.ith.R
import com.theevilroot.ith.adapters.ITHMenuItemAdapter
import com.theevilroot.ith.adapters.items
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
                    return@setPositiveButton
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

    private fun loadStoryByIndex(id: Int) {
        if (!app.isLogged())
            return
        thread(start = true, block = {
            try {
                app.currentStory = app.session!!.setAndLoadStory(id)
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
            AlertDialog.Builder(this).setAdapter(ITHMenuItemAdapter(this), { di, index ->
                if (index >= items.size)
                    return@setAdapter
                val item = items[index]
                item.action.invoke(di, this, app)
                Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
            }).setTitle("Пользователь: ${app.username}").create().show()
        }
    }

    fun showAbout() {
        //AlertDialog.Builder(this).setTitle("О Программе ITH").setIcon(R.mipmap.ic_ith_round).setMessage()
    }

    fun showFavorites() {

    }

    fun showGotoDialog() {
        val view = layoutInflater.inflate(R.layout.layout_goto_story, null, false)
        AlertDialog.Builder(this).setTitle(R.string.goto_story_title).setView(view).setPositiveButton(R.string.goto_story_button_title, { di, _ ->
            val input = view.findViewById<EditText>(R.id.goto_story_index_input_field)
            if (input.text.isBlank()) {
                di.dismiss()
                showGotoDialog()
                return@setPositiveButton
            }
            val index = try {
                input.text.toString().toInt()
            } catch (e: NumberFormatException) {
                di.dismiss();
                showGotoDialog()
                return@setPositiveButton
            }
            loadStoryByIndex(index)
            di.dismiss()
        }).create().show()
    }

    fun shareStory(id: Int) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, "http://ithappens.me/story/$id")
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

}