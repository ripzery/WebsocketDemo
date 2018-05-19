package me.ripzery.websocketdemo.consumer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_consumer.*
import me.ripzery.websocketdemo.R

class ConsumerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_consumer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        layoutTransaction.visibility = View.GONE
        tvEmpty.visibility = View.VISIBLE
        btnSubscribe.isEnabled = false
        btnConsume.isEnabled = false
        btnSubscribe.isSelected = true
        btnConsume.isSelected = true
    }
}
