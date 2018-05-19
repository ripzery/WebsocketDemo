package me.ripzery.websocketdemo.requestor


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import co.omisego.omisego.extension.bd
import co.omisego.omisego.model.transaction.request.TransactionRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet_requestor.*
import kotlinx.android.synthetic.main.fragment_requestor.*
import kotlinx.android.synthetic.main.layout_transaction.*
import me.ripzery.websocketdemo.R
import me.ripzery.websocketdemo.data.ConsumeLog
import me.ripzery.websocketdemo.viewmodels.TransactionRequestViewModel
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.ZoneId


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class RequestorFragment : Fragment(), RequestorContract.View {

    private lateinit var mPresenter: RequestorPresenter
    private lateinit var transactionRequest: TransactionRequest
    private lateinit var transactionRequestViewModel: TransactionRequestViewModel
    private var logList: MutableList<ConsumeLog> = mutableListOf()
    private lateinit var logRecyclerAdapter: ConsumeLogRecyclerAdapter
    var lambdaTransactionRequest: ((TransactionRequest) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_requestor, container, false)
        transactionRequestViewModel = ViewModelProviders.of(activity!!).get(TransactionRequestViewModel::class.java)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = RequestorPresenter(this)
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

            val imm = getSystemService<InputMethodManager>(context!!, InputMethodManager::class.java)
            imm!!.hideSoftInputFromWindow(view.windowToken, 0)
        }

        btnSubscribe.setOnClickListener {
            if (btnSubscribe.isSelected) mPresenter.doSubscribe(transactionRequest)
            else mPresenter.doUnsubscribe(transactionRequest)
        }

        tvPeek.setOnClickListener {
            val bottomShit = BottomSheetBehavior.from(bottomSheet)
            when (bottomShit.state) {
                BottomSheetBehavior.STATE_EXPANDED -> bottomShit.state = BottomSheetBehavior.STATE_COLLAPSED
                BottomSheetBehavior.STATE_COLLAPSED -> bottomShit.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        logRecyclerAdapter = ConsumeLogRecyclerAdapter(logList)
        val layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, layoutManager.orientation)
        recyclerView.adapter = logRecyclerAdapter
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(dividerItemDecoration)
    }

    override fun addLog(consumeLog: ConsumeLog) {
        logRecyclerAdapter.addItem(consumeLog)
    }


    override fun showTransactionInfo(transactionRequest: TransactionRequest) {
        this.transactionRequest = transactionRequest
        lambdaTransactionRequest?.invoke(transactionRequest)
        transactionRequestViewModel.liveTransactionRequest.value = transactionRequest
        TransitionManager.beginDelayedTransition(cardViewTransaction)
        btnSubscribe.isEnabled = true
        layoutTransaction.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        val amount = transactionRequest.amount!!.divide(transactionRequest.mintedToken.subunitToUnit, 2, RoundingMode.HALF_EVEN)
        tvAmount.text = "${transactionRequest.type.name.toUpperCase()}: ${DecimalFormat("#,###.00").format(amount)} ${transactionRequest.mintedToken.symbol}"
        tvId.text = "ID: ${transactionRequest.id}"
        val localTime = transactionRequest.createdAt!!.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
        tvCreatedAt.text = "Created At: ${localTime.hour}:${localTime.minute}:${localTime.second} PM"
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
