package sdc

import kotlin.jvm.Transient

data class Controls(
    @Transient var forward: Boolean = false,
    @Transient var left: Boolean = false,
    @Transient var right: Boolean = false,
    @Transient var reverse: Boolean = false,
)