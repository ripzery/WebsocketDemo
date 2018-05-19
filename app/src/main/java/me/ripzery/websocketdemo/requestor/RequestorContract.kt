package me.ripzery.websocketdemo.requestor

import co.omisego.omisego.model.transaction.request.TransactionRequest
import me.ripzery.websocketdemo.data.ConsumeLog
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
        fun addLog(consumeLog: ConsumeLog)
    }

    interface Presenter {
        fun doTransactionRequest(amount: BigDecimal)
        fun doSubscribe(transactionRequest: TransactionRequest)
        fun doUnsubscribe(transactionRequest: TransactionRequest)
    }
}