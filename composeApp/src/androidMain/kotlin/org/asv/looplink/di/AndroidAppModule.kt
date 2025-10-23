package org.asv.looplink.di

import android.webkit.WebView
import org.asv.looplink.DriverFactory
import org.asv.looplink.MainViewModel
import org.asv.looplink.network.AndroidKtorServer
import org.asv.looplink.network.ServerManager
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.webDriver.cuimsAPI
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DriverFactory(get()) }
    factory { WebView(get()) }
    single { cuimsAPI(get()) }
    single { LANServiceDiscovery(get())}
    single { AndroidKtorServer(get()) }
    single{ ServerManager(get()) }
    viewModel {
        MainViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
