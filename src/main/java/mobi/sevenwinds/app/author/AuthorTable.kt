package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.joda.time.format.DateTimeFormat
import java.time.format.DateTimeFormatter

object AuthorTable : IntIdTable("author") {
    val fio = text("fio")
    val timestamp = datetime("timestamp")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fio by AuthorTable.fio
    var timestamp by AuthorTable.timestamp

    private val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")


    fun toResponse(): AuthorResponse {
        val formattedTimestamp = timestamp.toString(formatter)
        return AuthorResponse(fio, formattedTimestamp)
    }
}