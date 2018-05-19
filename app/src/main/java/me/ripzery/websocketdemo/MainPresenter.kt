package me.ripzery.websocketdemo

import android.annotation.SuppressLint
import android.util.Log
import co.omisego.omisego.OMGAPIClient
import co.omisego.omisego.custom.OMGCallback
import co.omisego.omisego.model.APIError
import co.omisego.omisego.model.OMGResponse
import co.omisego.omisego.model.socket.SocketTopic
import co.omisego.omisego.model.transaction.consumption.TransactionConsumption
import co.omisego.omisego.model.transaction.request.TransactionRequest
import co.omisego.omisego.model.transaction.request.TransactionRequestCreateParams
import co.omisego.omisego.model.transaction.request.TransactionRequestType
import co.omisego.omisego.network.ewallet.EWalletClient
import co.omisego.omisego.operation.startListeningEvents
import co.omisego.omisego.websocket.OMGSocketClient
import co.omisego.omisego.websocket.SocketChannelCallback
import co.omisego.omisego.websocket.SocketClientContract
import co.omisego.omisego.websocket.SocketCustomEventCallback
import java.math.BigDecimal


/*
 * OmiseGO
 *
 * Created by Phuchit Sirimongkolsathien on 19/5/2018 AD.
 * Copyright © 2017-2018 OmiseGO. All rights reserved.
 */
class MainPresenter(private val mView: MainContract.View) : MainContract.Presenter {
    private val requestorAPIClient: OMGAPIClient
    private val socketClient: SocketClientContract.Client

    init {
        requestorAPIClient = initializeOMGAPIClientByAuthToken("ZyqbUmnHGCKjquCR1LGGVWQEiNA-EB9MJkMCcdd1nXo", "-c_1xZaBzcDZe2CRPwGq1uJ7qfSB7rHlmMaZG6mKxAQ") // User01
        socketClient = initializeSocketClientByAuthToken("ZyqbUmnHGCKjquCR1LGGVWQEiNA-EB9MJkMCcdd1nXo", "-c_1xZaBzcDZe2CRPwGq1uJ7qfSB7rHlmMaZG6mKxAQ") // User01
    }

    override fun doTransactionRequest(amount: BigDecimal) {
        requestorAPIClient.createTransactionRequest(
                TransactionRequestCreateParams(
                        TransactionRequestType.RECEIVE,
                        "OMG:a9ef7096-4060-4155-b79d-b36c42d5d095",
                        amount = amount
                )
        ).enqueue(object : OMGCallback<TransactionRequest> {
            override fun fail(response: OMGResponse<APIError>) {
                Log.d("Test", response.toString())
            }

            @SuppressLint("SetTextI18n")
            override fun success(response: OMGResponse<TransactionRequest>) {
                mView.showTransactionInfo(response.data)
            }
        })
    }

    override fun doSubscribe(transactionRequest: TransactionRequest) {
        transactionRequest.startListeningEvents(socketClient, callback = object : SocketCustomEventCallback.TransactionRequestCallback() {
            override fun onTransactionConsumptionFinalizedFail(apiError: APIError) {

            }

            override fun onTransactionConsumptionFinalizedSuccess(transactionConsumption: TransactionConsumption) {

            }

            override fun onTransactionConsumptionRequest(transactionConsumption: TransactionConsumption) {

            }
        })
    }


    override fun doUnsubscribe(transactionRequest: TransactionRequest) {
        transactionRequest.stopListening(socketClient)
    }

    private fun initializeOMGAPIClientByAuthToken(authToken: String, apiKey: String): OMGAPIClient {
        val client = EWalletClient.Builder {
            baseUrl = "http://192.168.43.133:4000/api/"
            this.apiKey = apiKey
            authenticationToken = authToken
            debug = true
        }.build()

        return OMGAPIClient(client)
    }

    private fun initializeSocketClientByAuthToken(authToken: String, apiKey: String): SocketClientContract.Client {
        val socketClient = OMGSocketClient.Builder {
            baseURL = "ws://192.168.43.133:4000/api/socket/"
            this.apiKey = apiKey
            authenticationToken = authToken
            debug = true
        }.build()

        socketClient.setChannelListener(object : SocketChannelCallback {
            override fun onError(apiError: APIError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onJoinedChannel(topic: SocketTopic) {
                mView.showUnsubscribe()
            }

            override fun onLeftChannel(topic: SocketTopic) {
                mView.showSubscribe()
            }
        })

        return socketClient
    }
}