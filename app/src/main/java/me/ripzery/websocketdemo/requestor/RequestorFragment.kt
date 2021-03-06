package me.ripzery.websocketdemo.requestor


import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import co.omisego.omisego.extension.bd
import co.omisego.omisego.model.transaction.consumption.TransactionConsumption
import co.omisego.omisego.model.transaction.request.TransactionRequest
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.bottom_sheet_requestor.*
import kotlinx.android.synthetic.main.fragment_requestor.*
import kotlinx.android.synthetic.main.layout_transaction.*
import me.ripzery.websocketdemo.R
import me.ripzery.websocketdemo.data.ConsumeLog
import me.ripzery.websocketdemo.qr.ShowQRFragment
import me.ripzery.websocketdemo.viewmodels.TransactionRequestViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.ZoneId

class RequestorFragment : Fragment(), RequestorContract.View {

    private lateinit var mPresenter: RequestorPresenter
    private lateinit var transactionRequest: TransactionRequest
    private lateinit var transactionRequestViewModel: TransactionRequestViewModel
    private var logList: MutableList<ConsumeLog> = mutableListOf()
    private var confirmDialog: MaterialDialog? = null
    private lateinit var logRecyclerAdapter: ConsumeLogRecyclerAdapter

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
        btnShowQR.isEnabled = false

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

        btnShowQR.setOnClickListener {
            val showQRFragment = ShowQRFragment
                    .newInstance(
                            "${transactionRequest.type.name}: ${formatAmount(transactionRequest.amount!!, transactionRequest.mintedToken.subunitToUnit)} ${transactionRequest.mintedToken.symbol}".toUpperCase()
                    )
            showQRFragment.show(childFragmentManager, "fragment_requestor")
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


    @SuppressLint("SetTextI18n")
    override fun showTransactionInfo(transactionRequest: TransactionRequest) {
        this.transactionRequest = transactionRequest
        transactionRequestViewModel.liveTransactionRequest.value = transactionRequest
        TransitionManager.beginDelayedTransition(cardViewTransaction)
        btnShowQR.isEnabled = true
        btnSubscribe.isEnabled = true
        layoutTransaction.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        tvAmount.text = "${transactionRequest.type.name.toUpperCase()}: ${formatAmount(transactionRequest.amount!!, transactionRequest.mintedToken.subunitToUnit)} ${transactionRequest.mintedToken.symbol}"
        tvId.text = "ID: ${transactionRequest.id}"
        val localTime = transactionRequest.createdAt!!.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
        tvCreatedAt.text = "Created At: ${localTime.hour}:${localTime.minute}:${localTime.second} PM"
    }

    private fun formatAmount(amount: BigDecimal, subunitToUnit: BigDecimal): String {
        val amount = amount.divide(subunitToUnit, 2, RoundingMode.HALF_EVEN)
        return DecimalFormat("#,###.00").format(amount)
    }


    override fun showDialog(transactionConsumption: TransactionConsumption) {
        val name = transactionConsumption.user?.username
        val amount = transactionConsumption.amount
        val subunitToUnit = transactionConsumption.mintedToken.subunitToUnit

        confirmDialog = confirmDialog ?: MaterialDialog.Builder(context!!)
                .typeface("raleway_semibold.ttf", "raleway_medium.ttf")
                .title("Incoming transaction")
                .positiveText("Approve")
                .positiveColor(ContextCompat.getColor(context!!, R.color.primaryDarkColor))
                .negativeText("Reject")
                .negativeColor(ContextCompat.getColor(context!!, R.color.secondaryLightColor))
                .autoDismiss(true)
                .onPositive { _, _ ->
                    mPresenter.approve(transactionConsumption)
                }
                .onNegative { _, _ ->
                    mPresenter.reject(transactionConsumption)
                }
                .build()

        confirmDialog?.setContent("$name has sent you ${formatAmount(amount, subunitToUnit)} OMG 🤪")
        confirmDialog?.show()
    }

    override fun showSubscribe() {
        btnSubscribe.text = "Subscribe"
        btnSubscribe.isSelected = true
    }

    override fun showUnsubscribe() {
        btnSubscribe.text = "Unsubscribe"
        btnSubscribe.isSelected = false
    }

    override fun showApprove() {
        Toast.makeText(context!!, "Approved", Toast.LENGTH_SHORT).show()
    }

    override fun showReject() {
        Toast.makeText(context!!, "Rejected", Toast.LENGTH_SHORT).show()
    }

}
