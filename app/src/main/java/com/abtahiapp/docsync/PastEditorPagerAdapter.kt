package com.abtahiapp.docsync

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PastEditorPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val originalTitle: String,
    private val originalContent: String,
    private val pastEditorsJson: String
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> EditedVersionFragment.newInstance(originalTitle, originalContent, pastEditorsJson)
            1 -> OriginalContentFragment.newInstance(originalTitle, originalContent)
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
