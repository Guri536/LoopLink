package org.asv.looplink.typesalias

/**
 * A common declaration for the Parcelable interface.
 * On Android, this will be a typealias for the real android.os.Parcelable.
 * On other platforms, it will be a simple empty interface.
 */
expect interface KmpParcelable

/**
 * A common declaration for the Parcelize annotation.
 * On Android, this will be a typealias for kotlinx.parcelize.Parcelize.
 * On other platforms, it will be a simple empty annotation.
 */
expect annotation class KmpParcelize()

/**
 * A common declaration for the IgnoreOnParcel annotation.
 * On Android, this will be a typealias for kotlinx.parcelize.IgnoreOnParcel.
 * On other platforms, it's a simple empty annotation.
 */
expect annotation class KmpIgnoreOnParcel()