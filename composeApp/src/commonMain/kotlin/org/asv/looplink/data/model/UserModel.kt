package org.asv.looplink.data.model

@kotlinx.serialization.Serializable
data class UserModel(
    var name: String,
    var uid: String,
    var section: String? = null,
    var program: String? = null,
    var contact: String? = null,
    var cGPA: String? = null,
    var email: String? = null,
    var pfpPath: String? = null
)