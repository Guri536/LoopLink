
package org.asv.looplink.di

import org.asv.looplink.DatabaseMng
import org.asv.looplink.DriverFactory
import org.asv.looplink.data.repository.ChatRepository
import org.asv.looplink.data.repository.UserRespository
import org.asv.looplink.network.ConnectionManager
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule = module {
    single { DatabaseMng(get<DriverFactory>().createDriver()) }
    single { ChatViewModel() }
    single { UserRespository() }
    single { ChatRepository() }
    single { ConnectionManager() }
    single { PeerDiscoveryViewModel(get(), get(), get(), get()) }
}

expect fun platformModule(): Module
