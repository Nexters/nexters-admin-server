package nexters.admin.domain.user

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nexters.admin.support.extensions.sha256Encrypt
import javax.persistence.Column
import javax.persistence.Embeddable

private class PasswordDeserializer : JsonDeserializer<Password>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Password = Password(p.text)
}

private class PasswordSerializer : JsonSerializer<Password>() {
    override fun serialize(password: Password, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(password.value)
    }
}

@JsonSerialize(using = PasswordSerializer::class)
@JsonDeserialize(using = PasswordDeserializer::class)
@Embeddable
data class Password(
        @Column(name = "password", nullable = false)
        var value: String
) {
    init {
        value = value.sha256Encrypt()
    }

    fun isSamePassword(password: Password): Boolean {
        return this.value == password.value
    }
}
