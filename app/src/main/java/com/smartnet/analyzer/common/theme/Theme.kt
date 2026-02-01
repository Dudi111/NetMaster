import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.smartnet.analyzer.common.theme.DarkColor2
import com.smartnet.analyzer.common.theme.LightColor2
import com.smartnet.analyzer.common.theme.Purple700
import com.smartnet.analyzer.common.theme.Teal200
import com.smartnet.analyzer.common.theme.Typography1

private val ColorPalette = darkColorScheme(
    primary = Color.White,
    secondary = Teal200,

    background = DarkColor2,
    surface = DarkColor2,

    onPrimary = Color.Black,
    onSecondary = Color.Black,

    onBackground = LightColor2,
    onSurface = LightColor2,

    primaryContainer = Purple700,
    onPrimaryContainer = Color.White
)

@Composable
fun ComposeSpeedTestTheme(
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = ColorPalette,
        typography = Typography1,
        shapes = Shapes,
        content = content
    )
}