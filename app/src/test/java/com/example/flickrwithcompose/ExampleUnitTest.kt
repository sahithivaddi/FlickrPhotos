import com.example.flickrwithcompose.fetchImages
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

class ExampleUnitTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testFetchImages() {
        // Given
        val searchText = "porcupine" // Search text for fetching cat images
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(getJson("mockData.json")) // Read the JSON file containing mock response
        mockWebServer.enqueue(mockResponse)

        // When
        val images = runBlocking {
            fetchImages(searchText)
        }

        // Then
        Assert.assertTrue(images.isNotEmpty()) // Check if the list of images is not empty
    }

    // Function to read JSON file from resources directory
    private fun getJson(path: String): String {
        val uri = javaClass.classLoader?.getResource(path)
        if (uri != null) {
            val file = File(uri.path)
            if (file.exists()) {
                return String(file.readBytes())
            } else {
                throw FileNotFoundException("File not found at path: $path")
            }
        } else {
            throw IllegalArgumentException("Resource not found: $path")
        }
    }
}