package de.tudarmstadt.informatik.hostage.ui.fragment;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import de.tudarmstadt.informatik.hostage.R;
import de.tudarmstadt.informatik.hostage.logging.Record;
import de.tudarmstadt.informatik.hostage.ui.model.LogFilter;
import de.tudarmstadt.informatik.hostage.ui.activity.MainActivity;
import de.tudarmstadt.informatik.hostage.logging.MessageRecord;
import de.tudarmstadt.informatik.hostage.persistence.HostageDBOpenHelper;

/**
 * Displays detailed informations about an record.
 *
 * @author Fabio Arnold
 * @author Alexander Brakowski
 * @author Julien Clauter
 */
public class RecordDetailFragment extends UpNavigatibleFragment {

	/**
	 * Hold the record of which the detail informations should be shown
	 */
	private Record mRecord;

	/**
	 * The database helper to retrieve data from the database
	 */
	private HostageDBOpenHelper mDBOpenHelper;

	/**
	 * The layout inflater
	 */
	private LayoutInflater mInflater;

	/*
	 * References to the views in the layout
	 */
	private View mRootView;
	private LinearLayout mRecordOverviewConversation;
	private TextView mRecordDetailsTextAttackType;
	private TextView mRecordDetailsTextSsid;
	private TextView mRecordDetailsTextBssid;
	private TextView mRecordDetailsTextRemoteip;
	private TextView mRecordDetailsTextProtocol;
	private ImageButton mRecordDeleteButton;

	/**
	 * Sets the record of which the details should be displayed
	 * @param rec the record to be used
	 */
	public void setRecord(Record rec) {
		this.mRecord = rec;
	}

	/**
	 * Retriebes the record which is used for the display of the detail informations
	 * @return the record
	 */
	public Record getRecord() {
		return this.mRecord;
	}

	/**
	 * Retrieves the id of the layout
	 * @return the id of the layout
	 */
	public int getLayoutId() {
		return R.layout.fragment_record_detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		mInflater = inflater;
		getActivity().setTitle(mRecord.getSsid());

		this.mDBOpenHelper = new HostageDBOpenHelper(this.getActivity().getBaseContext());

		this.mRootView = inflater.inflate(this.getLayoutId(), container, false);
		this.assignViews(mRootView);
		this.configurateRootView(mRootView);

		return mRootView;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart() {
		super.onStart();

	}

	/**
	 * Retrieves all the views from the given view
	 *
	 * @param view the layout view
	 */
	private void assignViews(View view) {
		mRecordOverviewConversation = (LinearLayout) view.findViewById(R.id.record_overview_conversation);
		mRecordDetailsTextAttackType = (TextView) view.findViewById(R.id.record_details_text_attack_type);
		mRecordDetailsTextSsid = (TextView) view.findViewById(R.id.record_details_text_ssid);
		mRecordDetailsTextBssid = (TextView) view.findViewById(R.id.record_details_text_bssid);
		mRecordDetailsTextRemoteip = (TextView) view.findViewById(R.id.record_details_text_remoteip);
		mRecordDetailsTextProtocol = (TextView) view.findViewById(R.id.record_details_text_protocol);
		mRecordDeleteButton = (ImageButton) view.findViewById(R.id.DeleteButton);
	}


	/**
	 * Configures the given view and fills it with the detail information
	 *
	 * @param rootView the view to use to display the informations
	 */
	private void configurateRootView(View rootView) {

		mRecordDetailsTextAttackType.setText(mRecord.getWasInternalAttack() ? R.string.RecordInternalAttack : R.string.RecordExternalAttack);
		mRecordDetailsTextBssid.setText(mRecord.getBssid());
		mRecordDetailsTextSsid.setText(mRecord.getSsid());
		if (mRecord.getRemoteIP() != null)
			mRecordDetailsTextRemoteip.setText(mRecord.getRemoteIP() + ":" + mRecord.getRemotePort());

			mRecordDetailsTextProtocol.setText(mRecord.getProtocol());

		ArrayList<Record> conversation = this.mDBOpenHelper.getConversationForAttackID(mRecord.getAttack_id());

		// display the conversation of the attack
		for (Record r : conversation) {
			View row;

			String from = r.getLocalIP() == null ? "-" : r.getLocalIP() + ":" + r.getLocalPort();
			String to = r.getRemoteIP() == null ? "-" : r.getRemoteIP() + ":" + r.getRemotePort();

			if (r.getType() == MessageRecord.TYPE.SEND) {
				row = mInflater.inflate(R.layout.fragment_record_conversation_sent, null);
			} else {
				row = mInflater.inflate(R.layout.fragment_record_conversation_received, null);

				String tmp = from;
				from = to;
				to = tmp;
			}

			TextView conversationInfo = (TextView) row.findViewById(R.id.record_conversation_info);
			TextView conversationContent = (TextView) row.findViewById(R.id.record_conversation_content);
			conversationContent.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(final View v, final MotionEvent motionEvent) {
					if (v.getId() == R.id.record_conversation_content) {
						if (v.canScrollVertically(1) || v.canScrollVertically(-1)) { // if the view is scrollable
							v.getParent().requestDisallowInterceptTouchEvent(true);
							switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
								case MotionEvent.ACTION_UP:
									v.getParent().requestDisallowInterceptTouchEvent(false);
									break;
							}
						}
					}
					return false;
				}
			});

			Date date = new Date(r.getTimestamp());
			conversationInfo.setText(String.format(getString(R.string.record_details_info), from, to, getDateAsString(date), getTimeAsString(date)));
			if (r.getPacket() != null)
				conversationContent.setText(r.getPacket());

			mRecordOverviewConversation.addView(row);
		}

		mRecordDeleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Activity activity = getActivity();
				if (activity == null) {
					return;
				}
				new AlertDialog.Builder(getActivity())
						.setTitle(android.R.string.dialog_alert_title)
						.setMessage(R.string.record_details_confirm_delete)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										mDBOpenHelper.deleteByAttackID(mRecord.getAttack_id());

										MainActivity.getInstance().navigateBack();
									}
								}
						).setNegativeButton(R.string.no, null)
						.setIcon(android.R.drawable.ic_dialog_alert).show();
			}
		});
	}

	/*****************************
	 * 
	 * Date Transform
	 * 
	 * ***************************/

	/**
	 * Converts the given data to an localized string
	 *
	 * @param date the date to convert
	 * @return the converted date as an string
	 */
	private String getDateAsString(Date date) {
		return DateFormat.getDateFormat(getActivity()).format(date);
	}

	/**
	 * Converts the given date to an localized time
	 *
	 * @param date the date to convert
	 * @return the converted time as an string
	 */
	private String getTimeAsString(Date date) {
		return DateFormat.getTimeFormat(getActivity()).format(date);
	}
}
