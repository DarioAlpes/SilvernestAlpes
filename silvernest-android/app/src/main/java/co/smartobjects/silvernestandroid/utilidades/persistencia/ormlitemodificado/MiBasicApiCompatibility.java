package co.smartobjects.silvernestandroid.utilidades.persistencia.ormlitemodificado;

import android.database.Cursor;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class MiBasicApiCompatibility implements MiApiCompatibility
{
	@Override
	public Cursor rawQuery(SQLiteDatabase db, String sql, String[] selectionArgs, CancellationHook cancellationHook)
	{
		// NOTE: cancellationHook will always be null
		return db.rawQuery(sql, selectionArgs);
	}

	@Override
	public CancellationHook createCancellationHook()
	{
		return null;
	}
}
