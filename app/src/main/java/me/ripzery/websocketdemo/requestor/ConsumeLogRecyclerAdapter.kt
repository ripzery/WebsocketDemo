package me.ripzery.websocketdemo.requestor

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.viewholder_tx_consumption_log.view.*
import me.ripzery.websocketdemo.R
import me.ripzery.websocketdemo.data.ConsumeLog


/*
 * OmiseGO
 *
 * Created by Phuchit Sirimongkolsathien on 19/5/2018 AD.
 * Copyright © 2017-2018 OmiseGO. All rights reserved.
 */
class ConsumeLogRecyclerAdapter(private val list: MutableList<ConsumeLog>) : RecyclerView.Adapter<ConsumeLogRecyclerAdapter.ConsumeLogVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsumeLogVH {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_tx_consumption_log, parent, false)
        return ConsumeLogVH(rootView)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ConsumeLogVH, position: Int) {
        holder.setModel(list[position])
    }

    fun addItem(consumeLog: ConsumeLog) {
        list.add(consumeLog)
        notifyItemInserted(list.size - 1)
    }

    inner class ConsumeLogVH(val rootView: View) : RecyclerView.ViewHolder(rootView) {
        fun setModel(data: ConsumeLog) {
            rootView.tvTitle.text = data.name
            val amount = rootView.resources.getString(R.string.sent_amount)
            rootView.tvAmount.text = String.format(amount, data.amount.toInt())
        }
    }

}