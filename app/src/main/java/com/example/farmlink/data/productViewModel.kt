package com.example.farmlink.data

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmlink.models.Product
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ProductViewModel : ViewModel() {

    private val cloudinaryUrl = "https://api.cloudinary.com/v1_1/du2pop4gk/image/upload"
    private val uploadPreset = "farmlink"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val db = FirebaseDatabase.getInstance().getReference("products")

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts: StateFlow<List<Product>> = _allProducts

    private val _myProducts = MutableStateFlow<List<Product>>(emptyList())
    val myProducts: StateFlow<List<Product>> = _myProducts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // ✅ FETCH ALL PRODUCTS (Feed) — only shows available products
    fun fetchAllProducts() {
        _isLoading.value = true
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Product>()
                snapshot.children.forEach { userNode ->
                    userNode.children.forEach { productNode ->
                        val product = productNode.getValue(Product::class.java)
                        // ✅ Hide sold out products from feed
                        if (product != null && product.isAvailable) list.add(product)
                    }
                }
                _allProducts.value = list.sortedByDescending { it.postedDate }
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
            }
        })
    }

    // ✅ FETCH MY PRODUCTS (Profile) — shows all including sold out
    fun fetchMyProducts(uid: String) {
        _isLoading.value = true
        db.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Product>()
                snapshot.children.forEach { productNode ->
                    val product = productNode.getValue(Product::class.java)
                    if (product != null) list.add(product)
                }
                _myProducts.value = list.sortedByDescending { it.postedDate }
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
            }
        })
    }

    // ✅ ADD PRODUCT — now includes quantity, unit, location, isAvailable
    fun addProduct(
        uid: String,
        imageUri: Uri?,
        name: String,
        price: String,
        category: String,
        description: String,
        expiryDate: String,
        postedDate: String,
        sellerUsername: String,
        quantity: String,
        unit: String,
        location: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (name.isBlank() || price.isBlank()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Name and price are required.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) { _isLoading.value = true }

                val imageUrl = if (imageUri != null) {
                    try {
                        uploadToCloudinary(context, imageUri)
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Image upload failed, listing without image.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        ""
                    }
                } else ""

                val ref = db.child(uid).push()
                val product = Product(
                    id = ref.key ?: "",
                    name = name,
                    price = price,
                    category = category,
                    description = description,
                    imageUrl = imageUrl,
                    expiryDate = expiryDate,
                    postedDate = postedDate,
                    sellerUsername = sellerUsername,
                    sellerUid = uid,
                    quantity = quantity,
                    unit = unit,
                    location = location,
                    isAvailable = true  // ✅ always available on creation
                )
                ref.setValue(product).await()

                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    Toast.makeText(context, "Product listed successfully!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    Toast.makeText(
                        context,
                        "Failed to list product: ${e.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // ✅ DELETE PRODUCT
    fun deleteProduct(uid: String, productId: String, context: Context) {
        db.child(uid).child(productId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Product deleted.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Delete failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ✅ UPDATE PRODUCT — now includes quantity, unit, location
    fun updateProduct(
        uid: String,
        productId: String,
        newImageUri: Uri?,
        existingImageUrl: String,
        name: String,
        price: String,
        category: String,
        description: String,
        quantity: String,
        unit: String,
        location: String,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageUrl = if (newImageUri != null) {
                    try {
                        uploadToCloudinary(context, newImageUri)
                    } catch (e: Exception) {
                        existingImageUrl
                    }
                } else existingImageUrl

                val updates = mapOf(
                    "name" to name,
                    "price" to price,
                    "category" to category,
                    "description" to description,
                    "imageUrl" to imageUrl,
                    "quantity" to quantity,
                    "unit" to unit,
                    "location" to location
                )

                db.child(uid).child(productId).updateChildren(updates).await()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Listing updated!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                    onFailure()
                }
            }
        }
    }

    // ✅ TOGGLE AVAILABILITY — Live ↔ Sold Out
    fun toggleAvailability(uid: String, productId: String, current: Boolean, context: Context) {
        db.child(uid).child(productId).child("isAvailable").setValue(!current)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    if (!current) "Marked as Available ✓" else "Marked as Sold Out",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update availability", Toast.LENGTH_SHORT).show()
            }
    }

    // ✅ CLOUDINARY UPLOAD
    private fun uploadToCloudinary(context: Context, uri: Uri): String {
        val cacheFile = java.io.File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")

        context.contentResolver.openInputStream(uri)?.use { input ->
            cacheFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("Could not open image. Please try again.")

        val fileBytes = cacheFile.readBytes()
        cacheFile.delete()

        if (fileBytes.isEmpty()) throw Exception("Selected image is empty.")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "product_${System.currentTimeMillis()}.jpg",
                fileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .addFormDataPart("upload_preset", uploadPreset)
            .build()

        val request = Request.Builder()
            .url(cloudinaryUrl)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful) {
            throw Exception("Cloudinary error ${response.code}: ${responseBody?.take(200)}")
        }

        return Regex("\"secure_url\":\"(.*?)\"")
            .find(responseBody ?: "")
            ?.groupValues?.get(1)
            ?.replace("\\/", "/")
            ?: throw Exception("Image URL not found in response.")
    }

    // ✅ SAFE STREAM READER
    private fun readAllBytes(stream: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()
        val chunk = ByteArray(4096)
        var bytesRead: Int
        while (stream.read(chunk).also { bytesRead = it } != -1) {
            buffer.write(chunk, 0, bytesRead)
        }
        return buffer.toByteArray()
    }

    // ✅ DELETE EXPIRED PRODUCTS
    fun deleteExpiredProducts() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        db.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { userNode ->
                userNode.children.forEach { productNode ->
                    val expiryStr = productNode.child("expiryDate")
                        .getValue(String::class.java) ?: return@forEach
                    try {
                        val expiryDate = sdf.parse(expiryStr) ?: return@forEach
                        if (expiryDate.before(today)) {
                            productNode.ref.removeValue()
                        }
                    } catch (_: Exception) {
                        // Malformed date — skip silently
                    }
                }
            }
        }.addOnFailureListener {
            // Silently fail — non-critical
        }
    }
}