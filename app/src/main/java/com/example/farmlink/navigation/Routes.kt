package com.example.farmlink.navigation

const val ROUTE_LOGIN = "login"
const val ROUTE_REGISTER = "register"
const val ROUTE_FEED = "feed"
const val ROUTE_PROFILE = "profile"
const val ROUTE_ADD_PRODUCT = "add_product"

const val ROUTE_UPDATE_PRODUCT = "update_product/{productId}"
fun updateProductRoute(productId: String) = "update_product/$productId"