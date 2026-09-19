package com.thirtydegreesray.openhub.mvp.presenter;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.dao.DaoSession;
import com.thirtydegreesray.openhub.http.core.HttpObserver;
import com.thirtydegreesray.openhub.http.core.HttpResponse;
import com.thirtydegreesray.openhub.mvp.contract.IPullRequestFilesContract;
import com.thirtydegreesray.openhub.mvp.model.CommitFile;
import com.thirtydegreesray.openhub.mvp.model.CommitFilesPathModel;
import com.thirtydegreesray.openhub.mvp.presenter.base.BasePresenter;
import com.thirtydegreesray.openhub.ui.adapter.base.DoubleTypesModel;

import java.util.ArrayList;

import javax.inject.Inject;

import retrofit2.Response;
import rx.Observable;

/**
 * Loads the changed files of a pull request.
 */
public class PullRequestFilesPresenter extends BasePresenter<IPullRequestFilesContract.View>
        implements IPullRequestFilesContract.Presenter {

    @AutoAccess String owner;
    @AutoAccess String repoName;
    @AutoAccess int number;

    private ArrayList<CommitFile> commitFiles;

    @Inject
    public PullRequestFilesPresenter(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public void loadFiles() {
        if (commitFiles != null) {
            mView.showFiles(getSortedList(commitFiles));
            return;
        }
        mView.showLoading();
        HttpObserver<ArrayList<CommitFile>> httpObserver =
                new HttpObserver<ArrayList<CommitFile>>() {
                    @Override
                    public void onError(Throwable error) {
                        mView.hideLoading();
                        mView.showLoadError(getErrorTip(error));
                    }

                    @Override
                    public void onSuccess(HttpResponse<ArrayList<CommitFile>> response) {
                        mView.hideLoading();
                        commitFiles = response.body();
                        mView.showFiles(getSortedList(commitFiles));
                    }
                };
        generalRxHttpExecute(new IObservableCreator<ArrayList<CommitFile>>() {
            @Override
            public Observable<Response<ArrayList<CommitFile>>> createObservable(boolean forceNetWork) {
                return getPullRequestService().getPullRequestFiles(forceNetWork,
                        owner, repoName, number, 1);
            }
        }, httpObserver, true);
    }

    @Override
    public ArrayList<DoubleTypesModel<CommitFilesPathModel, CommitFile>> getSortedList(
            ArrayList<CommitFile> files) {
        ArrayList<DoubleTypesModel<CommitFilesPathModel, CommitFile>> list = new ArrayList<>();
        String preBasePath = "";
        for (CommitFile file : files) {
            if (!preBasePath.equals(file.getBasePath())) {
                list.add(new DoubleTypesModel<CommitFilesPathModel, CommitFile>(
                        new CommitFilesPathModel().setPath(file.getBasePath()), null));
                preBasePath = file.getBasePath();
            }
            list.add(new DoubleTypesModel<CommitFilesPathModel, CommitFile>(null, file));
        }
        return list;
    }
}
