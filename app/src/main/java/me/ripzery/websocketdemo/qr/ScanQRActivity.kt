package me.ripzery.websocketdemo.qr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import co.omisego.omisego.OMGAPIClient
import co.omisego.omisego.model.APIError
import co.omisego.omisego.model.OMGResponse
import co.omisego.omisego.model.transaction.request.TransactionRequest
import co.omisego.omisego.network.ewallet.EWalletClient
import co.omisego.omisego.qrcode.scanner.OMGQRScannerContract
import kotlinx.android.synthetic.main.activity_scan_qr.*
import me.ripzery.websocketdemo.R
import me.ripzery.websocketdemo.network.IPAddress


/*
 * OmiseGO
 *
 * Created by Phuchit Sirimongkolsathien on 20/5/2018 AD.
 * Copyright © 2017-2018 OmiseGO. All rights reserved.
 */
class ScanQRActivity : AppCompatActivity(), OMGQRScannerContract.Callback {
    private lateinit var omgapiClient: OMGAPIClient

    override fun scannerDidCancel(view: OMGQRScannerContract.View) {

    }

    override fun scannerDidDecode(view: OMGQRScannerContract.View, transactionRequest: OMGResponse<TransactionRequest>) {
        Toast.makeText(this, transactionRequest.data.id, Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK, Intent().apply { putExtra("id", transactionRequest.data.id) })
        finish()
    }

    override fun scannerDidFailToDecode(view: OMGQRScannerContract.View, exception: OMGResponse<APIError>) {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qr)
        omgapiClient = initializeOMGAPIClientByAuthToken("ZWaBP-Kzdknf_RRcVB5F5ApYnW0jA5BlvPCMFgCo4qY", "zjH7vrLnwxuruQaDIZZ6jqKhlgLTsUCCYusBzUMQ3Ww")
        scannerView.startCamera(omgapiClient, this)
    }

    private fun initializeOMGAPIClientByAuthToken(authToken: String, apiKey: String): OMGAPIClient {
        val client = EWalletClient.Builder {
            baseUrl = "${IPAddress.HOST}/api/"
            this.apiKey = apiKey
            authenticationToken = authToken
            debug = true
        }.build()

        return OMGAPIClient(client)
    }

    override fun onStop() {
        super.onStop()
        scannerView.stopCamera()
    }

    override fun onStart() {
        super.onStart()
        scannerView.startCamera(omgapiClient, this)
    }
}