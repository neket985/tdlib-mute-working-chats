package ru.smirnov.muteworkingchats.worker

import me.tongfei.progressbar.ProgressBar
import ru.smirnov.muteworkingchats.TdApi
import ru.smirnov.muteworkingchats.TdApi.BoostChat
import ru.smirnov.muteworkingchats.holder.ChatsHolder
import ru.smirnov.muteworkingchats.holder.ClientHolder
import ru.smirnov.muteworkingchats.util.QUERY_LIMIT
import ru.smirnov.muteworkingchats.util.UNMUTE_FOR_VALUE
import ru.smirnov.muteworkingchats.util.requestHandler

object UnmuteFolderChatsWorker : WorkerI {
    fun work(folderId: Int) {
        val mutedChats = awaitForComplete<List<TdApi.Chat>> { future ->
            val chatIdsFile = MuteFolderChatsWorker.getChatIdsFile(folderId)
            if (chatIdsFile.exists().not() || chatIdsFile.readLines().none { it.isNotBlank() }) {
                println("Отсутствует файл, формируемый при переводе чатов в бесшумный режим")
                future.complete(emptyList())
            } else {
                val chatIdsForUnmute = chatIdsFile.readLines().mapNotNull { it.toLongOrNull() }.toSet()
                ClientHolder.getClient().send(TdApi.GetChats(TdApi.ChatListFolder(folderId), QUERY_LIMIT)) {
                    val mutedChats = if (it is TdApi.Chats) {
                        it.chatIds.toList().mapNotNull { chatId ->
                            ChatsHolder.getChat(chatId) ?: return@mapNotNull null
                        }.filter { chat ->
                            val isMuted = chat.notificationSettings.muteFor > 0
                            isMuted && chatIdsForUnmute.contains(chat.id)
                        }
                    } else {
                        System.err.println("Received unknown object [${it.javaClass.simpleName}]")
                        emptyList()
                    }
                    future.complete(mutedChats)
                }
            }
        }

        if(mutedChats.isNotEmpty()) {
            ProgressBar.wrap(mutedChats, "Unmute chats").forEach { chat ->
                awaitForComplete { f ->
                    ClientHolder.getClient().send(
                        TdApi.SetChatNotificationSettings(
                            chat.id,
                            chat.notificationSettings.apply {
                                useDefaultMuteFor = true
                                muteFor = UNMUTE_FOR_VALUE
                            },
                        ),
                        requestHandler(f)
                    )
                }
            }
        }
        println("Чаты выведены из беззвучного режима")
    }
}
