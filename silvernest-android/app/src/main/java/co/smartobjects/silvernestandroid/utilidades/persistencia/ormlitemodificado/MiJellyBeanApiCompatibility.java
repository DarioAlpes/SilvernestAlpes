package co.smartobjects.silvernestandroid.utilidades.persistencia.ormlitemodificado;

import android.database.Cursor;
import android.support.v4.os.CancellationSignal;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class MiJellyBeanApiCompatibility extends MiBasicApiCompatibility
{

	@Override
	public Cursor rawQuery(SQLiteDatabase db, String sql, String[] selectionArgs, CancellationHook cancellationHook)
	{
		if(cancellationHook == null)
		{
			return db.rawQuery(sql, selectionArgs);
		}
		else
		{
			return db.rawQuery(sql, selectionArgs, ((MiJellyBeanCancellationHook) cancellationHook).cancellationSignal);
		}
	}

	@Override
	public CancellationHook createCancellationHook()
	{
		return new MiJellyBeanCancellationHook();
	}

	/**
	 * Hook object that supports canceling a running query.
	 */
	protected static class MiJellyBeanCancellationHook implements CancellationHook
	{

		private final CancellationSignal cancellationSignal;

		public MiJellyBeanCancellationHook()
		{
			this.cancellationSignal = new CancellationSignal();
		}

		@Override
		public void cancel()
		{
			cancellationSignal.cancel();
		}
	}
}
