package ru.smirnov.muteworkingchats.worker

import ru.smirnov.muteworkingchats.TdApi
import ru.smirnov.muteworkingchats.holder.ChatsHolder
import ru.smirnov.muteworkingchats.holder.ClientHolder
import ru.smirnov.muteworkingchats.util.QUERY_LIMIT

object GetFolderChatsWorker : WorkerI {
    fun work(folderId: Int) {
        awaitForComplete { future ->
            ClientHolder.getClient().send(TdApi.GetChats(TdApi.ChatListFolder(folderId), QUERY_LIMIT)) {
                if (it is TdApi.Chats) {
                    it.chatIds.forEachIndexed { i, chatId ->
                        val chat = ChatsHolder.getChat(chatId) ?: return@forEachIndexed
                        println("Чат ${i + 1}: ${chat.title} [${chat.id}]")
                    }
                } else {
                    println("Received unknown object [${it.javaClass.simpleName}]")
                }
                future.complete(Unit)
            }
        }
    }
}
