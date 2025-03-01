import androidx.compose.material.MaterialTheme

import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.smartnet.analyzer.common.theme.DarkColor2
import com.smartnet.analyzer.common.theme.LightColor2
import com.smartnet.analyzer.common.theme.Purple700
import com.smartnet.analyzer.common.theme.Teal200
import com.smartnet.analyzer.common.theme.Typography1

private val ColorPalette = darkColors(
    primary = Color.White,
    primaryVariant = Purple700,
    secondary = Teal200,
    background = DarkColor2,
    surface = DarkColor2,
    onSurface = LightColor2,
    onBackground = LightColor2
)

@Composable
fun ComposeSpeedTestTheme(
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colors = ColorPalette,
        typography = Typography1,
        shapes = Shapes,
        content = content
    )
}