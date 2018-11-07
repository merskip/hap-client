package pl.merskip.homekitcollector

import org.slf4j.LoggerFactory


fun <T> loggerFor(clazz: Class<T>) = LoggerFactory.getLogger(clazz)
