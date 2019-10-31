package co.smartobjects.silvernestandroid.utilidades.persistencia.ormlitemodificado;

import android.database.Cursor;
import io.requery.android.database.sqlite.SQLiteDatabase;

public interface MiApiCompatibility
{

	/**
	 * Perform a raw query on a database with an optional cancellation-hook.
	 */
	public Cursor rawQuery(SQLiteDatabase db, String sql, String[] selectionArgs, CancellationHook cancellationHook);

	/**
	 * Return a cancellation hook object that will be passed to the
	 * {@link #rawQuery(SQLiteDatabase, String, String[], CancellationHook)}. If not supported then this will return
	 * null.
	 */
	public CancellationHook createCancellationHook();

	/**
	 * Cancellation hook class returned by {@link MiApiCompatibility#createCancellationHook()}.
	 */
	public interface CancellationHook
	{
		/**
		 * Cancel the associated query.
		 */
		public void cancel();
	}
}
