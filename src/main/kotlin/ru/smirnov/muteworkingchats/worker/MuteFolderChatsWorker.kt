package ru.smirnov.muteworkingchats.worker

import ru.smirnov.muteworkingchats.TdApi
import ru.smirnov.muteworkingchats.holder.ChatsHolder
import ru.smirnov.muteworkingchats.holder.ClientHolder
import ru.smirnov.muteworkingchats.holder.PropertiesHolder
import ru.smirnov.muteworkingchats.util.MUTE_FOR_VALUE
import ru.smirnov.muteworkingchats.util.QUERY_LIMIT
import ru.smirnov.muteworkingchats.util.requestHandler
import java.io.File

object MuteFolderChatsWorker : WorkerI {
    fun work(folderId: Int) {
        awaitForComplete { future ->
            ClientHolder.getClient().send(TdApi.GetChats(TdApi.ChatListFolder(folderId), QUERY_LIMIT)) {
                if (it is TdApi.Chats) {
                    val notMutedChats = it.chatIds.toList().mapNotNull { chatId ->
                        ChatsHolder.getChat(chatId) ?: return@mapNotNull null
                    }.filter { chat ->
                        val isMuted = chat.notificationSettings.muteFor > 0
                        isMuted.not() && chat.id != PropertiesHolder.tgUserId
                    }
                    if (notMutedChats.isEmpty()) {
                        println("Все чаты в указанной папке уже на беззвучном режиме")
                    } else {
                        getChatIdsFile(folderId).apply {
                            writeText(notMutedChats.joinToString("\n") { it.id.toString() })
                        }

                        notMutedChats.forEach { chat ->
                            ClientHolder.getClient().send(
                                TdApi.SetChatNotificationSettings(
                                    chat.id,
                                    chat.notificationSettings.apply {
                                        muteFor = MUTE_FOR_VALUE
                                    },
                                ),
                                requestHandler,
                            )
                        }
                    }
                } else {
                    println("Received unknown object [${it.javaClass.simpleName}]")
                }
                future.complete(Unit)
            }
        }
        println("Чаты переведены в беззвучный режим")
    }

    fun getChatIdsFile(folderId: Int) =
        File("${PropertiesHolder.chatIdsForUnmuteFilePrefix}$folderId.txt")
}
