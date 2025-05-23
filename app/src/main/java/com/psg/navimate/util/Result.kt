package com.psg.navimate.util

data class Result<T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T)      = Result(Status.SUCCESS, data, null)
        fun <T> loading()             = Result(Status.LOADING, null, null)
        fun <T> error(message: String) = Result(Status.ERROR, null, message)
    }
}


enum class Status {
    SUCCESS,
    LOADING,
    ERROR
}