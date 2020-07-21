package me.ripzery.websocketdemo.network

/*
 * OmiseGO
 *
 * Created by Phuchit Sirimongkolsathien on 22/5/2018 AD.
 * Copyright © 2017-2018 OmiseGO. All rights reserved.
 */


import android.util.Log
import co.omisego.omisego.OMGAPIClient
import co.omisego.omisego.custom.OMGCallback
import co.omisego.omisego.model.APIError
import co.omisego.omisego.model.OMGResponse
import co.omisego.omisego.network.ewallet.EWalletClient
import co.omisego.omisego.websocket.OMGSocketClient
import co.omisego.omisego.websocket.SocketClientContract
import me.ripzery.websocketdemo.BuildConfig

val requestorOMGClient: OMGAPIClient by lazy { createOMGAPIClient(BuildConfig.API_KEY, BuildConfig.USER_REQUESTOR_AUTH_TOKEN) }
val consumerOMGClient: OMGAPIClient by lazy { createOMGAPIClient(BuildConfig.API_KEY, BuildConfig.USER_CONSUMER_AUTH_TOKEN) }
val requestorSocketClient: SocketClientContract.Client by lazy { createOMGSocketClient(BuildConfig.API_KEY, BuildConfig.USER_REQUESTOR_AUTH_TOKEN) }
val consumerSocketClient: SocketClientContract.Client by lazy { createOMGSocketClient(BuildConfig.API_KEY, BuildConfig.USER_CONSUMER_AUTH_TOKEN) }

fun createOMGSocketClient(apiKey: String, authToken: String): SocketClientContract.Client {
    return OMGSocketClient.Builder {
        baseURL = "${BuildConfig.HOST_SOCKET}/api/socket/"
        this.apiKey = apiKey
        authenticationToken = authToken
        debug = false
    }.build()
}

fun createOMGAPIClient(apiKey: String, authToken: String): OMGAPIClient {
    val client = EWalletClient.Builder {
        baseUrl = "${BuildConfig.HOST}/api/"
        this.apiKey = apiKey
        authenticationToken = authToken
        debug = false
    }.build()

    return OMGAPIClient(client)
}

class CentralizedErrorCallback<T>(private val lambda: (data: T) -> Unit) : OMGCallback<T> {
    override fun fail(response: OMGResponse<APIError>) {// Do something
        Log.d("CentralizedError", response.data.toString())
    }

    override fun success(response: OMGResponse<T>) {// Do something
        lambda(response.data)
    }
}
