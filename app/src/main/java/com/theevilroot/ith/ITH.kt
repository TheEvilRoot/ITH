package com.theevilroot.ith

import android.app.Application
import com.theevilroot.ithapi.ITHApi
import com.theevilroot.ithapi.Story
import java.io.File

class ITH : Application() {

    lateinit var userFile: File
    lateinit var username: String
    var session: ITHApi? = null
    var currentStory: Story? = null

    override fun onCreate() {
        super.onCreate()
        userFile = File(filesDir, "ith.user")
    }

    fun isLogged(): Boolean {
        return session != null && session!!.isLogged
    }

}