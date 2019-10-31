package co.smartobjects.ui.javafx.iconosyfonts

import de.jensd.fx.glyphs.GlyphIcon
import javafx.scene.text.Font
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

class PrompterIconosMenuPrincipalView @JvmOverloads constructor(
        icon: PrompterIconosMenuPrincipal = PrompterIconosMenuPrincipal.REGISTRO,
        iconSize: String = "1em")
    : GlyphIcon<PrompterIconosMenuPrincipal>(PrompterIconosMenuPrincipal::class.java)
{
    companion object
    {
        private const val TTF_PATH = "/fonts/${PrompterIconosMenuPrincipal.NOMBRE_FAMILIA_FONT}.ttf"

        init
        {
            try
            {
                Font.loadFont(PrompterIconosMenuPrincipalView::class.java.getResource(TTF_PATH).openStream(), 10.0)
            }
            catch (ex: IOException)
            {
                Logger.getLogger(PrompterIconosMenuPrincipalView::class.java.name).log(Level.SEVERE, null, ex)
            }
        }
    }

    init
    {
        setIcon(icon)
        style = String.format("-fx-font-family: %s; -fx-font-size: %s;", icon.fontFamily(), iconSize)
    }

    override fun getDefaultGlyph() = PrompterIconosMenuPrincipal.REGISTRO
}