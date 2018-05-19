package me.ripzery.websocketdemo.consumer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import co.omisego.omisego.model.transaction.consumption.TransactionConsumption
import co.omisego.omisego.model.transaction.request.TransactionRequest
import kotlinx.android.synthetic.main.fragment_consumer.*
import kotlinx.android.synthetic.main.layout_transaction.*
import me.ripzery.websocketdemo.R
import me.ripzery.websocketdemo.viewmodels.TransactionRequestViewModel
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.ZoneId

class ConsumerFragment : Fragment(), ConsumerContract.View {
    private lateinit var transactionRequestViewModel: TransactionRequestViewModel
    private lateinit var mPresenter: ConsumerContract.Presenter
    private lateinit var transactionConsumption: TransactionConsumption

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_consumer, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = ConsumerPresenter(this)
        layoutTransaction.visibility = View.GONE
        tvEmpty.visibility = View.VISIBLE
        btnSubscribe.isEnabled = false
        btnConsume.isEnabled = false
        btnSubscribe.isSelected = true
        btnConsume.isSelected = true

        transactionRequestViewModel = ViewModelProviders.of(activity!!).get(TransactionRequestViewModel::class.java)
        transactionRequestViewModel.liveTransactionRequest.observe(this, Observer {
            it ?: return@Observer
            Log.d("Consumer", it.toString())
            updateTransactionInfo(it)
        })

        etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                when (p0?.length) {
                    0 -> btnConsume.isEnabled = false
                    else -> btnConsume.isEnabled = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })


    }

    private fun updateTransactionInfo(transactionRequest: TransactionRequest) {
        TransitionManager.beginDelayedTransition(cardViewTransaction)
        layoutTransaction.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        val amount = transactionRequest.amount!!.divide(transactionRequest.mintedToken.subunitToUnit, 2, RoundingMode.HALF_EVEN)
        tvAmount.text = "${transactionRequest.type.name.toUpperCase()}: ${DecimalFormat("#,###.00").format(amount)} ${transactionRequest.mintedToken.symbol}"
        tvId.text = "ID: ${transactionRequest.id}"
        val localTime = transactionRequest.createdAt!!.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
        tvCreatedAt.text = "Created At: ${localTime.hour}:${localTime.minute}:${localTime.second} PM"

        btnConsume.setOnClickListener {
            mPresenter.consume(etAmount.text.toString().toBigDecimal(), transactionRequest)
            val imm = ContextCompat.getSystemService<InputMethodManager>(context!!, InputMethodManager::class.java)
            imm!!.hideSoftInputFromWindow(view!!.windowToken, 0)
        }

        btnSubscribe.setOnClickListener {
            if (btnSubscribe.isSelected) mPresenter.doSubscribe(transactionConsumption)
            else mPresenter.doUnsubscribe(transactionConsumption)
        }
    }

    override fun showConsumption(transactionConsumption: TransactionConsumption) {
        this.transactionConsumption = transactionConsumption
        btnSubscribe.isEnabled = true
    }

    override fun showSubscribe() {
        btnSubscribe.text = "Subscribe"
        btnSubscribe.isSelected = true
    }

    override fun showUnsubscribe() {
        btnSubscribe.text = "Unsubscribe"
        btnSubscribe.isSelected = false
    }
}
