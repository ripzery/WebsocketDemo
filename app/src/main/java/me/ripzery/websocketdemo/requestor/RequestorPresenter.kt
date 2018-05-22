package me.ripzery.websocketdemo.requestor

import android.annotation.SuppressLint
import android.util.Log
import co.omisego.omisego.OMGAPIClient
import co.omisego.omisego.custom.OMGCallback
import co.omisego.omisego.model.APIError
import co.omisego.omisego.model.BalanceList
import co.omisego.omisego.model.OMGResponse
import co.omisego.omisego.model.socket.SocketTopic
import co.omisego.omisego.model.transaction.consumption.TransactionConsumption
import co.omisego.omisego.model.transaction.consumption.approve
import co.omisego.omisego.model.transaction.consumption.reject
import co.omisego.omisego.model.transaction.request.TransactionRequest
import co.omisego.omisego.model.transaction.request.TransactionRequestCreateParams
import co.omisego.omisego.model.transaction.request.TransactionRequestType
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
class RequestorPresenter(private val mView: RequestorContract.View) : RequestorContract.Presenter {
    private val requestorAPIClient: OMGAPIClient
    private var socketClient: SocketClientContract.Client
    private var currentTransactionRequest: TransactionRequest? = null

    init {
        requestorAPIClient = initializeOMGAPIClientByAuthToken("Z9EduOa0e8Aevb-47hrH4LRITQ_fUPbrK_VSqzjNPec", "zjH7vrLnwxuruQaDIZZ6jqKhlgLTsUCCYusBzUMQ3Ww") // User01
        socketClient = initializeSocketClientByAuthToken("Z9EduOa0e8Aevb-47hrH4LRITQ_fUPbrK_VSqzjNPec", "zjH7vrLnwxuruQaDIZZ6jqKhlgLTsUCCYusBzUMQ3Ww") // User01
    }

    override fun doTransactionRequest(amount: BigDecimal) {
        requestorAPIClient.createTransactionRequest(
            TransactionRequestCreateParams(
                TransactionRequestType.RECEIVE,
                "tok_OMG_01C78Z7DVJSK5NSGGMA0SA6WE2",
                amount = amount
            )
        ).enqueue(object : OMGCallback<TransactionRequest> {
            override fun fail(response: OMGResponse<APIError>) {
                Log.d("Test", response.toString())
            }

            @SuppressLint("SetTextI18n")
            override fun success(response: OMGResponse<TransactionRequest>) {
                currentTransactionRequest?.stopListening(socketClient)
                currentTransactionRequest = response.data
                mView.showTransactionInfo(response.data)
            }
        })
    }

    override fun doSubscribe(transactionRequest: TransactionRequest) {
        transactionRequest.startListeningEvents(socketClient, callback = object : SocketCustomEventCallback.TransactionRequestCallback() {
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
        transactionRequest.stopListening(socketClient)
    }

    override fun getBalance() {
        requestorAPIClient.listBalances().enqueue(object : OMGCallback<BalanceList> {
            override fun fail(response: OMGResponse<APIError>) {
            }

            override fun success(response: OMGResponse<BalanceList>) {
                mView.showBalance(response.data.data[0].balances[0])
            }
        })
    }

    override fun approve(transactionConsumption: TransactionConsumption) {
        transactionConsumption.approve(requestorAPIClient).enqueue(object : OMGCallback<TransactionConsumption> {
            override fun fail(response: OMGResponse<APIError>) {
                Log.d("error", response.data.toString())
            }

            override fun success(response: OMGResponse<TransactionConsumption>) {
                mView.showApprove(response.data)
            }
        })
    }

    override fun reject(transactionConsumption: TransactionConsumption) {
        transactionConsumption.reject(requestorAPIClient).enqueue(object : OMGCallback<TransactionConsumption> {
            override fun fail(response: OMGResponse<APIError>) {
                Log.d("error", response.data.toString())
            }

            override fun success(response: OMGResponse<TransactionConsumption>) {
                mView.showReject(response.data)
            }
        })
    }

    private fun initializeOMGAPIClientByAuthToken(authToken: String, apiKey: String): OMGAPIClient {
        val client = EWalletClient.Builder {
            baseUrl = "${IPAddress.HOST}/api/"
            this.apiKey = apiKey
            authenticationToken = authToken
            debug = false
        }.build()

        return OMGAPIClient(client)
    }

    private fun initializeSocketClientByAuthToken(authToken: String, apiKey: String): SocketClientContract.Client {
        val socketClient = OMGSocketClient.Builder {
            baseURL = "${IPAddress.HOST_SOCKET}/api/socket/"
            this.apiKey = apiKey
            authenticationToken = authToken
            debug = false
        }.build()

        socketClient.setChannelListener(object : SocketChannelCallback {
            override fun onError(apiError: APIError) {
                Log.d("Test", apiError.toString())
            }

            override fun onJoinedChannel(topic: SocketTopic) {
                Log.d("RequestorJoined", topic.name)
                mView.showUnsubscribe()
            }

            override fun onLeftChannel(topic: SocketTopic) {
                Log.d("RequestorLeft", topic.name)
                mView.showSubscribe()
            }
        })

        return socketClient
    }
}