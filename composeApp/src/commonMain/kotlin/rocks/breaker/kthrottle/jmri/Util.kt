package rocks.breaker.kthrottle.jmri

fun getAddressType(address: Int) = if (address < 128) "S" else "L"
