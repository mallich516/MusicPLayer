package com.mallich.musicplayer.fragments.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mallich.musicplayer.fragments.home.tabs.SongsFragment

class HomeViewPager2Adapter(
    fm: FragmentActivity,
    private val fragmentsList: MutableList<Fragment>,
    private val titlesList: MutableList<String>
) : FragmentStateAdapter(fm) {

    override fun getItemCount(): Int {
        return fragmentsList.size
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return fragmentsList[position]
            1 -> return fragmentsList[position]
        }
        return SongsFragment()
    }

}