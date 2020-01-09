package de.tudarmstadt.informatik.hostage.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import de.tudarmstadt.informatik.hostage.persistence.HostageDBContract.AttackEntry;
import de.tudarmstadt.informatik.hostage.persistence.HostageDBContract.NetworkEntry;
import de.tudarmstadt.informatik.hostage.persistence.HostageDBContract.PacketEntry;
import de.tudarmstadt.informatik.hostage.persistence.HostageDBOpenHelper;

public class HostageContentProvider extends ContentProvider {

	public static final String AUTHORITY = "de.tudarmstadt.informatik.hostage.provider";

	public static final Uri CONTENT_URI_NETWORK = Uri.parse("content://" + AUTHORITY + "/network");
	public static final Uri CONTENT_URI_ATTACK = Uri.parse("content://" + AUTHORITY + "/attack");
	public static final Uri CONTENT_URI_PACKET = Uri.parse("content://" + AUTHORITY + "/packet");

	public static final Uri CONTENT_URI_RECORD_OVERVIEW = Uri.parse("content://" + AUTHORITY + "/record-overview");

	private static final int NETWORK_ALL = 11;
	private static final int NETWORK_ONE = 12;
	private static final int ATTACK_ALL = 21;
	private static final int ATTACK_ONE = 22;
	private static final int PACKET_ALL = 31;
	private static final int PACKET_ONE = 32;

	private static final int NETWORK_OVERVIEW_ALL = 101;
	private static final int NETWORK_OVERVIEW_ONE = 102;

	private static final UriMatcher uriMatcher;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "network", NETWORK_ALL);
		uriMatcher.addURI(AUTHORITY, "network/#", NETWORK_ONE);
		uriMatcher.addURI(AUTHORITY, "attack", ATTACK_ALL);
		uriMatcher.addURI(AUTHORITY, "attack/#", ATTACK_ONE);
		uriMatcher.addURI(AUTHORITY, "packet", PACKET_ALL);
		uriMatcher.addURI(AUTHORITY, "packet/#", PACKET_ONE);
		uriMatcher.addURI(AUTHORITY, "record-overview", NETWORK_OVERVIEW_ALL);
		uriMatcher.addURI(AUTHORITY, "record-overview/#", NETWORK_OVERVIEW_ONE);
	}

	private HostageDBOpenHelper mDBOpenHelper;

	@Override
	public boolean onCreate() {
		mDBOpenHelper = new HostageDBOpenHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		int uriMatch = uriMatcher.match(uri);

		if (isNetworkUriMatch(uriMatch)) {
			queryBuilder.setTables(NetworkEntry.TABLE_NAME);
		} else if (isAttackUriMatch(uriMatch)) {
			queryBuilder.setTables(AttackEntry.TABLE_NAME);
		} else if (isPacketUriMatch(uriMatch)) {
			queryBuilder.setTables(PacketEntry.TABLE_NAME);
		}

		if (uriMatch == NETWORK_ONE) {
			String rowID = uri.getPathSegments().get(1);
			queryBuilder.appendWhere(NetworkEntry.KEY_ID + "=" + rowID);
		} else if (uriMatch == ATTACK_ONE) {
			String rowID = uri.getPathSegments().get(1);
			queryBuilder.appendWhere(AttackEntry.KEY_ID + "=" + rowID);
		} else if (uriMatch == PACKET_ONE) {
			String rowID = uri.getPathSegments().get(1);
			queryBuilder.appendWhere(PacketEntry.KEY_ID + "=" + rowID);
		}

		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		return cursor;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();

		int uriMatch = uriMatcher.match(uri);

		if (uriMatch == NETWORK_ONE) {
			String rowID = uri.getPathSegments().get(1);
			selection = NetworkEntry.KEY_ID + "=" + rowID + (!TextUtils.isEmpty(selection) ? "AND (" + selection + ")" : "");
		} else if (uriMatch == ATTACK_ONE) {
			String rowID = uri.getPathSegments().get(1);
			selection = AttackEntry.KEY_ID + "=" + rowID + (!TextUtils.isEmpty(selection) ? "AND (" + selection + ")" : "");
		} else if (uriMatch == PACKET_ONE) {
			String rowID = uri.getPathSegments().get(1);
			selection = PacketEntry.KEY_ID + "=" + rowID + (!TextUtils.isEmpty(selection) ? "AND (" + selection + ")" : "");
		}

		if (selection == null) {
			selection = "1";
		}

		int deleteCount = 0;
		if (isNetworkUriMatch(uriMatch)) {
			deleteCount = db.delete(NetworkEntry.TABLE_NAME, selection, selectionArgs);
		} else if (isAttackUriMatch(uriMatch)) {
			deleteCount = db.delete(AttackEntry.TABLE_NAME, selection, selectionArgs);
		} else if (isPacketUriMatch(uriMatch)) {
			deleteCount = db.delete(PacketEntry.TABLE_NAME, selection, selectionArgs);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return deleteCount;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();

		int uriMatch = uriMatcher.match(uri);

		long id = -1;
		Uri insertedId = null;
		if (isNetworkUriMatch(uriMatch)) {
			id = db.insert(NetworkEntry.TABLE_NAME, null, values);
			if (id > -1) {
				insertedId = ContentUris.withAppendedId(CONTENT_URI_NETWORK, id);
			}
		} else if (isAttackUriMatch(uriMatch)) {
			id = db.insert(AttackEntry.TABLE_NAME, null, values);
			if (id > -1) {
				insertedId = ContentUris.withAppendedId(CONTENT_URI_NETWORK, id);
			}
		} else if (isPacketUriMatch(uriMatch)) {
			id = db.insert(PacketEntry.TABLE_NAME, null, values);
			if (id > -1) {
				insertedId = ContentUris.withAppendedId(CONTENT_URI_NETWORK, id);
			}
		}

		if (id > -1) {
			getContext().getContentResolver().notifyChange(insertedId, null);
			return insertedId;
		}

		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();

		int uriMatch = uriMatcher.match(uri);

		if (uriMatch == NETWORK_ONE) {
			String rowID = uri.getPathSegments().get(1);
			selection = NetworkEntry.KEY_ID + "=" + rowID + (!TextUtils.isEmpty(selection) ? "AND (" + selection + ")" : "");
		} else if (uriMatch == ATTACK_ONE) {
			String rowID = uri.getPathSegments().get(1);
			selection = AttackEntry.KEY_ID + "=" + rowID + (!TextUtils.isEmpty(selection) ? "AND (" + selection + ")" : "");
		} else if (uriMatch == PACKET_ONE) {
			String rowID = uri.getPathSegments().get(1);
			selection = PacketEntry.KEY_ID + "=" + rowID + (!TextUtils.isEmpty(selection) ? "AND (" + selection + ")" : "");
		}

		int updateCount = 0;
		if (isNetworkUriMatch(uriMatch)) {
			updateCount = db.update(NetworkEntry.TABLE_NAME, values, selection, selectionArgs);
		} else if (isAttackUriMatch(uriMatch)) {
			updateCount = db.update(AttackEntry.TABLE_NAME, values, selection, selectionArgs);
		} else if (isPacketUriMatch(uriMatch)) {
			updateCount = db.update(PacketEntry.TABLE_NAME, values, selection, selectionArgs);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return updateCount;
	}

	@Override
	public String getType(Uri uri) {
		int uriMatch = uriMatcher.match(uri);

		if (isNetworkUriMatch(uriMatch)) {
			if (uriMatch == NETWORK_ONE) {
				return "vnd.android.cursor.item/vnd.de.tudarmstadt.informatik.hostage.provider.network";
			}
			return "vnd.android.cursor.dir/vnd.de.tudarmstadt.informatik.hostage.provider.network";
		} else if (isAttackUriMatch(uriMatch)) {
			if (uriMatch == ATTACK_ONE) {
				return "vnd.android.cursor.item/vnd.de.tudarmstadt.informatik.hostage.provider.attack";
			}
			return "vnd.android.cursor.dir/vnd.de.tudarmstadt.informatik.hostage.provider.attack";
		} else if (isPacketUriMatch(uriMatch)) {
			if (uriMatch == PACKET_ONE) {
				return "vnd.android.cursor.item/vnd.de.tudarmstadt.informatik.hostage.provider.packet";
			}
			return "vnd.android.cursor.dir/vnd.de.tudarmstadt.informatik.hostage.provider.packet";
		}

		return null;
	}

	private boolean isNetworkUriMatch(int uriMatch) {
		if (uriMatch == NETWORK_ALL || uriMatch == NETWORK_ONE) {
			return true;
		}
		return false;
	}

	private boolean isAttackUriMatch(int uriMatch) {
		if (uriMatch == ATTACK_ALL || uriMatch == ATTACK_ONE) {
			return true;
		}
		return false;
	}

	private boolean isPacketUriMatch(int uriMatch) {
		if (uriMatch == PACKET_ALL || uriMatch == PACKET_ONE) {
			return true;
		}
		return false;
	}

}
