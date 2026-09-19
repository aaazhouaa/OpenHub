package com.thirtydegreesray.openhub.mvp.contract;

import com.thirtydegreesray.openhub.mvp.contract.base.IBaseContract;
import com.thirtydegreesray.openhub.mvp.contract.base.IBaseListContract;
import com.thirtydegreesray.openhub.mvp.contract.base.IBasePagerContract;
import com.thirtydegreesray.openhub.mvp.model.CodeSearchItem;

import java.util.ArrayList;

/**
 * Contract for the code search results list.
 */
public interface ICodeSearchContract {

    interface View extends IBaseContract.View, IBasePagerContract.View, IBaseListContract.View {
        void showCodeSearchResult(ArrayList<CodeSearchItem> items);
    }

    interface Presenter extends IBasePagerContract.Presenter<ICodeSearchContract.View> {
        void searchCode(int page);
    }
}
