package ru.smirnov.muteworkingchats.worker

import me.tongfei.progressbar.ProgressBar
import ru.smirnov.muteworkingchats.TdApi
import ru.smirnov.muteworkingchats.holder.ChatsHolder
import ru.smirnov.muteworkingchats.holder.ClientHolder
import ru.smirnov.muteworkingchats.holder.PropertiesHolder
import ru.smirnov.muteworkingchats.util.MUTE_FOR_3D
import ru.smirnov.muteworkingchats.util.QUERY_LIMIT
import ru.smirnov.muteworkingchats.util.requestHandler
import java.io.File

object MuteFolderChatsWorker : WorkerI {
    fun work(folderId: Int) {
        val notMutedChats = awaitForComplete { future ->
            ClientHolder.getClient().send(TdApi.GetChats(TdApi.ChatListFolder(folderId), QUERY_LIMIT)) {
                val notMutedChats = if (it is TdApi.Chats) {
                    it.chatIds.toList().mapNotNull { chatId ->
                        ChatsHolder.getChat(chatId) ?: return@mapNotNull null
                    }.filter { chat ->
                        val isMuted = chat.notificationSettings.muteFor > 0
                        isMuted.not() && chat.id != PropertiesHolder.tgUserId
                    }
                } else {
                    println("Received unknown object [${it.javaClass.simpleName}]")
                    emptyList()
                }
                future.complete(notMutedChats)
            }
        }

        if (notMutedChats.isEmpty()) {
            println("Все чаты в указанной папке уже на беззвучном режиме")
        } else {
            getChatIdsFile(folderId).apply {
                val existingChatIds = if (this.exists()) {
                    readLines().mapNotNull { it.toLongOrNull() }.toSet()
                } else emptySet()

                val notMutedChatsIds = notMutedChats.map { it.id }
                writeText(existingChatIds.plus(notMutedChatsIds).joinToString("\n"))
            }

//            ProgressBar.wrap(notMutedChats, "Mute chats").forEach { chat ->
            notMutedChats.forEach { chat ->
                awaitForComplete { f ->
                    ClientHolder.getClient().send(
                        TdApi.SetChatNotificationSettings(
                            chat.id,
                            chat.notificationSettings.apply {
                                useDefaultMuteFor = false
                                muteFor = MUTE_FOR_3D
                            },
                        ),
                        requestHandler(f),
                    )
                }
            }
        }
        println("Чаты переведены в беззвучный режим")
    }

    fun getChatIdsFile(folderId: Int) =
        File("${PropertiesHolder.chatIdsForUnmuteFilePrefix}$folderId.txt")
}
