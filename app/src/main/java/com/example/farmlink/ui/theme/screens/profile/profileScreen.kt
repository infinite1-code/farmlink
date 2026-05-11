package com.example.farmlink.ui.theme.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.farmlink.ui.theme.screens.FeedBottomNav
import com.example.farmlink.ui.theme.screens.isExpiringSoon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private val Lime = Color(0xFFB6FF00)
private val DarkBg = Color(0xFF0A0A0A)
private val CardBg = Color(0xFF1A1A1A)
private val DividerColor = Color(0xFF2A2A2A)

@Composable
fun ProfileScreen(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid ?: ""
    val email = user?.email ?: "No email"

    var username by remember { mutableStateOf("Farmer") }
    var phone by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val myProducts by productViewModel.myProducts.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            FirebaseDatabase.getInstance()
                .getReference("Users/$uid")
                .get()
                .addOnSuccessListener { snap ->
                    username = snap.child("username").getValue(String::class.java) ?: "Farmer"
                    phone = snap.child("phone").getValue(String::class.java) ?: ""
                }
            productViewModel.fetchMyProducts(uid)
        }
    }

    // ── Logout dialog ────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = CardBg,
            title = { Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out?", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Log Out", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // ── Delete confirmation dialog ───────────────────────────
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            containerColor = CardBg,
            title = { Text("Delete Listing", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete \"${product.name}\"? This cannot be undone.",
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    productViewModel.deleteProduct(uid, product.id, context)
                    productToDelete = null
                }) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = { FeedBottomNav(navController, currentRoute = ROUTE_PROFILE) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top bar ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "My Profile",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showLogoutDialog = true }) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Red)
                }
            }

            // ── Avatar + info card ───────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(CardBg, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Lime, Color(0xFF7AAF00))))
                            .border(3.dp, Color(0xFF2A2A2A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = username.take(1).uppercase(),
                            color = Color.Black,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(username, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null,
                            tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(email, color = Color.Gray, fontSize = 13.sp)
                    }

                    if (phone.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null,
                                tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(phone, color = Color.Gray, fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = DividerColor)
                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStat(myProducts.size.toString(), "Listings")
                        VerticalDivider(modifier = Modifier.height(40.dp), color = DividerColor)
                        ProfileStat("Active", "Status")
                        VerticalDivider(modifier = Modifier.height(40.dp), color = DividerColor)
                        ProfileStat("Farmer", "Role")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Listings header ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Listings", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${myProducts.size} products", color = Color.Gray, fontSize = 13.sp)
            }

            Spacer(Modifier.height(12.dp))

            // ── Product grid ─────────────────────────────────
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Lime)
                }
            } else {
                val gridItems: List<Product?> = myProducts + listOf(null)
                // ✅ Each tile is 160dp tall — fully visible, not cut off
                val rows = ((gridItems.size - 1) / 3) + 1

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((rows * 168).dp) // ✅ 168dp per row = fully visible tiles
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
                ) {
                    items(gridItems) { product ->
                        if (product == null) {
                            // ── "+" Add tile ─────────────────
                            Box(
                                modifier = Modifier
                                    .height(152.dp) // ✅ explicit height
                                    .fillMaxWidth()
                                    .background(CardBg, RoundedCornerShape(12.dp))
                                    .border(1.dp, Lime.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { navController.navigate(ROUTE_ADD_PRODUCT) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Lime.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add Product",
                                            tint = Lime,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "New Listing",
                                        color = Lime,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            // ── Product tile ──────────────────
                            Box(
                                modifier = Modifier
                                    .height(152.dp) // ✅ explicit height — no cut-off
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardBg)
                            ) {
                                // Product image
                                if (product.imageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = product.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🌿", fontSize = 32.sp)
                                    }
                                }

                                // ── Gradient overlay (bottom) ─
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.9f)
                                                )
                                            )
                                        )
                                )

                                // ── Name + price (bottom) ─────
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(start = 7.dp, bottom = 7.dp, end = 7.dp)
                                ) {
                                    Text(
                                        product.name,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "KES ${product.price}",
                                        color = Lime,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // ── ✅ Edit icon — top-left ───
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(5.dp)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .clickable {
                                            navController.navigate(
                                                updateProductRoute(product.id)
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Lime,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                // ── ✅ Delete icon — top-right ─
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(5.dp)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .clickable { productToDelete = product },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                // ── ✅ Availability toggle — bottom center ────
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (product.isAvailable) "ON" else "OFF",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )

                                    Switch(
                                        checked = product.isAvailable,
                                        onCheckedChange = {
                                            productViewModel.toggleAvailability(
                                                uid, product.id, product.isAvailable, context
                                            )
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF2E7D32),   // Green track
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color(0xFFC62828) // Red track
                                        )
                                    )
                                }

                                // ── Expiry badge ──────────────
                                if (product.expiryDate.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = 5.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.65f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            product.expiryDate,
                                            color = if (isExpiringSoon(product.expiryDate))
                                                Color.Red else Color.White,
                                            fontSize = 8.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Spacer(Modifier.height(2.dp))
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}