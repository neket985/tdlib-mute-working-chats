package ru.smirnov.muteworkingchats.worker

import ru.smirnov.muteworkingchats.TdApi
import ru.smirnov.muteworkingchats.holder.ClientHolder

object GetMessageWorker : WorkerI {
    fun work(chatId: Long, messageId: Long) {
        awaitForComplete { future ->
            ClientHolder.getClient().send(TdApi.GetMessage(chatId, messageId)) { message ->
                ClientHolder.getClient().send(TdApi.GetMessageStatistics(chatId, messageId, true)) { messageStat ->
                    ClientHolder.getClient().send(TdApi.GetMessageViewers(chatId, messageId)) { messageView ->
                        println(message)
                        println(messageStat)
                        println(messageView)
                        future.complete(Unit)
                    }
                }
            }
        }
    }
}
