package com.example.husgo.firebasekotlin.Model

import android.support.annotation.Keep

@Keep
class Room {
    var id: String? = null
    var create_user_id: String? = null
    var name: String? = null
    var level: String? = null
    var message_list:List<ChatRoomMessages>?=null
    var isactive:Boolean?=null


    constructor() {

    }

    constructor(id: String, create_user_id: String, name: String, level: String,message_list:List<ChatRoomMessages>, isactive:Boolean) {
        this.id = id
        this.create_user_id = create_user_id
        this.name = name
        this.level = level
        this.message_list=message_list
        this.isactive=isactive
    }
}
