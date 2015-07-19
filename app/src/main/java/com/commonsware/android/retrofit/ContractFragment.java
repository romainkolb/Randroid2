package com.commonsware.android.retrofit;

import android.app.Activity;
import android.support.v4.app.Fragment;

// derived from https://gist.github.com/JakeWharton/2621173

public class ContractFragment<T> extends Fragment {
    private T contract;

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        try {
            contract = (T) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(((Object) activity).getClass()
                    .getSimpleName()
                    + " does not implement contract interface for "
                    + ((Object) this).getClass().getSimpleName(), e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        contract = null;
    }

    protected final T getContract() {
        return (contract);
    }

}
