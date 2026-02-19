package rocks.breaker.jmri_throttle

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform