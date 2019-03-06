package com.example.husgo.firebasekotlin.Model

import android.support.annotation.Keep

@Keep
class Users {
    var isim: String? = null
    var user_id: String? = null
    var profile_image: String? = null
    var seviye: String? = null
    var phone: String? = null
    var token_key:String?=null
    constructor(isim: String, user_id: String, profile_image: String, seviye: String, phone: String) {
        this.isim = isim
        this.user_id = user_id
        this.profile_image = profile_image
        this.seviye = seviye
        this.phone = phone
    }

    constructor() {}
}
