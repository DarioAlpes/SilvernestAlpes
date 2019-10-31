package co.smartobjects.ui.javafx.iconosyfonts;

import de.jensd.fx.glyphs.GlyphIcons;

public enum PrompterIconosGenerales implements GlyphIcons
{
	PERSONA("\ue900"), PRODUCTO_PAGADO("\ue901"), NUMERO_RESERVA("\ue902");

	private final String unicode;

	public static final String NOMBRE_FAMILIA_FONT = "prompter-iconos-generales";

	PrompterIconosGenerales(String unicode)
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
