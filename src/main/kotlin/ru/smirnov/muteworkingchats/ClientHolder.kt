package ru.smirnov.muteworkingchats

import ru.smirnov.muteworkingchats.handler.MainHandler

object ClientHolder {
    private var client: Client = createClient()

    fun getClient() = client

    fun recreateClient() {
        client = createClient()
    }

    fun createClient() = Client.create(MainHandler(), null, null)
}
