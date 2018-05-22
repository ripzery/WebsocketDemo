package me.ripzery.websocketdemo.requestor

import co.omisego.omisego.model.Balance
import co.omisego.omisego.model.transaction.consumption.TransactionConsumption
import co.omisego.omisego.model.transaction.request.TransactionRequest
import java.math.BigDecimal

/*
 * OmiseGO
 *
 * Created by Phuchit Sirimongkolsathien on 19/5/2018 AD.
 * Copyright © 2017-2018 OmiseGO. All rights reserved.
 */
interface RequestorContract {
    interface View {
        fun showTransactionInfo(transactionRequest: TransactionRequest)
        fun showSubscribe()
        fun showUnsubscribe()
        fun addLog(transactionConsumption: TransactionConsumption)
        fun showDialog(transactionConsumption: TransactionConsumption)
        fun showApprove(transactionConsumption: TransactionConsumption)
        fun showReject(transactionConsumption: TransactionConsumption)
        fun showBalance(balance: Balance)
    }

    interface Presenter {
        fun getBalance()
        fun doTransactionRequest(amount: BigDecimal)
        fun doSubscribe(transactionRequest: TransactionRequest)
        fun doUnsubscribe(transactionRequest: TransactionRequest)
        fun approve(transactionConsumption: TransactionConsumption)
        fun reject(transactionConsumption: TransactionConsumption)
    }
}