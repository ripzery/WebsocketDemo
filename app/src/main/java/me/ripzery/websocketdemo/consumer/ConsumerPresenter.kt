package me.ripzery.websocketdemo.consumer

import android.util.Log
import co.omisego.omisego.OMGAPIClient
import co.omisego.omisego.custom.OMGCallback
import co.omisego.omisego.model.APIError
import co.omisego.omisego.model.OMGResponse
import co.omisego.omisego.model.socket.SocketTopic
import co.omisego.omisego.model.transaction.consumption.TransactionConsumption
import co.omisego.omisego.model.transaction.request.TransactionRequest
import co.omisego.omisego.model.transaction.request.TransactionRequestParams
import co.omisego.omisego.model.transaction.request.toTransactionConsumptionParams
import co.omisego.omisego.network.ewallet.EWalletClient
import co.omisego.omisego.operation.startListeningEvents
import co.omisego.omisego.websocket.OMGSocketClient
import co.omisego.omisego.websocket.SocketChannelCallback
import co.omisego.omisego.websocket.SocketClientContract
import co.omisego.omisego.websocket.SocketCustomEventCallback
import me.ripzery.websocketdemo.network.IPAddress
import java.math.BigDecimal


/*
 * OmiseGO
 *
 * Created by Phuchit Sirimongkolsathien on 19/5/2018 AD.
 * Copyright © 2017-2018 OmiseGO. All rights reserved.
 */
class ConsumerPresenter(val mView: ConsumerContract.View) : ConsumerContract.Presenter {
    private val consumerAPIClient: OMGAPIClient
    private val socketClient: SocketClientContract.Client

    init {
        consumerAPIClient = initializeOMGAPIClientByAuthToken("NSDcbjFHo8qEJeCX39DKJejMO5afZptplqSuM_TChpA", "-c_1xZaBzcDZe2CRPwGq1uJ7qfSB7rHlmMaZG6mKxAQ")
        socketClient = initializeSocketClientByAuthToken("NSDcbjFHo8qEJeCX39DKJejMO5afZptplqSuM_TChpA", "-c_1xZaBzcDZe2CRPwGq1uJ7qfSB7rHlmMaZG6mKxAQ")
    }

    override fun consume(amount: BigDecimal, transactionRequest: TransactionRequest) {
        consumerAPIClient.consumeTransactionRequest(transactionRequest.toTransactionConsumptionParams(amount)!!)
                .enqueue(object : OMGCallback<TransactionConsumption> {
                    override fun fail(response: OMGResponse<APIError>) {

                    }

                    override fun success(response: OMGResponse<TransactionConsumption>) {
                        mView.showConsumption(response.data)
                    }
                })
    }


    override fun doSubscribe(transactionConsumption: TransactionConsumption) {
        transactionConsumption.startListeningEvents(socketClient, mapOf(), object : SocketCustomEventCallback.TransactionConsumptionCallback() {
            override fun onTransactionConsumptionFinalizedFail(apiError: APIError) {
                Log.d("Consumer", transactionConsumption.toString())
            }

            override fun onTransactionConsumptionFinalizedSuccess(transactionConsumption: TransactionConsumption) {
                Log.d("Consumer", transactionConsumption.toString())
                mView.showConsumption(transactionConsumption)
            }
        })
    }

    override fun getTransactionById(id: String) {
        consumerAPIClient.retrieveTransactionRequest(TransactionRequestParams(id)).enqueue(object : OMGCallback<TransactionRequest> {
            override fun fail(response: OMGResponse<APIError>) {

            }

            override fun success(response: OMGResponse<TransactionRequest>) {
                mView.showTransactionRequest(response.data)
            }
        })
    }

    override fun doUnsubscribe(transactionConsumption: TransactionConsumption) {
        transactionConsumption.stopListening(socketClient)
    }

    private fun initializeOMGAPIClientByAuthToken(authToken: String, apiKey: String): OMGAPIClient {
        val client = EWalletClient.Builder {
            baseUrl = "http://${IPAddress.HOST}:4000/api/"
            this.apiKey = apiKey
            authenticationToken = authToken
            debug = true
        }.build()

        return OMGAPIClient(client)
    }

    private fun initializeSocketClientByAuthToken(authToken: String, apiKey: String): SocketClientContract.Client {
        val socketClient = OMGSocketClient.Builder {
            baseURL = "ws://${IPAddress.HOST}:4000/api/socket/"
            this.apiKey = apiKey
            authenticationToken = authToken
            debug = true
        }.build()

        socketClient.setChannelListener(object : SocketChannelCallback {
            override fun onError(apiError: APIError) {
                Log.d("Test", apiError.toString())
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