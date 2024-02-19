package com.example.flickrwithcompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.flickrwithcompose.ui.theme.FlickrWithComposeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                    MyApp()
                }
            }
        }
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MyApp() {
    val navController = rememberNavController()
    val isLoading = remember { mutableStateOf(false) } // Add loading state

    NavHost(
        navController = navController,
        startDestination = ScreenNavigation.SearchScreen.route
    ) {
        composable(route = ScreenNavigation.SearchScreen.route) {
            // Define your ImageDetailView composable here
            // Example: ImageDetailView(navController = navController)
            SearchFlickrScreen(navController = navController)
        }

        composable(route = "ImageDetailView/{customObjectString}") {
            // Define your ImageDetailView composable here
            // Example: ImageDetailView(navController = navController)
            isLoading.value = true // Set loading state to true when navigating to the second screen
            ImageDetailView(navController = navController){
                isLoading.value = false
            }

        }
    }

    FlickrWithComposeTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SearchFlickrScreen(navController)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchFlickrScreen(navController: NavController) {
    var searchText by remember { mutableStateOf("") }
    var images by remember { mutableStateOf(emptyList<ImageData>()) }
    var isLoading by remember { mutableStateOf(false) }

// Call fetchImages inside a coroutine scope
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Enter search text") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        SearchButton(onClick = {
            coroutineScope.launch {
                isLoading = true
                val fetchedImages = fetchImages(searchText)
                images = fetchedImages
                isLoading = false
            }
        })

        Spacer(modifier = Modifier.height(16.dp))

        ImageGrid(images = images, navController = navController)
    }
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator() // Show progress indicator if loading
        }
    }
}


@Composable
fun SearchButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
    ) {
        Text("Search")
    }
}


@ExperimentalFoundationApi
@Composable
fun ImageGrid(images: List<ImageData>, navController: NavController) {
    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
        items(images) { imageData ->
            CoilImage(
                data = imageData,
                contentDescription = imageData.description,
                modifier = Modifier.aspectRatio(1f),
                navController = navController
            )
        }
    }
}

@Composable
fun CoilImage(
    data: ImageData,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    navController: NavController

) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                // Navigate to ImageDetailView with image data as arguments
                val routeWithArgs = navigateToDetail(data).route
                Log.d("tag","navBackStackEntry route :${routeWithArgs}")
                navController.navigate(routeWithArgs)

            }
    ) {
        val painter = rememberImagePainter(data.imageUrl)
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}


@Composable
fun ImageDetailView(navController: NavController,onLoaded: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val routeString = navBackStackEntry?.arguments?.getString("customObjectString")
    val (encodedUrl, titleVal , descriptionVal ,authorVal ,publishedDateVal) = routeString?.split("|") ?: listOf("","","","","")
    val imageData = ImageData(URLDecoder.decode(encodedUrl, "UTF-8"), titleVal,descriptionVal,authorVal,publishedDateVal)

    val routeWithArgs = navBackStackEntry?.destination?.route ?: "ImageDetailView"
    Log.d("tag","navBackStackEntry route :${routeWithArgs}")

    Log.d("tag","navBackStackEntry arg :${navBackStackEntry?.arguments}")
     val imageUrl = imageData.imageUrl
     val title = imageData.title
     val description =  imageData.description
    val author =  imageData.author
    val publishedDate = imageData.publishedDate
    Log.d("tag","navBackStackEntry imageData :${imageData}")

    // Display the image details using the extracted arguments
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Image(
            painter = rememberImagePainter(imageUrl),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Author: ${author}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Published Date: ${publishedDate}", style = MaterialTheme.typography.bodyMedium)
    }
    DisposableEffect(navController) {
        onDispose {
            onLoaded() // Invoke the callback when the composable is disposed
        }
    }
}

suspend fun fetchImages(searchText: String): List<ImageData> {
    return withContext(Dispatchers.IO) {
        val apiUrl =
            "https://api.flickr.com/services/feeds/photos_public.gne?format=json&nojsoncallback=1&tags=$searchText"
        val url = URL(apiUrl)
        val urlConnection = url.openConnection() as HttpURLConnection
        try {
            val inputStream = urlConnection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            var inputLine: String?
            while (bufferedReader.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
            bufferedReader.close()
            val jsonResponse = JSONObject(response.toString())
            val items = jsonResponse.getJSONArray("items")
            val urls = mutableListOf<ImageData>()
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val media = item.getJSONObject("media")
                val imageUrl = media.getString("m")
                val data: ImageData? = ImageData(
                    imageUrl = imageUrl,
                    author = "author",
                    description = "Description",
                    title = item.getString("title"),
                    publishedDate = item.getString("published")
                )

                data?.let { urls.add(it) }
            }
            urls
        } finally {
            urlConnection.disconnect()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FlickrWithComposeTheme {
        val navController = rememberNavController()
        SearchFlickrScreen(navController)
    }
}