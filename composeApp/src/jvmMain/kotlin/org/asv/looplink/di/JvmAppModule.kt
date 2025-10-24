package org.asv.looplink.di

import org.asv.looplink.DriverFactory
import org.asv.looplink.network.ServerManager
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.viewmodel.MainViewModel
import org.asv.looplink.webDriver.cuimsAPI
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DriverFactory() }
    single { cuimsAPI() }
    single { LANServiceDiscovery().apply { initialize() } }
    single { ServerManager() }
    single {
        MainViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
