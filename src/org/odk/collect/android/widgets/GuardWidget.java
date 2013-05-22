package org.odk.collect.android.widgets;

import android.text.Editable;
import android.text.TextWatcher;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TableLayout;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.services.TriggerManagerActivity;

public class GuardWidget extends QuestionWidget implements IBinaryWidget {

    protected EditText mAnswer;

    public GuardWidget(Context context, FormEntryPrompt p) {
        super(context, p);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);
        mAnswer = new EditText(context);
        mAnswer.setId(QuestionWidget.newUniqueId());
        mAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mAnswer.setLayoutParams(params);
        mAnswer.setBackgroundDrawable(null);
        mAnswer.setKeyListener(new TextKeyListener(Capitalize.NONE, false));
        mAnswer.setFocusable(false);
        mAnswer.setClickable(false);

        String s = p.getAnswerText();

        if (s!=null) {
            mAnswer.setText(s);
        }else {
            Intent i = new Intent(context, TriggerManagerActivity.class);
            try {
                i.putExtra("qid",mPrompt.getIndex().toString());
                Collect.getInstance().getFormController().setIndexWaitingForData(mPrompt.getIndex());
                fireActivity(i);
            } catch (ActivityNotFoundException e) {

            }
        }
        mAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                FormController formController = Collect.getInstance()
                        .getFormController();
                formController.stepToNextScreenEvent();
            }

        });
        addView(mAnswer);

    }

    protected void fireActivity(Intent i) throws ActivityNotFoundException {
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "launchIntent",
                i.getAction(), mPrompt.getIndex());
        ((Activity) getContext()).startActivityForResult(i,
                FormEntryActivity.EX_STRING_CAPTURE);
    }

    @Override
    public void setBinaryData(Object answer) {
        // TODO Auto-generated method stub
        mAnswer.setText((String) answer);
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void cancelWaitingForBinaryData() {
        // TODO Auto-generated method stub
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public boolean isWaitingForBinaryData() {
        // TODO Auto-generated method stub
        return mPrompt.getIndex().equals(Collect.getInstance().getFormController().getIndexWaitingForData());
    }

    @Override
    public IAnswerData getAnswer() {
        // TODO Auto-generated method stub
        String s = mAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }

    @Override
    public void clearAnswer() {
        // TODO Auto-generated method stub
        mAnswer.setText(null);
    }

    @Override
    public void setFocus(Context context) {
        // TODO Auto-generated method stub
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        // TODO Auto-generated method stub
        mAnswer.setOnLongClickListener(l);
    }
}