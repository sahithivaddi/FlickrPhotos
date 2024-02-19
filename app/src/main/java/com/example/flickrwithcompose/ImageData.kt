package com.example.flickrwithcompose

import java.net.URLEncoder

data class ImageData(
    var imageUrl: String,
    var title: String,
    var description: String,
    var author: String,
    var publishedDate: String
)
fun ImageData.toRouteString(): String {
    val encodedUrl = URLEncoder.encode(imageUrl, "UTF-8")

    return "$encodedUrl|$title|$description|$author|$publishedDate" // Delimited string representation
}