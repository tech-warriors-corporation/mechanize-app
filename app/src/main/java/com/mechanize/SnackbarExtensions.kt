package com.mechanize

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.Gravity
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

fun Snackbar.top() {
    val params = view.layoutParams as CoordinatorLayout.LayoutParams

    params.setMargins(0, getStatusBarHeight(), 0, 0)

    params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
    view.layoutParams = params

    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.apply {
        maxLines = 4
        ellipsize = TextUtils.TruncateAt.END
        gravity = Gravity.CENTER_HORIZONTAL
    }

    setAction(R.string.close) { dismiss() }
    setActionTextColor(ContextCompat.getColor(context, R.color.primary))

    duration = 6000

    show()
}

@SuppressLint("DiscouragedApi", "InternalInsetResource")
private fun Snackbar.getStatusBarHeight(): Int {
    val resources = view.context.resources
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    val marginTop = if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    val halfSpace = resources.getDimension(R.dimen.half_space).toInt()

    return marginTop + halfSpace
}
