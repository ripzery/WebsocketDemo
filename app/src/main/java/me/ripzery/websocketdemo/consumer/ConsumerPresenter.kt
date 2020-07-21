package me.ripzery.websocketdemo.consumer

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
import co.omisego.omisego.model.transaction.request.TransactionRequest
import co.omisego.omisego.model.transaction.request.TransactionRequestParams
import co.omisego.omisego.model.transaction.request.toTransactionConsumptionParams
import co.omisego.omisego.operation.startListeningEvents
import co.omisego.omisego.websocket.SocketChannelCallback
import co.omisego.omisego.websocket.SocketCustomEventCallback
import me.ripzery.websocketdemo.network.CentralizedErrorCallback
import me.ripzery.websocketdemo.network.consumerOMGClient
import me.ripzery.websocketdemo.network.consumerSocketClient
import java.math.BigDecimal

class ConsumerPresenter(val mView: ConsumerContract.View) : ConsumerContract.Presenter {

    init {
        consumerSocketClient.setChannelListener(object : SocketChannelCallback {
            override fun onError(apiError: APIError) {}
            override fun onJoinedChannel(topic: SocketTopic) {
                mView.showUnsubscribe()
            }

            override fun onLeftChannel(topic: SocketTopic) {
                mView.showSubscribe()
            }
        })
    }

    override fun consume(amount: BigDecimal, transactionRequest: TransactionRequest) {
        consumerOMGClient.consumeTransactionRequest(transactionRequest.toTransactionConsumptionParams(amount)!!)
                .enqueue(CentralizedErrorCallback {
                    mView.showConsumption(it)
                })
    }

    override fun getBalance() {
        consumerOMGClient.listBalances().enqueue(CentralizedErrorCallback {
            mView.showBalance(it.data[0].balances[0])
        })
    }

    override fun doSubscribe(transactionConsumption: TransactionConsumption) {
        transactionConsumption.startListeningEvents(consumerSocketClient, mapOf(), object : SocketCustomEventCallback.TransactionConsumptionCallback() {
            override fun onTransactionConsumptionFinalizedFail(apiError: APIError) {
                Log.d("Consumer", transactionConsumption.toString())
            }

            override fun onTransactionConsumptionFinalizedSuccess(transactionConsumption: TransactionConsumption) {
                Log.d("Consumer", transactionConsumption.toString())
                mView.showConsumption(transactionConsumption)
                getBalance()
            }
        })
    }

    override fun getTransactionById(id: String) {
        consumerOMGClient.retrieveTransactionRequest(TransactionRequestParams(id)).enqueue(CentralizedErrorCallback {
            mView.showTransactionRequest(it)
        })
    }

    override fun doUnsubscribe(transactionConsumption: TransactionConsumption) {
        transactionConsumption.stopListening(consumerSocketClient)
    }
}