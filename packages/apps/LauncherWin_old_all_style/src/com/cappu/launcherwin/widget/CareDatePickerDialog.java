
package com.cappu.launcherwin.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Calendar;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.widget.CareDatePicker.OnDateChangedListener;


/**
 * A simple dialog containing an {@link android.widget.CareDatePicker}.
 * 
 * <p>
 * See the <a href="{@docRoot}guide/topics/ui/controls/pickers.html">Pickers</a>
 * guide.
 * </p>
 * 
 * @hide
 */
public class CareDatePickerDialog extends CareDialog implements OnClickListener, OnDateChangedListener {

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";

    private final CareDatePicker mDatePicker;
    public OnDateSetListener mCallBack;
    private final Calendar mCalendar;

    private boolean mTitleNeedsUpdate = true;

    Context mContext;

    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateSetListener {

        /**
         * @param view
         *            The view associated with this listener.
         * @param year
         *            The year that was set.
         * @param monthOfYear
         *            The month that was set (0-11) for compatibility with
         *            {@link java.util.Calendar}.
         * @param dayOfMonth
         *            The day of the month that was set.
         */
        void onDateSet(CareDatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    /**
     * @param context
     *            The context the dialog is to run in.
     * @param callBack
     *            How the parent is notified that the date is set.
     * @param year
     *            The initial year of the dialog.
     * @param monthOfYear
     *            The initial month of the dialog.
     * @param dayOfMonth
     *            The initial day of the dialog.
     */
    public CareDatePickerDialog(Context context, int year, int monthOfYear, int dayOfMonth) {
        this(context, 0, year, monthOfYear, dayOfMonth);
    }

    /**
     * @param context
     *            The context the dialog is to run in.
     * @param theme
     *            the theme to apply to this dialog
     * @param callBack
     *            How the parent is notified that the date is set.
     * @param year
     *            The initial year of the dialog.
     * @param monthOfYear
     *            The initial month of the dialog.
     * @param dayOfMonth
     *            The initial day of the dialog.
     */
    public CareDatePickerDialog(Context context, int theme, int year, int monthOfYear, int dayOfMonth) {
        super(context, theme);

        mContext = context;

        mCalendar = Calendar.getInstance();

        Context themeContext = getContext();
        setButton(BUTTON_POSITIVE, themeContext.getText(R.string.date_time_done), this);
        setIcon(0);

        LayoutInflater inflater = (LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.care_date_picker_dialog, null);
        setView(view);
        mDatePicker = (CareDatePicker) view.findViewById(R.id.datePicker);
        mDatePicker.init(year, monthOfYear, dayOfMonth, this);
        updateTitle(year, monthOfYear, dayOfMonth);
    }

    public void setOnDateSetListener(OnDateSetListener callBack) {
        mCallBack = callBack;
    }

    public void onClick(DialogInterface dialog, int which) {
        tryNotifyDateSet();
    }

    public void onDateChanged(CareDatePicker view, int year, int month, int day) {
        mDatePicker.init(year, month, day, this);
        updateTitle(year, month, day);
    }

    /**
     * Gets the {@link DatePicker} contained in this dialog.
     * 
     * @return The calendar view.
     */
    public CareDatePicker getDatePicker() {
        return mDatePicker;
    }

    /**
     * Sets the current date.
     * 
     * @param year
     *            The date year.
     * @param monthOfYear
     *            The date month.
     * @param dayOfMonth
     *            The date day of month.
     */
    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    private void tryNotifyDateSet() {
        if (mCallBack != null) {
            mDatePicker.clearFocus();
            mCallBack.onDateSet(mDatePicker, mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
        }
    }

    @Override
    protected void onStop() {
        // tryNotifyDateSet();
        super.onStop();
    }

    private void updateTitle(int year, int month, int day) {
        if (!mDatePicker.getCalendarViewShown()) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, day);
            String title = DateUtils.formatDateTime(mContext, mCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
                    | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY);
            setTitle(title);
            mTitleNeedsUpdate = true;
        } else {
            if (mTitleNeedsUpdate) {
                mTitleNeedsUpdate = false;
                setTitle(R.string.date_picker_dialog_title);
            }
        }
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, mDatePicker.getYear());
        state.putInt(MONTH, mDatePicker.getMonth());
        state.putInt(DAY, mDatePicker.getDayOfMonth());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt(YEAR);
        int month = savedInstanceState.getInt(MONTH);
        int day = savedInstanceState.getInt(DAY);
        mDatePicker.init(year, month, day, this);
    }
}
