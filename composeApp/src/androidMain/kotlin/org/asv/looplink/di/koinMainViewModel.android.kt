package org.asv.looplink.di

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import org.asv.looplink.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
actual fun koinMainViewModel(): MainViewModel {
    val activity = LocalActivity.current as ComponentActivity
    return koinViewModel(viewModelStoreOwner = activity)
}