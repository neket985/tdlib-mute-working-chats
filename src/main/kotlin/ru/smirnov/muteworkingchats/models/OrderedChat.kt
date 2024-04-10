package ru.smirnov.muteworkingchats.models

import ru.smirnov.muteworkingchats.TdApi.ChatPosition

class OrderedChat(
    val chatId: Long,
    val position: ChatPosition,
) : Comparable<OrderedChat> {

    override operator fun compareTo(other: OrderedChat): Int {
        if (position.order != other.position.order) {
            return if (other.position.order < position.order) -1 else 1
        }
        return if (chatId != other.chatId) {
            if (other.chatId < chatId) -1 else 1
        } else {
            0
        }
    }

    override fun equals(other: Any?): Boolean {
        val o = other as OrderedChat?
        return chatId == o!!.chatId && position.order == o.position.order
    }

    override fun hashCode(): Int {
        var result = chatId.hashCode()
        result = 31 * result + position.hashCode()
        return result
    }
}
