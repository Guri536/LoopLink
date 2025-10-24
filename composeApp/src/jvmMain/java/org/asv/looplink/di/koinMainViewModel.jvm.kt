package org.asv.looplink.di

import androidx.compose.runtime.Composable
import org.asv.looplink.viewmodel.MainViewModel
import org.koin.compose.koinInject
import org.koin.java.KoinJavaComponent.get

@Composable
actual fun koinMainViewModel(): MainViewModel = koinInject()
