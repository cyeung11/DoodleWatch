package com.jkjk.doodlewatch.core

import com.jkjk.doodlewatch.core.act.BaseAct
import com.jkjk.doodlewatch.core.act.CommunicationAct
import dagger.Component

/**
 *Created by chrisyeung on 8/4/2021.
 */
@Component(modules = [CommunicationModule::class])
interface CommunicationComponent {
    fun inject(act: CommunicationAct)
}