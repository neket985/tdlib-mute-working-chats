package ru.smirnov.muteworkingchats.handler

import ru.smirnov.muteworkingchats.Client
import ru.smirnov.muteworkingchats.TdApi
import ru.smirnov.muteworkingchats.holder.AuthorizationStateHolder
import ru.smirnov.muteworkingchats.holder.ChatsHolder
import ru.smirnov.muteworkingchats.holder.FoldersHolder

object MainHandler : Client.ResultHandler {
    private val ignoredTypes = setOf(
        TdApi.UpdateOption.CONSTRUCTOR,
        TdApi.UpdateDefaultReactionType.CONSTRUCTOR,
        TdApi.UpdateAnimationSearchParameters.CONSTRUCTOR,
        TdApi.UpdateAccentColors.CONSTRUCTOR,
        TdApi.UpdateProfileAccentColors.CONSTRUCTOR,
        TdApi.UpdateSpeechRecognitionTrial.CONSTRUCTOR,
        TdApi.UpdateAttachmentMenuBots.CONSTRUCTOR,
        TdApi.UpdateDefaultBackground.CONSTRUCTOR,
        TdApi.UpdateFileDownloads.CONSTRUCTOR,
        TdApi.UpdateDiceEmojis.CONSTRUCTOR,
        TdApi.UpdateActiveEmojiReactions.CONSTRUCTOR,
        TdApi.UpdateChatThemes.CONSTRUCTOR,
        TdApi.UpdateScopeNotificationSettings.CONSTRUCTOR,
        TdApi.UpdateUnreadMessageCount.CONSTRUCTOR,
        TdApi.UpdateUnreadChatCount.CONSTRUCTOR,
        TdApi.UpdateStoryStealthMode.CONSTRUCTOR,
        TdApi.UpdateHavePendingNotifications.CONSTRUCTOR,
        TdApi.UpdateConnectionState.CONSTRUCTOR,
        TdApi.UpdateUser.CONSTRUCTOR,
        TdApi.UpdateUserStatus.CONSTRUCTOR,
    )

    override fun onResult(obj: TdApi.Object) {
        when (obj.constructor) {
            TdApi.UpdateAuthorizationState.CONSTRUCTOR -> {
                AuthorizationStateHolder.onAuthorizationStateUpdated((obj as TdApi.UpdateAuthorizationState).authorizationState)
            }
            TdApi.UpdateChatFolders.CONSTRUCTOR -> {
                val update = obj as TdApi.UpdateChatFolders
                FoldersHolder.updateState(update)
            }
            TdApi.UpdateNewChat.CONSTRUCTOR -> {
                val update = obj as TdApi.UpdateNewChat
                ChatsHolder.updateState(update)
            }
            TdApi.UpdateChatNotificationSettings.CONSTRUCTOR -> {
                val update = obj as TdApi.UpdateChatNotificationSettings
                ChatsHolder.updateState(update)
            }
            in ignoredTypes -> {}
            else -> {
//                println("missed event ${obj::class.java.simpleName}")
            }
        }
    }
}
