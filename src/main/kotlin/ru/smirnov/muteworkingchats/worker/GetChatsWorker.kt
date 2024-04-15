package ru.smirnov.muteworkingchats.worker

import ru.smirnov.muteworkingchats.holder.ChatsHolder

object GetChatsWorker : WorkerI {
    fun work() {
        ChatsHolder.getChats().forEachIndexed { i, chat ->
            println("Чат ${i + 1}: ${chat.title} [${chat.id}]")
        }
    }
}
