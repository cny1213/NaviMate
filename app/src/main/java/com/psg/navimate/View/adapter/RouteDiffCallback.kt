package com.psg.navimate.View.adapter

import androidx.recyclerview.widget.DiffUtil
import com.psg.navimate.Model.Route


class RouteDiffCallback : DiffUtil.ItemCallback<Route>() {
    // 같은 "아이템"인지 비교 (ID 역할)
    override fun areItemsTheSame(oldItem: Route, newItem: Route): Boolean {
        return oldItem.routeNumber == newItem.routeNumber
    }

    // 같은 "내용"인지 비교 (equals())
    override fun areContentsTheSame(oldItem: Route, newItem: Route): Boolean {
        return oldItem == newItem
    }
}