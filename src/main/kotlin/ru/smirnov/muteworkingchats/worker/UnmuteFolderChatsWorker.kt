package ru.smirnov.muteworkingchats.worker

import ru.smirnov.muteworkingchats.TdApi
import ru.smirnov.muteworkingchats.holder.ChatsHolder
import ru.smirnov.muteworkingchats.holder.ClientHolder
import ru.smirnov.muteworkingchats.util.QUERY_LIMIT
import ru.smirnov.muteworkingchats.util.UNMUTE_FOR_VALUE
import ru.smirnov.muteworkingchats.util.requestHandler

object UnmuteFolderChatsWorker : WorkerI {
    fun work(folderId: Int) {
        awaitForComplete { future ->
            val chatIdsFile = MuteFolderChatsWorker.getChatIdsFile(folderId)
            if (chatIdsFile.exists().not() || chatIdsFile.readLines().none { it.isNotBlank() }) {
                println("Отсутствует файл, формируемый при переводе чатов в бесшумный режим")
            } else {
                val chatIdsForUnmute = chatIdsFile.readLines().mapNotNull { it.toLongOrNull() }.toSet()
                ClientHolder.getClient().send(TdApi.GetChats(TdApi.ChatListFolder(folderId), QUERY_LIMIT)) {
                    if (it is TdApi.Chats) {
                        val mutedChats = it.chatIds.toList().mapNotNull { chatId ->
                            ChatsHolder.getChat(chatId) ?: return@mapNotNull null
                        }.filter { chat ->
                            val isMuted = chat.notificationSettings.muteFor > 0
                            isMuted && chatIdsForUnmute.contains(chat.id)
                        }

                        mutedChats.forEach { chat ->
                            ClientHolder.getClient().send(
                                TdApi.SetChatNotificationSettings(
                                    chat.id,
                                    chat.notificationSettings.apply {
                                        muteFor = UNMUTE_FOR_VALUE
                                    },
                                ),
                                requestHandler
                            )
                        }
                    } else {
                        println("Received unknown object [${it.javaClass.simpleName}]")
                    }
                    future.complete(Unit)
                }
            }
        }
        println("Чаты выведены из беззвучного режима")
    }
}
