package com.example.usbprintertest

class PrintBuilder {

    private var content = ""

    fun text(content: String): PrintBuilder {
        this.content = content
        return this
    }

    fun build() {

    }
}