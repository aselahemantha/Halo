package com.example.halo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.halo.R
import com.example.halo.ui.theme.HaloTheme

@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFFDAD6), // Light Red/Pink from screenshot
                shape = RoundedCornerShape(50) // Pill shape
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Placeholder icon, assuming a baseline material icon or resource
            // You might need to add a drawable or use standard icon
            // Using a generic error icon or similar if exact one not available.
            // Assuming "ic_wifi_off" or similar might not exist, using standard icon.
            // Or creating a custom path if needed. For now, let's try standard icon.
            // Actually, Material 3 has Icons.Rounded.WifiOff but implementation
            // might not include full icon set.
            // Let's use a Text placeholder or standard icon if available.
             Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel), // Temporary, should be wifi off
                contentDescription = "No internet",
                tint = Color(0xFF410002) // Dark Red text color
            )
            Text(
                text = "No internet connection",
                color = Color(0xFF410002),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
        
        TextButton(onClick = onRetry) {
            Text(
                text = "RETRY",
                color = Color(0xFF410002),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Preview
@Composable
fun OfflineBannerPreview() {
    HaloTheme {
        OfflineBanner()
    }
}
