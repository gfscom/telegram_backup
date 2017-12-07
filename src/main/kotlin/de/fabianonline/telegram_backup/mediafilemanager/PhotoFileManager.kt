/* Telegram_Backup
 * Copyright (C) 2016 Fabian Schlenz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package de.fabianonline.telegram_backup.mediafilemanager

import de.fabianonline.telegram_backup.UserManager
import de.fabianonline.telegram_backup.Database
import de.fabianonline.telegram_backup.StickerConverter
import de.fabianonline.telegram_backup.DownloadProgressInterface
import de.fabianonline.telegram_backup.DownloadManager

import com.github.badoualy.telegram.api.TelegramClient
import com.github.badoualy.telegram.tl.core.TLIntVector
import com.github.badoualy.telegram.tl.core.TLObject
import com.github.badoualy.telegram.tl.api.messages.TLAbsMessages
import com.github.badoualy.telegram.tl.api.messages.TLAbsDialogs
import com.github.badoualy.telegram.tl.api.*
import com.github.badoualy.telegram.tl.api.upload.TLFile
import com.github.badoualy.telegram.tl.exception.RpcErrorException
import com.github.badoualy.telegram.tl.api.request.TLRequestUploadGetFile

import java.io.IOException
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import java.util.LinkedList
import java.net.URL
import java.util.concurrent.TimeoutException

import org.apache.commons.io.FileUtils

class PhotoFileManager(msg: TLMessage, user: UserManager, client: TelegramClient) : AbstractMediaFileManager(msg, user, client) {
    private var photo: TLPhoto? = null
    private var size: TLPhotoSize? = null

    val extension: String
        get() = "jpg"

    val letter: String
        get() = "p"
    val name: String
        get() = "photo"
    val description: String
        get() = "Photo"

    init {
        val p = (msg.getMedia() as TLMessageMediaPhoto).getPhoto()
        if (p is TLPhoto) {
            this.photo = p as TLPhoto

            var biggest: TLPhotoSize? = null
            for (s in photo!!.getSizes())
                if (s is TLPhotoSize) {
                    val size = s as TLPhotoSize
                    if (biggest == null || size.getW() > biggest!!.getW() && size.getH() > biggest!!.getH()) {
                        biggest = size
                    }
                }
            if (biggest == null) {
                throw RuntimeException("Could not find a size for a photo.")
            }
            this.size = biggest
        } else if (p is TLPhotoEmpty) {
            this.isEmpty = true
        } else {
            throwUnexpectedObjectError(p)
        }
    }

    fun getSize(): Int {
        return if (size != null) size!!.getSize() else 0
    }

    @Throws(RpcErrorException::class, IOException::class, TimeoutException::class)
    fun download() {
        if (isEmpty) return
        val loc = size!!.getLocation() as TLFileLocation
        DownloadManager.downloadFile(client, getTargetPathAndFilename(), getSize(), loc.getDcId(), loc.getVolumeId(), loc.getLocalId(), loc.getSecret())
    }
}
