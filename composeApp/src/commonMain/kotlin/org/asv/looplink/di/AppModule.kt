package org.asv.looplink.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.asv.looplink.DatabaseManager
import org.asv.looplink.DriverFactory
import org.asv.looplink.data.repository.ChatRepository
import org.asv.looplink.data.repository.UserRepository
import org.asv.looplink.network.ConnectionManager
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.math.sin

val commonModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    single { DatabaseManager(get<DriverFactory>().createDriver()) }
    single { ChatViewModel(get(), get(), get(), get()) }
    single { UserRepository(get(), get()) }
    single { ChatRepository(get()) }
    single { ConnectionManager(get()) }
    single {
        PeerDiscoveryViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}

expect fun platformModule(): Module
