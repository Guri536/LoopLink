
package org.asv.looplink.di

import org.asv.looplink.DatabaseMng
import org.asv.looplink.DriverFactory
import org.asv.looplink.data.repository.UserRespository
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule = module {
    single { DatabaseMng(get<DriverFactory>().createDriver()) }
    single { ChatViewModel() }
    single { UserRespository() }
    single { PeerDiscoveryViewModel(get(), get()) }
}

expect fun platformModule(): Module
