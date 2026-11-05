package com.krzyzowski.knowledgeboost.content

import android.content.ContentResolver
import android.content.Intent
import android.provider.DocumentsContract
import androidx.core.net.toUri
import java.io.IOException

class DocumentTopicFolder(private val resolver: ContentResolver) : TopicFolderSource {
    override fun read(uri: String): List<TopicFile> {
        val tree = uri.toUri()
        val documentId = DocumentsContract.getTreeDocumentId(tree)
        val root = DocumentsContract.buildDocumentUriUsingTree(tree, documentId)
        resolver.query(root, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID), null, null, null).use {
            if (it == null || !it.moveToFirst()) {
                throw IOException("The topic folder is missing or inaccessible. Select the folder again.")
            }
        }
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, documentId)
        val columns = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )
        val files = mutableListOf<TopicFile>()
        val cursor = resolver.query(children, columns, null, null, null)
            ?: throw IOException("The topic folder could not be opened. Select the folder again.")
        cursor.use {
            if (it.extras.getBoolean(DocumentsContract.EXTRA_LOADING)) {
                throw IOException("The topic folder is still loading. Try Reload topics again.")
            }
            val idColumn = it.getColumnIndexOrThrow(columns[0])
            val nameColumn = it.getColumnIndexOrThrow(columns[1])
            val typeColumn = it.getColumnIndexOrThrow(columns[2])
            while (it.moveToNext()) {
                if (it.getString(typeColumn) == DocumentsContract.Document.MIME_TYPE_DIR) continue
                val name = it.getString(nameColumn) ?: throw IOException("A file in the topic folder has no name.")
                if (!name.endsWith(".json", ignoreCase = true)) continue
                val document = DocumentsContract.buildDocumentUriUsingTree(tree, it.getString(idColumn))
                val stream = resolver.openInputStream(document) ?: throw IOException("$name: the file could not be opened.")
                files += TopicFile(name, stream.bufferedReader(Charsets.UTF_8).use { reader -> reader.readText() })
            }
        }
        return files.sortedBy { it.name }
    }

    override fun retain(uri: String) {
        resolver.takePersistableUriPermission(uri.toUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    override fun release(uri: String) {
        resolver.releasePersistableUriPermission(uri.toUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}
