package me.ripzery.websocketdemo.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import co.omisego.omisego.model.transaction.request.TransactionRequest

/*
 * OmiseGO
 *
 * Created by Phuchit Sirimongkolsathien on 19/5/2018 AD.
 * Copyright © 2017-2018 OmiseGO. All rights reserved.
 */
class TransactionRequestViewModel : ViewModel() {
    val liveTransactionRequest: MutableLiveData<TransactionRequest> by lazy { MutableLiveData<TransactionRequest>() }
}