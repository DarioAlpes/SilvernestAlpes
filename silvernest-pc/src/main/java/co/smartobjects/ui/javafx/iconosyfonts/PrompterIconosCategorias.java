package co.smartobjects.ui.javafx.iconosyfonts;

import co.smartobjects.ui.modelos.menufiltrado.ProveedorIconosCategoriasFiltrado;
import de.jensd.fx.glyphs.GlyphIcons;

public enum PrompterIconosCategorias implements GlyphIcons, ProveedorIconosCategoriasFiltrado.Icono
{
	ACCESO("\ue900"), DINERO("\ue901"), ENTRADA("\ue902"), GENERICO("\ue903"), PAQUETE("\ue904");

	private final String unicode;

	public static final String NOMBRE_FAMILIA_FONT = "prompter-iconos-categorias";

	PrompterIconosCategorias(String unicode)
	{
		this.unicode = unicode;
	}

	@Override
	public String unicode()
	{
		return unicode;
	}

	@Override
	public String fontFamily()
	{
		return NOMBRE_FAMILIA_FONT;
	}
}
