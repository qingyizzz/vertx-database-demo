package com.qinyi.demo.buildrestapi

class PlayerNotFoundException(id: Long) : RuntimeException("Player id: $id was not found. ")
