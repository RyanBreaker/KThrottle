package rocks.breaker.kthrottle

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
