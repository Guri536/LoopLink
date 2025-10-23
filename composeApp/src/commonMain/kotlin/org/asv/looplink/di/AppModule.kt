
package org.asv.looplink.di

import org.asv.looplink.DatabaseMng
import org.asv.looplink.DriverFactory
import org.asv.looplink.MainViewModel
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.webDriver.cuimsAPI
import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule = module {
    single { DatabaseMng(get<DriverFactory>().createDriver()) }
    single { ChatViewModel() }
}

expect fun platformModule(): Module
