package com.mbs.musicon.ExoPlayer

class FirebaseMusicSource {

    private val onReadyListener = mutableListOf<(Boolean) -> Unit>()

    private var state = State.STATE_CREATED
        set(value) {
            if(value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
                synchronized(onReadyListener) {
                    field = value
                    onReadyListener.forEach {
                        listner -> listner(state == State.STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if(state == State.STATE_CREATED || state == State.STATE_INITIALIZING) {
            onReadyListener += action
            return false
        } else {
            action(state == State.STATE_INITIALIZED)
            return true
        }
    }

}

enum class State{
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR,
}

