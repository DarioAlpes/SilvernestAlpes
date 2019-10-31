package co.smartobjects.ui.javafx.iconosyfonts;

import de.jensd.fx.glyphs.GlyphIcons;

public enum PrompterIconosMenuPrincipal implements GlyphIcons
{
	REGISTRO("\ue902"), CONSUMOS("\ue901");

	private final String unicode;

	public static final String NOMBRE_FAMILIA_FONT = "prompter-iconos-menu-principal";

	PrompterIconosMenuPrincipal(String unicode)
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
