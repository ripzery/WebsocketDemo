package me.ripzery.websocketdemo.qr


import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.omisego.omisego.qrcode.generator.generateQRCode
import kotlinx.android.synthetic.main.fragment_show_qr.*
import me.ripzery.websocketdemo.R
import me.ripzery.websocketdemo.viewmodels.TransactionRequestViewModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ShowQRFragment : DialogFragment() {

    private var mTransactionInfo: String? = null

    private lateinit var transactionRequestViewModel: TransactionRequestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTransactionInfo = arguments?.getString(ARG_PARAM1)
        transactionRequestViewModel = ViewModelProviders.of(activity!!).get(TransactionRequestViewModel::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvTransactionInfo.text = mTransactionInfo ?: ""
        btnClose.setOnClickListener { dismiss() }
        val bitmap = transactionRequestViewModel.liveTransactionRequest.value?.generateQRCode()
        ivQRCode.setImageBitmap(bitmap)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    companion object {
        fun newInstance(text: String): ShowQRFragment {
            val bundle = Bundle()
            bundle.putString(ARG_PARAM1, text)
            return ShowQRFragment().apply {
                arguments = bundle
            }
        }
    }
}
