package me.ripzery.websocketdemo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import co.omisego.omisego.extension.bd
import co.omisego.omisego.model.transaction.request.TransactionRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_transaction.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.ZoneId

class MainActivity : AppCompatActivity(), MainContract.View {
    private lateinit var mPresenter: MainContract.Presenter
    private lateinit var transactionRequest: TransactionRequest
    private var subscribed: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mPresenter = MainPresenter(this)

        layoutTransaction.visibility = View.GONE
        tvEmpty.visibility = View.VISIBLE
        btnSubscribe.isEnabled = false
        btnGenerate.isEnabled = false
        btnSubscribe.isSelected = true
        btnGenerate.isSelected = true

        etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                when (p0?.length) {
                    0 -> btnGenerate.isEnabled = false
                    else -> btnGenerate.isEnabled = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

        btnGenerate.setOnClickListener {
            it.isSelected = true
            val amount1 = etAmount.text.toString().toBigDecimal().times(10000.bd)
            mPresenter.doTransactionRequest(amount1)
        }

        btnSubscribe.setOnClickListener {
            if (btnSubscribe.isSelected) mPresenter.doSubscribe(transactionRequest)
            else mPresenter.doUnsubscribe(transactionRequest)
        }
    }

    override fun showTransactionInfo(transactionRequest: TransactionRequest) {
        this.transactionRequest = transactionRequest
        TransitionManager.beginDelayedTransition(cardViewTransaction)
        btnSubscribe.isEnabled = true
        layoutTransaction.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        val amount = transactionRequest.amount!!.divide(transactionRequest.mintedToken.subunitToUnit, 2, RoundingMode.HALF_EVEN)
        tvAmount.text = "Receive: ${DecimalFormat("#,###.00").format(amount)} ${transactionRequest.mintedToken.symbol}"
        tvId.text = "ID: ${transactionRequest.id}"
        val localTime = transactionRequest.createdAt!!.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
        tvCreatedAt.text = "Created At: ${localTime.hour}:${localTime.minute}:${localTime.second} PM"
    }

    override fun showSubscribe() {
        btnSubscribe.text = "Subscribe"
        btnSubscribe.isSelected = true
        subscribed = true
    }

    override fun showUnsubscribe() {
        btnSubscribe.text = "Unsubscribe"
        btnSubscribe.isSelected = false
        subscribed = false
    }
}
