package com.example.farmlink.ui.theme.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.farmlink.navigation.ROUTE_PROFILE
import com.example.farmlink.navigation.ROUTE_ADD_PRODUCT
import com.google.firebase.auth.FirebaseAuth

private val Lime = Color(0xFFB6FF00)
private val DarkBg = Color(0xFF0A0A0A)
private val CardBg = Color(0xFF1A1A1A)

@Composable
fun DashboardScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val displayName = user?.displayName ?: "Farmer"

    Scaffold(
        containerColor = DarkBg,
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hello,", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        displayName,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Lime)
                        .clickable { navController.navigate(ROUTE_PROFILE) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.take(1).uppercase(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Products", "0", Icons.Default.Eco, Modifier.weight(1f))
                StatCard("Orders", "0", Icons.Default.ShoppingCart, Modifier.weight(1f))
                StatCard("Revenue", "KES 0", Icons.Default.TrendingUp, Modifier.weight(1f))
            }

            Spacer(Modifier.height(28.dp))

            // Quick actions
            Text("Quick Actions", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    label = "Add Product",
                    icon = Icons.Default.Add,
                    modifier = Modifier.weight(1f)
                ) { navController.navigate(ROUTE_ADD_PRODUCT) }

                QuickActionCard(
                    label = "My Profile",
                    icon = Icons.Default.Person,
                    modifier = Modifier.weight(1f)
                ) { navController.navigate(ROUTE_PROFILE) }
            }

            Spacer(Modifier.height(28.dp))

            // Recent activity placeholder
            Text("Recent Activity", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No recent activity yet.\nStart by adding your first product!",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = Lime, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun QuickActionCard(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Lime.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Lime, modifier = Modifier.size(18.dp))
            }
            Text(label, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    NavigationBar(containerColor = Color(0xFF111111)) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Lime,
                selectedTextColor = Lime,
                unselectedIconColor = Color.Gray,
                indicatorColor = Color(0xFF1A1A1A)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(ROUTE_PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Lime,
                selectedTextColor = Lime,
                unselectedIconColor = Color.Gray,
                indicatorColor = Color(0xFF1A1A1A)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(ROUTE_ADD_PRODUCT) },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
            label = { Text("Add") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Lime,
                selectedTextColor = Lime,
                unselectedIconColor = Color.Gray,
                indicatorColor = Color(0xFF1A1A1A)
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen(navController = rememberNavController())
}