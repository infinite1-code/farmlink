package com.example.farmlink.ui.theme.screens.updateproduct

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.farmlink.data.ProductViewModel
import com.example.farmlink.ui.theme.screens.addproduct.SectionLabel
import com.example.farmlink.ui.theme.screens.addproduct.productTextFieldColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private val Lime = Color(0xFFB6FF00)
private val DarkBg = Color(0xFF0A0A0A)
private val CardBg = Color(0xFF1A1A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductScreen(
    navController: NavController,
    productId: String,
    productViewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var existingImageUrl by remember { mutableStateOf("") }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var dataLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (uid.isNotEmpty() && productId.isNotEmpty()) {
            FirebaseDatabase.getInstance()
                .getReference("products/$uid/$productId")
                .get()
                .addOnSuccessListener { snap ->
                    name = snap.child("name").getValue(String::class.java) ?: ""
                    price = snap.child("price").getValue(String::class.java) ?: ""
                    category = snap.child("category").getValue(String::class.java) ?: ""
                    description = snap.child("description").getValue(String::class.java) ?: ""
                    quantity = snap.child("quantity").getValue(String::class.java) ?: ""
                    unit = snap.child("unit").getValue(String::class.java) ?: ""
                    location = snap.child("location").getValue(String::class.java) ?: ""
                    existingImageUrl = snap.child("imageUrl").getValue(String::class.java) ?: ""
                    dataLoaded = true
                }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> newImageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
            Text("Update Listing", color = Color.White, fontSize = 18.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(Modifier.height(20.dp))

        if (!dataLoaded) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Lime)
            }
        } else {

            // ── Image picker ──────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .border(
                        1.dp,
                        if (newImageUri != null) Lime else Color(0xFF333333),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    newImageUri != null -> {
                        Image(
                            painter = rememberAsyncImagePainter(model = newImageUri),
                            contentDescription = "New image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                        )
                        ChangePhotoOverlay(Modifier.align(Alignment.BottomEnd), "Change")
                    }
                    existingImageUrl.isNotEmpty() -> {
                        AsyncImage(
                            model = existingImageUrl,
                            contentDescription = "Current image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                        )
                        ChangePhotoOverlay(Modifier.align(Alignment.BottomEnd), "Replace photo")
                    }
                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null,
                                tint = Lime.copy(alpha = 0.7f), modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Tap to add photo", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Product Details ───────────────────────────────
            SectionLabel("Product Details")
            Spacer(Modifier.height(14.dp))

            UpdateTextField(value = name, label = "Product Name *") { name = it }
            Spacer(Modifier.height(12.dp))

            UpdateTextField(value = category, label = "Category") { category = it }
            Spacer(Modifier.height(12.dp))

            UpdateTextField(value = description, label = "Description", singleLine = false) {
                description = it
            }

            Spacer(Modifier.height(20.dp))

            // ── Pricing & Quantity ────────────────────────────
            SectionLabel("Pricing & Quantity")
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UpdateTextField(
                    value = price,
                    label = "Price (KES) *",
                    modifier = Modifier.weight(1f)
                ) { price = it }
                UpdateTextField(
                    value = quantity,
                    label = "Quantity *",
                    modifier = Modifier.weight(1f)
                ) { quantity = it }
            }

            Spacer(Modifier.height(12.dp))
            UpdateTextField(value = unit, label = "Unit (e.g. kg, bags, pieces)") { unit = it }

            Spacer(Modifier.height(20.dp))

            // ── Location ──────────────────────────────────────
            SectionLabel("Location")
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Farm Location *", color = Color.Gray) },
                placeholder = { Text("e.g. Kisii, Kenya", color = Color(0xFF555555)) },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, null, tint = Lime,
                        modifier = Modifier.size(18.dp))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = productTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(32.dp))

            // ── Save button ───────────────────────────────────
            Button(
                onClick = {
                    isLoading = true
                    productViewModel.updateProduct(
                        uid = uid,
                        productId = productId,
                        newImageUri = newImageUri,
                        existingImageUrl = existingImageUrl,
                        name = name,
                        price = price,
                        category = category,
                        description = description,
                        quantity = quantity,
                        unit = unit,
                        location = location,
                        context = context,
                        onSuccess = { isLoading = false; navController.popBackStack() },
                        onFailure = { isLoading = false }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Lime, contentColor = Color.Black),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading && name.isNotBlank() && price.isNotBlank()
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(color = Color.Black,
                            modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Saving...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun ChangePhotoOverlay(modifier: Modifier, label: String) {
    Box(
        modifier = modifier
            .padding(12.dp)
            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Edit, null, tint = Lime, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = Lime, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTextField(
    value: String,
    label: String,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 4,
        modifier = modifier,
        colors = productTextFieldColors(),
        shape = RoundedCornerShape(12.dp)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UpdateProductScreenPreview() {
    UpdateProductScreen(navController = rememberNavController(), productId = "")
}