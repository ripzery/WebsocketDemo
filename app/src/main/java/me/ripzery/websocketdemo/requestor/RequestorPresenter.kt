package me.ripzery.websocketdemo.requestor

/*
 * OmiseGO
 *
 * Created by Phuchit Sirimongkolsathien on 19/5/2018 AD.
 * Copyright © 2017-2018 OmiseGO. All rights reserved.
 */

import android.util.Log
import co.omisego.omisego.model.APIError
import co.omisego.omisego.model.socket.SocketTopic
import co.omisego.omisego.model.transaction.consumption.TransactionConsumption
import co.omisego.omisego.model.transaction.consumption.approve
import co.omisego.omisego.model.transaction.consumption.reject
import co.omisego.omisego.model.transaction.request.TransactionRequest
import co.omisego.omisego.model.transaction.request.TransactionRequestCreateParams
import co.omisego.omisego.model.transaction.request.TransactionRequestType
import co.omisego.omisego.operation.startListeningEvents
import co.omisego.omisego.websocket.SocketChannelCallback
import co.omisego.omisego.websocket.SocketCustomEventCallback
import me.ripzery.websocketdemo.network.CentralizedErrorCallback
import me.ripzery.websocketdemo.network.requestorOMGClient
import me.ripzery.websocketdemo.network.requestorSocketClient
import java.math.BigDecimal

class RequestorPresenter(private val mView: RequestorContract.View) : RequestorContract.Presenter {
    private var currentTransactionRequest: TransactionRequest? = null

    init {
        requestorSocketClient.setChannelListener(object : SocketChannelCallback {
            override fun onError(apiError: APIError) {
            }

            override fun onJoinedChannel(topic: SocketTopic) {
                mView.showUnsubscribe()
            }

            override fun onLeftChannel(topic: SocketTopic) {
                mView.showSubscribe()
            }
        })
    }

    override fun doTransactionRequest(amount: BigDecimal) {
        requestorOMGClient.createTransactionRequest(
                TransactionRequestCreateParams(
                        TransactionRequestType.RECEIVE,
                        "tok_OMG_01C78Z7DVJSK5NSGGMA0SA6WE2",
                        amount = amount
                )
        ).enqueue(CentralizedErrorCallback {
            currentTransactionRequest?.stopListening(requestorSocketClient)
            currentTransactionRequest = it
            mView.showTransactionInfo(it)
        })
    }

    override fun doSubscribe(transactionRequest: TransactionRequest) {
        transactionRequest.startListeningEvents(requestorSocketClient, callback = object : SocketCustomEventCallback.TransactionRequestCallback() {
            override fun onTransactionConsumptionFinalizedFail(apiError: APIError) {
                Log.d("Error", apiError.toString())
            }

            override fun onTransactionConsumptionFinalizedSuccess(transactionConsumption: TransactionConsumption) {
                Log.d("RequestorFinalized", transactionConsumption.toString())
                getBalance()
            }

            override fun onTransactionConsumptionRequest(transactionConsumption: TransactionConsumption) {
                mView.addLog(transactionConsumption)
                mView.showDialog(transactionConsumption)
            }
        })
    }

    override fun doUnsubscribe(transactionRequest: TransactionRequest) {
        transactionRequest.stopListening(requestorSocketClient)
    }

    override fun getBalance() {
        requestorOMGClient.listBalances().enqueue(CentralizedErrorCallback {
            mView.showBalance(it.data[0].balances[0])
        })
    }

    override fun approve(transactionConsumption: TransactionConsumption) {
        transactionConsumption.approve(requestorOMGClient).enqueue(CentralizedErrorCallback {
            mView.showApprove(transactionConsumption)
        })
    }

    override fun reject(transactionConsumption: TransactionConsumption) {
        transactionConsumption.reject(requestorOMGClient).enqueue(CentralizedErrorCallback {
            mView.showReject(transactionConsumption)
        })
    }
}