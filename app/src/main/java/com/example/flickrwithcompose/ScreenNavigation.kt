package com.example.flickrwithcompose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

sealed class ScreenNavigation(val route: String) {
    object SearchScreen: ScreenNavigation(route = "search")
    data class DetailScreen(val imageData : ImageData): ScreenNavigation(route = "ImageDetailView/${imageData.toRouteString()}")
}
// Function to create DetailScreen instance with dynamic route
fun navigateToDetail(imageData: ImageData): ScreenNavigation {
    return ScreenNavigation.DetailScreen(imageData)
}
