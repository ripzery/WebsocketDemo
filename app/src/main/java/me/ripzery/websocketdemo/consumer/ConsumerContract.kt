package me.ripzery.websocketdemo.consumer

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
interface ConsumerContract {
    interface View {
        fun showConsumption(transactionConsumption: TransactionConsumption)
        fun showSubscribe()
        fun showUnsubscribe()
        fun showTransactionRequest(transactionRequest: TransactionRequest)
        fun showApprove(transactionConsumption: TransactionConsumption)
        fun showReject(transactionConsumption: TransactionConsumption)
        fun showBalance(balance: Balance)
    }

    interface Presenter {
        fun consume(amount: BigDecimal, transactionRequest: TransactionRequest)
        fun getTransactionById(id: String)
        fun doSubscribe(transactionConsumption: TransactionConsumption)
        fun doUnsubscribe(transactionConsumption: TransactionConsumption)
        fun getBalance()
    }
}