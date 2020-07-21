package me.ripzery.websocketdemo.consumer

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import co.omisego.omisego.model.Balance
import co.omisego.omisego.model.transaction.consumption.TransactionConsumption
import co.omisego.omisego.model.transaction.request.TransactionRequest
import kotlinx.android.synthetic.main.fragment_consumer.*
import kotlinx.android.synthetic.main.layout_transaction.*
import me.ripzery.websocketdemo.R
import me.ripzery.websocketdemo.qr.ScanQRActivity
import me.ripzery.websocketdemo.viewmodels.TransactionRequestViewModel
import java.math.BigDecimal
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
            showTransactionRequest(it)
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

        btnScanQR.setOnClickListener {
            startActivityForResult(Intent(this.activity, ScanQRActivity::class.java), 100)
        }

        mPresenter.getBalance()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                100 -> {
                    val transactionRequestID = data?.getStringExtra("id") ?: return
                    mPresenter.getTransactionById(transactionRequestID)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun showTransactionRequest(transactionRequest: TransactionRequest) {
        TransitionManager.beginDelayedTransition(cardViewTransaction)
        layoutTransaction.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        val amount = transactionRequest.amount!!.divide(transactionRequest.mintedToken.subunitToUnit, 2, RoundingMode.HALF_EVEN)
        tvAmount.text = "${transactionRequest.type.name.toUpperCase()}: ${DecimalFormat("#,###.00").format(amount)} ${transactionRequest.mintedToken.symbol}"
        tvId.text = "ID: ${transactionRequest.id}"
        val localTime = transactionRequest.createdAt!!.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
        tvCreatedAt.text = "Created At: ${localTime.hour}:${localTime.minute}:${localTime.second} PM"

        btnConsume.setOnClickListener {
            mPresenter.consume(etAmount.text.toString().toBigDecimal().multiply(transactionRequest.mintedToken.subunitToUnit), transactionRequest)
            val imm = ContextCompat.getSystemService<InputMethodManager>(context!!, InputMethodManager::class.java)
            imm!!.hideSoftInputFromWindow(view!!.windowToken, 0)
        }

        btnSubscribe.setOnClickListener {
            if (btnSubscribe.isSelected) mPresenter.doSubscribe(transactionConsumption)
            else mPresenter.doUnsubscribe(transactionConsumption)
        }
    }

    override fun showApprove(transactionConsumption: TransactionConsumption) {
    }

    override fun showReject(transactionConsumption: TransactionConsumption) {
    }

    @SuppressLint("SetTextI18n")
    override fun showBalance(balance: Balance) {
        TransitionManager.beginDelayedTransition(layoutRootConsumer)
        tvUserInfo.text = "User2's balance = ${formatAmount(balance.amount, balance.mintedToken.subunitToUnit)} OMG"
    }

    override fun showConsumption(transactionConsumption: TransactionConsumption) {
        this.transactionConsumption = transactionConsumption
        mPresenter.doSubscribe(transactionConsumption)
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

    private fun formatAmount(amount: BigDecimal, subunitToUnit: BigDecimal): String {
        val amount = amount.divide(subunitToUnit, 2, RoundingMode.HALF_EVEN)
        return DecimalFormat("#,###.00").format(amount)
    }
}
