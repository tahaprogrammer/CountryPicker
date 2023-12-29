package com.country.codepickerlib;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

class DropDown extends PopupWindow {

    private Context context;
    private View rootView;

    private final CountryCodePicker codePicker;
    private final PropertiesDropDown properties;
    private final String countryCodeName;

    private CountryCodeAdapter cca;
    private CountryCodeAdapter.SortType currentSortType = new CountryCodeAdapter.SortType(); // default

    private DropDownEventsListener dropDownEventsListener;

    public DropDown(final CountryCodePicker codePicker, final String countryCodeName) {
        super(codePicker.getContext());
        this.context = codePicker.getContext();
        this.codePicker = codePicker;
        this.properties = codePicker.getPropertiesDropDown();
        this.countryCodeName = countryCodeName;
        this.dropDownEventsListener = properties.getDropDownEventsListener();

        inflateRoot();
        init();
    }

    private void inflateRoot() {
        rootView = LayoutInflater.from(context).inflate(R.layout.drop_down, null);
        setContentView(rootView);
    }

    private void init() {
        context = codePicker.getContext();
        codePicker.refreshCustomMasterList();
        codePicker.refreshPreferredCountries();

        List<CCPCountry> masterCountries = CCPCountry.getCustomMasterCountryList(context, codePicker);
        //keyboard
        final EditText searchField = rootView.findViewById(R.id.searchField);

        if (properties.isSearchAllowed() && properties.isKeyboardAutoPopup()) {
            UtilsKeyboard.showKeyboard(context);
        } else {
            UtilsKeyboard.hideKeyboard(context, searchField);
        }

        sort(currentSortType);

        RecyclerView countryList = rootView.findViewById(R.id.countryList);
        LinearLayout searchLayout = rootView.findViewById(R.id.searchLayout);
        ImageView clearQuery = rootView.findViewById(R.id.clearQuery);
        TextView noResult = rootView.findViewById(R.id.noResult);

        // type faces
        //set type faces
        try {
            if (properties.getDropDownTypeFace() != null) {
                if (properties.getDropDownTypeFaceStyle() != CountryCodePicker.DEFAULT_UNSET) {
                    noResult.setTypeface(properties.getDropDownTypeFace(), properties.getDropDownTypeFaceStyle());
                    searchField.setTypeface(properties.getDropDownTypeFace(), properties.getDropDownTypeFaceStyle());
                } else {
                    noResult.setTypeface(properties.getDropDownTypeFace());
                    searchField.setTypeface(properties.getDropDownTypeFace());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //dropdown background color
        if (properties.getBackgroundColor() != 0) {
            rootView.setBackgroundColor(properties.getBackgroundColor());
        }

        //clear button color and title color
        if (properties.getTextColor() != 0) {
            int textColor = properties.getTextColor();
            clearQuery.setColorFilter(textColor);
            noResult.setTextColor(textColor);
            searchField.setTextColor(textColor);
            searchField.setHintTextColor(Color.argb(100, Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
        }


        //editText tint
        // TODO: 7/11/19 Cursor drawable and background drawable selection
        if (properties.getSearchEditTextTintColor() != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                searchField.setBackgroundTintList(ColorStateList.valueOf(properties.getSearchEditTextTintColor()));
//				setCursorColor(searchField, properties.getSearchEditTextTintColor());
            }
        }

        //add messages to views
        searchField.setHint(context.getString(R.string.search_hint));
        noResult.setText(context.getString(R.string.no_result_found));

        //this will make popup dropdown compact
        if (!properties.isSearchAllowed()) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) countryList.getLayoutParams();
            params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
            rootView.setLayoutParams(params);
        }

        cca = new CountryCodeAdapter(context, masterCountries, codePicker, searchLayout, searchField, noResult, this, clearQuery, null);
        countryList.setLayoutManager(new LinearLayoutManager(context));
        countryList.setAdapter(cca);


        //auto scroll to mentioned countryNameCode
        if (countryCodeName != null) {
            boolean isPreferredCountry = false;
            if (codePicker.preferredCountries != null) {
                for (CCPCountry preferredCountry : codePicker.preferredCountries) {
                    if (preferredCountry.getNameCode().equalsIgnoreCase(countryCodeName)) {
                        isPreferredCountry = true;
                        break;
                    }
                }
            }

            //if selection is from preferred countries then it should show all (or maximum) preferred countries.
            // don't scroll if it was one of those preferred countries
            if (!isPreferredCountry) {
                int preferredCountriesOffset = 0;
                if (codePicker.preferredCountries != null && codePicker.preferredCountries.size() > 0) {
                    preferredCountriesOffset = codePicker.preferredCountries.size() + 1; //+1 is for divider
                }
                for (int i = 0; i < masterCountries.size(); i++) {
                    if (masterCountries.get(i).getNameCode().equalsIgnoreCase(countryCodeName)) {
                        countryList.scrollToPosition(i + preferredCountriesOffset);
                        break;
                    }
                }
            }
        }

        setOnDismissListener(() -> {
            if (dropDownEventsListener != null) {
                dropDownEventsListener.onDropDownDismiss();
            }
        });
    }

    private void rotateWithAnimation(ImageView imageView, CountryCodeAdapter.SortType.Order order) {
        float degrees = 0;
        if (order == CountryCodeAdapter.SortType.Order.DESC) {
            degrees = 180;
        }
        imageView.animate().rotation(degrees).setDuration(400).start();
    }


    private void sort(CountryCodeAdapter.SortType selectedSortType) {
        currentSortType = selectedSortType;
        if (cca != null) {
            cca.sort(selectedSortType);
        }
    }

    public void showAsDropDown(View view) {
        super.showAsDropDown(view);
        if (dropDownEventsListener != null) {
            dropDownEventsListener.onDropDownOpen();
        }
    }
}
