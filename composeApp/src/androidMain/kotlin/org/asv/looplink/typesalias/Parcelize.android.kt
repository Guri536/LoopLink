package org.asv.looplink.typesalias

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.IgnoredOnParcel

actual typealias KmpParcelable = android.os.Parcelable

actual typealias KmpParcelize = kotlinx.parcelize.Parcelize

// Add the new typealias for IgnoreOnParcel
actual typealias KmpIgnoreOnParcel = kotlinx.parcelize.IgnoredOnParcel