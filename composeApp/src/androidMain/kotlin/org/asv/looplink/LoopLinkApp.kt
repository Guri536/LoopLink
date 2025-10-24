
package org.asv.looplink

import android.app.Application
import org.asv.looplink.di.commonModule
import org.asv.looplink.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LoopLinkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LoopLinkApp)
            modules(commonModule + platformModule())
        }
    }
}
