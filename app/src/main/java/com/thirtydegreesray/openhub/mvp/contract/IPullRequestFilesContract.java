package com.thirtydegreesray.openhub.mvp.contract;

import com.thirtydegreesray.openhub.mvp.contract.base.IBaseContract;
import com.thirtydegreesray.openhub.mvp.contract.base.IBaseListContract;
import com.thirtydegreesray.openhub.mvp.model.CommitFile;
import com.thirtydegreesray.openhub.mvp.model.CommitFilesPathModel;
import com.thirtydegreesray.openhub.ui.adapter.base.DoubleTypesModel;

import java.util.ArrayList;

/**
 * Contract for the pull request changed-files list.
 */
public interface IPullRequestFilesContract {

    interface View extends IBaseContract.View, IBaseListContract.View {
        void showFiles(ArrayList<DoubleTypesModel<CommitFilesPathModel, CommitFile>> files);
    }

    interface Presenter extends IBaseContract.Presenter<IPullRequestFilesContract.View> {
        void loadFiles();

        ArrayList<DoubleTypesModel<CommitFilesPathModel, CommitFile>> getSortedList(
                ArrayList<CommitFile> files);
    }
}
