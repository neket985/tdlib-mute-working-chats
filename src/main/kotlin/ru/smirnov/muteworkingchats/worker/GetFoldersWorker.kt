package ru.smirnov.muteworkingchats.worker

import ru.smirnov.muteworkingchats.holder.FoldersHolder

object GetFoldersWorker : WorkerI {
    fun work() {
        FoldersHolder.getFolders().forEachIndexed { i, folder ->
            println("Папка ${i + 1}: ${folder.title} [${folder.id}]")
        }
    }
}
