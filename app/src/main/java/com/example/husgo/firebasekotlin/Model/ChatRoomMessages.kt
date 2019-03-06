package com.example.husgo.firebasekotlin.Model

import android.support.annotation.Keep

@Keep
class ChatRoomMessages {

    var id: String? = null
    var content: String? = null
    var send_user_id: String? = null
    var timestamp: String? = null

    constructor() {

    }

    constructor(id: String, content: String, send_user_id: String, timestamp: String) {
        this.id = id
        this.content = content
        this.send_user_id = send_user_id
        this.timestamp = timestamp
    }
}
