package edu.kit.cm.ivu.smartmeetings.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import edu.kit.cm.ivu.smartmeetings.R;

/**
 * 
 * @author Kirill Rakhman
 */
public class CreateDiscussionFragment extends FrontendFragment implements
		OnClickListener {

	EditText textViewName;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.create_discussion, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.button_create).setOnClickListener(this);

		textViewName = (EditText) view.findViewById(R.id.edit_discussion_name);
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.button_create:
			createDiscussion(textViewName.getText().toString());
			break;
		}
	}

	private void createDiscussion(String name) {
		if (name == null || name.trim().isEmpty()) {
			Toast.makeText(getActivity(), R.string.name_not_valid,
					Toast.LENGTH_SHORT).show();
			return;
		}

		name = name.trim();

		final String id = name + System.currentTimeMillis();

		final String discussionUri = GetTopicInfoWorker.getDiscussionUri(id,
				name);

		doInBackground(new GetTopicInfoWorker(getFrontend()), discussionUri);
	}
}
