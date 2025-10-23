package org.asv.looplink.data.model

data class UserModel(
    var name: String,
    var uid: String,
    var section: String? = null,
    var program: String? = null,
    var contact: String? = null,
    var cGPA: String? = null,
    var email: String? = null,
    var picture: ByteArray? = null
    ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserModel

        if (name != other.name) return false
        if (uid != other.uid) return false
        if (section != other.section) return false
        if (program != other.program) return false
        if (contact != other.contact) return false
        if (cGPA != other.cGPA) return false
        if (email != other.email) return false
        if (!picture.contentEquals(other.picture)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + uid.hashCode()
        result = 31 * result + (section?.hashCode() ?: 0)
        result = 31 * result + (program?.hashCode() ?: 0)
        result = 31 * result + (contact?.hashCode() ?: 0)
        result = 31 * result + (cGPA?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (picture?.contentHashCode() ?: 0)
        return result
    }
}