package ru.smirnov.muteworkingchats.holder

import ru.smirnov.muteworkingchats.TdApi
import ru.smirnov.muteworkingchats.TdApi.ChatFolderInfo

object FoldersHolder {
    private var folders = listOf<ChatFolderInfo>()

    fun updateState(update: TdApi.UpdateChatFolders) {
        folders = update.chatFolders.toList()
    }

    fun getFolders() = folders
}
