package com.example.farmlink.ui.theme.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.farmlink.data.ProductViewModel
import com.example.farmlink.models.Product
import com.example.farmlink.navigation.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private val Lime = Color(0xFFB6FF00)
private val DarkBg = Color(0xFF0A0A0A)
private val CardBg = Color(0xFF1A1A1A)
private val WhatsAppGreen = Color(0xFF25D366)

enum class FilterLevel { NONE, CATEGORY, NAME, SELLER }

@Composable
fun FeedScreen(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel()
) {
    val products by productViewModel.allProducts.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(FilterLevel.NONE) }
    var showSearch by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) { productViewModel.fetchAllProducts() }

    val categories by remember(products) {
        derivedStateOf {
            products.map { it.category.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()
        }
    }

    val filteredProducts by remember(products, searchQuery, activeFilter, selectedCategory) {
        derivedStateOf {
            var result = products
            when (activeFilter) {
                FilterLevel.CATEGORY -> {
                    if (selectedCategory.isNotEmpty()) {
                        result = result.filter {
                            it.category.equals(selectedCategory, ignoreCase = true)
                        }
                    }
                }
                FilterLevel.NAME -> {
                    if (searchQuery.isNotEmpty()) {
                        result = result.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }
                FilterLevel.SELLER -> {
                    if (searchQuery.isNotEmpty()) {
                        result = result.filter {
                            it.sellerUsername.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }
                FilterLevel.NONE -> {
                    if (searchQuery.isNotEmpty()) {
                        result = result.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.category.contains(searchQuery, ignoreCase = true) ||
                                    it.sellerUsername.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }
            }
            result
        }
    }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = { FeedBottomNav(navController, currentRoute = ROUTE_FEED) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Top bar ──────────────────────────────────────
            AnimatedContent(
                targetState = showSearch,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { searching ->
                if (searching) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    when (activeFilter) {
                                        FilterLevel.NAME -> "Search by product name..."
                                        FilterLevel.SELLER -> "Search by seller name..."
                                        else -> "Search products..."
                                    },
                                    color = Color(0xFF555555)
                                )
                            },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, null, tint = Lime,
                                    modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, null, tint = Color.Gray,
                                            modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { focusManager.clearFocus() }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = CardBg,
                                unfocusedContainerColor = CardBg,
                                focusedIndicatorColor = Lime,
                                unfocusedIndicatorColor = Color(0xFF333333),
                                cursorColor = Lime,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                showSearch = false
                                searchQuery = ""
                                activeFilter = FilterLevel.NONE
                                selectedCategory = ""
                                focusManager.clearFocus()
                            }
                        ) {
                            Text("Cancel", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🌱 FarmLink", color = Lime, fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold)
                            Text("Fresh produce, direct from farmers",
                                color = Color.Gray, fontSize = 12.sp)
                        }
                        IconButton(onClick = { showSearch = true }) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CardBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search",
                                    tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            LaunchedEffect(showSearch) {
                if (showSearch) focusRequester.requestFocus()
            }

            // ── Filter chips ──────────────────────────────────
            AnimatedVisibility(visible = showSearch) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChipItem(
                            label = "All",
                            icon = Icons.Default.GridView,
                            selected = activeFilter == FilterLevel.NONE,
                            onClick = { activeFilter = FilterLevel.NONE; selectedCategory = "" }
                        )
                        FilterChipItem(
                            label = "Category",
                            icon = Icons.Default.Category,
                            selected = activeFilter == FilterLevel.CATEGORY,
                            onClick = { activeFilter = FilterLevel.CATEGORY; searchQuery = "" }
                        )
                        FilterChipItem(
                            label = "Product",
                            icon = Icons.Default.Eco,
                            selected = activeFilter == FilterLevel.NAME,
                            onClick = { activeFilter = FilterLevel.NAME; selectedCategory = "" }
                        )
                        FilterChipItem(
                            label = "Seller",
                            icon = Icons.Default.Person,
                            selected = activeFilter == FilterLevel.SELLER,
                            onClick = { activeFilter = FilterLevel.SELLER; selectedCategory = "" }
                        )
                    }

                    AnimatedVisibility(visible = activeFilter == FilterLevel.CATEGORY) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp)
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategoryPill(
                                label = "All",
                                selected = selectedCategory.isEmpty(),
                                onClick = { selectedCategory = "" }
                            )
                            categories.forEach { cat ->
                                CategoryPill(
                                    label = cat,
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat }
                                )
                            }
                        }
                    }

                    if (searchQuery.isNotEmpty() || selectedCategory.isNotEmpty()) {
                        Text(
                            "${filteredProducts.size} result${if (filteredProducts.size != 1) "s" else ""} found",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // ── Product list ──────────────────────────────────
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Lime, strokeWidth = 3.dp)
                            Spacer(Modifier.height(12.dp))
                            Text("Loading fresh produce...", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }

                filteredProducts.isEmpty() && products.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌾", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No listings yet.\nBe the first to list!",
                                color = Color.Gray, fontSize = 15.sp,
                                textAlign = TextAlign.Center, lineHeight = 22.sp
                            )
                        }
                    }
                }

                filteredProducts.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", fontSize = 40.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No products match\n\"${searchQuery.ifEmpty { selectedCategory }}\"",
                                color = Color.Gray, fontSize = 15.sp,
                                textAlign = TextAlign.Center, lineHeight = 22.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = {
                                searchQuery = ""
                                selectedCategory = ""
                                activeFilter = FilterLevel.NONE
                            }) {
                                Text("Clear filter", color = Lime)
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            ProductFeedCard(product)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Lime else CardBg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null,
                tint = if (selected) Color.Black else Color.Gray,
                modifier = Modifier.size(14.dp))
            Text(label,
                color = if (selected) Color.Black else Color.Gray,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun CategoryPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Lime.copy(alpha = 0.2f) else Color(0xFF222222))
            .border(1.dp,
                if (selected) Lime else Color(0xFF333333),
                RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(label,
            color = if (selected) Lime else Color.Gray,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFeedCard(product: Product) {
    val context = LocalContext.current
    var showContactModal by remember { mutableStateOf(false) }
    var sellerPhone by remember { mutableStateOf("") }

    LaunchedEffect(product.sellerUid) {
        if (product.sellerUid.isNotEmpty()) {
            FirebaseDatabase.getInstance()
                .getReference("Users/${product.sellerUid}/phone")
                .get()
                .addOnSuccessListener { snap ->
                    sellerPhone = snap.getValue(String::class.java) ?: ""
                }
        }
    }

    // ── Contact modal ─────────────────────────────────────────
    if (showContactModal) {
        ModalBottomSheet(
            onDismissRequest = { showContactModal = false },
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Lime),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            product.sellerUsername.take(1).uppercase(),
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(product.sellerUsername, color = Color.White,
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(sellerPhone.ifEmpty { "No phone available" },
                            color = Color.Gray, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(4.dp))
                HorizontalDivider(color = Color(0xFF2A2A2A))
                Spacer(Modifier.height(20.dp))

                Text("Contact Seller", color = Color.White,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (sellerPhone.isNotEmpty()) {
                            context.startActivity(
                                Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$sellerPhone")
                                }
                            )
                        }
                        showContactModal = false
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2A2A), contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Call, null, tint = Lime, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Call ${sellerPhone.ifEmpty { "Unavailable" }}",
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (sellerPhone.isNotEmpty()) {
                            val cleaned = sellerPhone
                                .replace(" ", "").replace("-", "").trimStart('+')
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(
                                        "https://wa.me/$cleaned?text=" +
                                                Uri.encode("Hi ${product.sellerUsername}, I'm interested in your product: ${product.name} (KES ${product.price})")
                                    )
                                }
                            )
                        }
                        showContactModal = false
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppGreen, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Message, null, tint = Color.White,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("WhatsApp", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { showContactModal = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }

    // ── Card ──────────────────────────────────────────────────
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {

            // ── Image ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(Color(0xFF242424))
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("🌿", fontSize = 52.sp)
                    }
                }

                // Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )

                // Category chip
                if (product.category.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Lime, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(product.category, color = Color.Black,
                            fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                // Price badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("KES ${product.price}", color = Lime,
                        fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // ── Content ───────────────────────────────────────
            Column(modifier = Modifier.padding(16.dp)) {

                // Name
                Text(product.name, color = Color.White, fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)

                // Description
                if (product.description.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(product.description, color = Color(0xFF888888),
                        fontSize = 13.sp, maxLines = 2,
                        overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
                }

                Spacer(Modifier.height(10.dp))

                // ✅ Location + Quantity row
                val hasLocation = product.location.isNotEmpty()
                val hasQuantity = product.quantity.isNotEmpty()

                if (hasLocation || hasQuantity) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasLocation) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null,
                                    tint = Lime.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(product.location,
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 160.dp))
                            }
                        }
                        if (hasQuantity) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Scale, null,
                                    tint = Lime.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    "${product.quantity} ${product.unit}".trim(),
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                HorizontalDivider(color = Color(0xFF2A2A2A))
                Spacer(Modifier.height(14.dp))

                // ── Seller + Contact ──────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(listOf(Lime, Color(0xFF7AAF00)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                product.sellerUsername.take(1).uppercase(),
                                color = Color.Black, fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(product.sellerUsername, color = Color.White,
                                fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Verified Farmer", color = Lime, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = { showContactModal = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Lime, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Contact", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFF2A2A2A))
                Spacer(Modifier.height(12.dp))

                // ── Dates ─────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null,
                            tint = Color(0xFF666666), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Posted ${product.postedDate}",
                            color = Color(0xFF666666), fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null,
                            tint = if (isExpiringSoon(product.expiryDate))
                                Color.Red else Color(0xFF666666),
                            modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Expires ${product.expiryDate}",
                            color = if (isExpiringSoon(product.expiryDate))
                                Color.Red else Color(0xFF666666),
                            fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

fun isExpiringSoon(expiryDate: String): Boolean {
    return try {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val expiry = sdf.parse(expiryDate) ?: return false
        val diff = expiry.time - System.currentTimeMillis()
        diff in 0..(3 * 24 * 60 * 60 * 1000L)
    } catch (e: Exception) { false }
}

@Composable
fun FeedBottomNav(navController: NavController, currentRoute: String) {
    val user = FirebaseAuth.getInstance().currentUser
    NavigationBar(containerColor = Color(0xFF111111)) {
        NavigationBarItem(
            selected = currentRoute == ROUTE_FEED,
            onClick = { if (currentRoute != ROUTE_FEED) navController.navigate(ROUTE_FEED) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Feed") },
            label = { Text("Feed") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Lime, selectedTextColor = Lime,
                unselectedIconColor = Color.Gray, indicatorColor = Color(0xFF1A1A1A)
            )
        )
        NavigationBarItem(
            selected = currentRoute == ROUTE_ADD_PRODUCT,
            onClick = {
                if (user != null) navController.navigate(ROUTE_ADD_PRODUCT)
                else navController.navigate(ROUTE_LOGIN)
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Lime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Sell",
                        tint = Color.Black, modifier = Modifier.size(22.dp))
                }
            },
            label = { Text("Sell") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Lime, selectedTextColor = Lime,
                unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == ROUTE_PROFILE,
            onClick = {
                if (user != null) {
                    if (currentRoute != ROUTE_PROFILE) navController.navigate(ROUTE_PROFILE)
                } else navController.navigate(ROUTE_LOGIN)
            },
            icon = {
                Icon(
                    if (user != null) Icons.Default.Person else Icons.Default.Lock,
                    contentDescription = "Profile"
                )
            },
            label = { Text(if (user != null) "Profile" else "Login") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Lime, selectedTextColor = Lime,
                unselectedIconColor = Color.Gray, indicatorColor = Color(0xFF1A1A1A)
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FeedScreenPreview() {
    FeedScreen(navController = rememberNavController())
}