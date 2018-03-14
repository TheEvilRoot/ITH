package com.theevilroot.ith

import android.content.DialogInterface
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.theevilroot.ith.activities.ActivityStory

class ITHMenuItem(@StringRes val title: Int, @DrawableRes val icon: Int, val action: (di: DialogInterface, activity: ActivityStory, app: ITH) -> Unit)