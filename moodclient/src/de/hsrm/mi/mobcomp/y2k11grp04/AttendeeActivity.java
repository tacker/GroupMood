package de.hsrm.mi.mobcomp.y2k11grp04;

import uk.co.jasonfry.android.tools.ui.PageControl;
import uk.co.jasonfry.android.tools.ui.SwipeView;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.devsmart.android.ui.HorizontalListView;

import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.QuestionOption;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;
import de.hsrm.mi.mobcomp.y2k11grp04.service.MoodServerService;
import de.hsrm.mi.mobcomp.y2k11grp04.view.TopicGalleryAdapter;

public class AttendeeActivity extends ServiceActivity {

	private final int SCREEN_ORIENTATION_PORTRAIT = 1;
	protected ProgressBar loadingProgress;
	protected Meeting meeting;
	protected boolean meetingComplete = false;
	private View topicGallery;
	private Topic currentTopic;
	private Question currentQuestion;
	private TopicGalleryAdapter topicGalleryAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayout());

		loadingProgress = (ProgressBar) findViewById(R.id.groupMood_progressBar);

		Bundle b = getIntent().getExtras();
		b.setClassLoader(getClassLoader());
		meeting = b.getParcelable(MoodServerService.KEY_MEETING_MODEL);

		updateView();
	}

	/**
	 * Die Anzeige der Aktivity aktualisieren.
	 */
	protected void updateView() {
		TextView meetingName = (TextView) findViewById(R.id.groupMood_meetingName);
		meetingName.setText(meeting.getName());
		if (meetingComplete) {
			loadingProgress.setVisibility(View.GONE);
		} else {
			loadingProgress.setVisibility(View.VISIBLE);
		}

		topicGallery = findViewById(R.id.groupMood_gallery);
		topicGalleryAdapter = new TopicGalleryAdapter(meeting.getTopics());
		GalleryItemSelectListener topicGallerySelectListener = new GalleryItemSelectListener();

		if (topicGallery instanceof HorizontalListView) {
			HorizontalListView lv = ((HorizontalListView) topicGallery);
			lv.setAdapter(topicGalleryAdapter);
			lv.setOnItemSelectedListener(topicGallerySelectListener);
		} else {
			ListView lv = ((ListView) topicGallery);
			lv.setAdapter(topicGalleryAdapter);
			lv.setOnItemSelectedListener(topicGallerySelectListener);
		}

		if (getResources().getConfiguration().orientation == SCREEN_ORIENTATION_PORTRAIT)
			portrait();
		else
			landscape();
	}

	protected int getLayout() {
		return R.layout.attendee;
	}

	private void landscape() {
		PageControl mPageControl = (PageControl) findViewById(R.id.groupMood_page_control);
		SwipeView mSwipeView = (SwipeView) findViewById(R.id.groupMood_swipe_view);
		mSwipeView.setPageControl(mPageControl);

		Topic currentTopic = getCurrentTopic();
		if (currentTopic != null) {
			for (Question q : currentTopic.getQuestions()) {
				FrameLayout questionView = new FrameLayout(this);
				mSwipeView.addView(questionView);
				TextView questionText = (TextView) createSwipeTextView();
				questionText.setText(q.getName());
				questionView.addView(questionText);
			}
		}
	}

	private void portrait() {
		updateDetailView();
	}

	private View createSwipeTextView() {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.question, null);
		TextView tv = (TextView) view.findViewById(R.id.groupMood_question);
		tv.setTextSize(16);
		tv.setSingleLine(false);
		return view;
	}

	private Topic getCurrentTopic() {
		if (currentTopic == null) {
			if (meeting.getTopics().size() > 0) {
				currentTopic = meeting.getTopics().get(0);
			}
		}
		return currentTopic;
	}

	/**
	 * Aktualisiert die Detailansicht
	 */
	protected void updateDetailView() {
		WebView webView = (WebView) findViewById(R.id.groupMood_detailWebView);
		String summary = "";
		summary += "<h1>" + meeting.getName() + "</h1>";
		if (meeting.getTopics().size() > 0) {
			summary += "<h2>Topics</h2>";
			summary += "<ul>";
			for (Topic t : meeting.getTopics()) {
				summary += "<li>" + t.getName();
				summary += "<br>Questions:";
				summary += "<ul>";
				for (Question q : t.getQuestions()) {
					summary += "<li>" + q.getName();
					summary += "<br>Type: " + q.getType();
					summary += "<br>Mode: " + q.getMode();
					summary += "<br>Average: " + q.getAvg();
					summary += "<br>Options:";
					summary += "<ul>";
					for (QuestionOption o : q.getOptions()) {
						summary += "<li>" + o.getKey() + " = " + o.getValue()
								+ "</li>";
					}
					summary += "</ul>";
					summary += "</li>";
				}
				summary += "</ul>";
				summary += "</li>";
			}
			summary += "</ul>";
		}

		webView.loadData("<html><body>" + summary + "</body></html>",
				"text/html", null);
	}

	@Override
	protected ServiceMessageRunnable getServiceMessageRunnable(Message message) {
		switch (message.what) {
		case MoodServerService.MSG_MEETING_COMPLETE_PROGRESS:
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					loadingProgress.setMax(serviceMessage.arg2);
					loadingProgress.setProgress(serviceMessage.arg1);
				}
			};
		case MoodServerService.MSG_MEETING_COMPLETE_RESULT:
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					Bundle b = serviceMessage.getData();
					b.setClassLoader(getClassLoader());
					meeting = b
							.getParcelable(MoodServerService.KEY_MEETING_MODEL);
					meetingComplete = true;
					updateView();
				}
			};
		default:
			return super.getServiceMessageRunnable(message);
		}
	}

	@Override
	protected void onConnect() {
		super.onConnect();
		// Meeting vollständig laden
		if (!meetingComplete)
			loadMeetingComplete(meeting);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (meeting != null) {
			outState.putParcelable(MoodServerService.KEY_MEETING_MODEL, meeting);
		}
		outState.putBoolean("meetingComplete", meetingComplete);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(MoodServerService.KEY_MEETING_MODEL)) {
			meeting = savedInstanceState
					.getParcelable(MoodServerService.KEY_MEETING_MODEL);
		}
		meetingComplete = savedInstanceState.getBoolean("meetingComplete");
		updateView();
	}

	private class GalleryItemSelectListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View parent,
				int position, long arg3) {
			Log.v(getClass().getCanonicalName(),
					topicGalleryAdapter.getItem(position).getName()
							+ " selected");
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
}