package com.example.husgo.firebasekotlin.Model

import android.support.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable
@Keep
class SendNotify {

    @SerializedName("to")
    var to: String? = null
    @SerializedName("data")
    var data: Data? = null

    constructor(to: String?, data: Data?) {
        this.to = to
        this.data = data
    }

    class Data {
        @SerializedName("content")
        var content: String? = null
        @SerializedName("title")
        var title: String? = null
        @SerializedName("notify_type")
        var notify_type: String? = null
        @SerializedName("chat_room_id")
        var chat_room_id: String? = null
        constructor(content: String?, title: String?, notify_type: String?,chat_room_id: String?) {
            this.content = content
            this.title = title
            this.notify_type = notify_type
            this.chat_room_id=chat_room_id
        }
    }
}
