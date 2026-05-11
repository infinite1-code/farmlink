package com.example.farmlink.ui.theme.screens.addproduct

import android.app.DatePickerDialog
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
import coil.compose.rememberAsyncImagePainter
import com.example.farmlink.data.ProductViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

private val Lime = Color(0xFFB6FF00)
private val DarkBg = Color(0xFF0A0A0A)
private val CardBg = Color(0xFF1A1A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid ?: ""

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isLoading by productViewModel.isLoading.collectAsState()

    var sellerUsername by remember { mutableStateOf("") }
    var usernameLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            FirebaseDatabase.getInstance()
                .getReference("Users/$uid/username")
                .get()
                .addOnSuccessListener { snap ->
                    sellerUsername = snap.getValue(String::class.java) ?: ""
                    usernameLoaded = true
                }
                .addOnFailureListener { usernameLoaded = true }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                expiryDate = String.format("%02d/%02d/%04d", day, month + 1, year)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
            show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // ── Top bar ──────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                "List a Product",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (usernameLoaded && sellerUsername.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, null, tint = Lime,
                    modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Listing as $sellerUsername", color = Lime, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Image picker ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardBg)
                .border(
                    width = 1.dp,
                    color = if (imageUri != null) Lime else Color(0xFF333333),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUri),
                    contentDescription = "Product image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, null, tint = Lime,
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Change", color = Lime, fontSize = 12.sp)
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2A2A2A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddAPhoto, null,
                            tint = Lime.copy(alpha = 0.7f),
                            modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Tap to add product photo", color = Color.White,
                        fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text("Optional — JPG or PNG", color = Color(0xFF555555), fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Product Details ───────────────────────────────────
        SectionLabel("Product Details")
        Spacer(Modifier.height(14.dp))

        ProductTextField(value = name, label = "Product Name *") { name = it }
        Spacer(Modifier.height(12.dp))

        ProductTextField(value = category, label = "Category (e.g. Vegetables, Grains, Dairy)") {
            category = it
        }
        Spacer(Modifier.height(12.dp))

        ProductTextField(
            value = description,
            label = "Description",
            singleLine = false
        ) { description = it }

        Spacer(Modifier.height(20.dp))

        // ── Pricing & Quantity ────────────────────────────────
        SectionLabel("Pricing & Quantity")
        Spacer(Modifier.height(14.dp))

        // Price + Quantity side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProductTextField(
                value = price,
                label = "Price (KES) *",
                modifier = Modifier.weight(1f)
            ) { price = it }

            ProductTextField(
                value = quantity,
                label = "Quantity *",
                modifier = Modifier.weight(1f)
            ) { quantity = it }
        }

        Spacer(Modifier.height(12.dp))

        // Unit field
        ProductTextField(
            value = unit,
            label = "Unit (e.g. kg, bags, pieces, litres, bunches)"
        ) { unit = it }

        Spacer(Modifier.height(20.dp))

        // ── Location & Expiry ─────────────────────────────────
        SectionLabel("Location & Availability")
        Spacer(Modifier.height(14.dp))

        // Location
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

        Spacer(Modifier.height(12.dp))

        // Expiry date
        OutlinedTextField(
            value = expiryDate,
            onValueChange = {},
            label = { Text("Expiry Date *", color = Color.Gray) },
            placeholder = { Text("dd/mm/yyyy", color = Color(0xFF555555)) },
            readOnly = true,
            leadingIcon = {
                Icon(Icons.Default.CalendarToday, null, tint = Lime,
                    modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.EditCalendar, null, tint = Lime)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            colors = productTextFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )

        if (expiryDate.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = Color(0xFF888888),
                    modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text("This listing auto-deletes on $expiryDate",
                    color = Color(0xFF888888), fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── Submit ────────────────────────────────────────────
        val isFormValid = usernameLoaded &&
                expiryDate.isNotEmpty() &&
                name.isNotBlank() &&
                price.isNotBlank() &&
                quantity.isNotBlank() &&
                location.isNotBlank()

        Button(
            onClick = {
                val postedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                productViewModel.addProduct(
                    uid = uid,
                    imageUri = imageUri,
                    name = name,
                    price = price,
                    category = category,
                    description = description,
                    expiryDate = expiryDate,
                    postedDate = postedDate,
                    sellerUsername = sellerUsername,
                    quantity = quantity,
                    unit = unit,
                    location = location,
                    context = context,
                    onSuccess = { navController.popBackStack() }
                )
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) Lime else Color(0xFF333333),
                contentColor = if (isFormValid) Color.Black else Color.Gray
            ),
            shape = RoundedCornerShape(14.dp),
            enabled = !isLoading && isFormValid
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(color = Color.Black,
                        modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Uploading...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            } else {
                Text("List Product", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Hints
        Spacer(Modifier.height(10.dp))
        when {
            !usernameLoaded -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = Lime, modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Loading your profile...", color = Color.Gray, fontSize = 12.sp)
                }
            }
            !isFormValid -> {
                Text(
                    "⚠ Fill in all required (*) fields to list",
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .background(Lime, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = Lime, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun productTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = CardBg,
    unfocusedContainerColor = CardBg,
    focusedIndicatorColor = Lime,
    unfocusedIndicatorColor = Color.Gray,
    cursorColor = Lime,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Lime,
    unfocusedLabelColor = Color.Gray
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTextField(
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
fun AddProductScreenPreview() {
    AddProductScreen(navController = rememberNavController())
}