package co.smartobjects.ui.javafx.iconosyfonts

import de.jensd.fx.glyphs.GlyphIcon
import javafx.scene.text.Font
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

class PrompterIconosCategoriasView @JvmOverloads constructor(
        icon: PrompterIconosCategorias = PrompterIconosCategorias.GENERICO,
        iconSize: String = "1em")
    : GlyphIcon<PrompterIconosCategorias>(PrompterIconosCategorias::class.java)
{
    companion object
    {
        private const val TTF_PATH = "/fonts/${PrompterIconosCategorias.NOMBRE_FAMILIA_FONT}.ttf"

        init
        {
            try
            {
                Font.loadFont(PrompterIconosCategoriasView::class.java.getResource(TTF_PATH).openStream(), 10.0)
            }
            catch (ex: IOException)
            {
                Logger.getLogger(PrompterIconosCategoriasView::class.java.name).log(Level.SEVERE, null, ex)
            }
        }
    }

    init
    {
        setIcon(icon)
        style = String.format("-fx-font-family: %s; -fx-font-size: %s;", icon.fontFamily(), iconSize)
    }

    override fun getDefaultGlyph() = PrompterIconosCategorias.GENERICO
}
