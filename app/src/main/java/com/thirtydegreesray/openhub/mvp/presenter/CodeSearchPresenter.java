package com.thirtydegreesray.openhub.mvp.presenter;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.common.Event;
import com.thirtydegreesray.openhub.dao.DaoSession;
import com.thirtydegreesray.openhub.http.core.HttpObserver;
import com.thirtydegreesray.openhub.http.core.HttpResponse;
import com.thirtydegreesray.openhub.http.error.HttpPageNoFoundError;
import com.thirtydegreesray.openhub.mvp.contract.ICodeSearchContract;
import com.thirtydegreesray.openhub.mvp.model.CodeSearchItem;
import com.thirtydegreesray.openhub.mvp.model.SearchModel;
import com.thirtydegreesray.openhub.mvp.model.SearchResult;
import com.thirtydegreesray.openhub.mvp.presenter.base.BasePagerPresenter;
import com.thirtydegreesray.openhub.util.StringUtils;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;

import retrofit2.Response;
import rx.Observable;

/**
 * Performs code search against the GitHub code search API.
 */
public class CodeSearchPresenter extends BasePagerPresenter<ICodeSearchContract.View>
        implements ICodeSearchContract.Presenter {

    @AutoAccess SearchModel searchModel;

    private ArrayList<CodeSearchItem> items;

    @Inject
    public CodeSearchPresenter(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public void onViewInitialized() {
        super.onViewInitialized();
        setEventSubscriber(true);
    }

    @Override
    protected void loadData() {
        if (searchModel != null) searchCode(1);
    }

    @Override
    public void searchCode(final int page) {
        mView.showLoading();
        HttpObserver<SearchResult<CodeSearchItem>> httpObserver =
                new HttpObserver<SearchResult<CodeSearchItem>>() {
                    @Override
                    public void onError(Throwable error) {
                        mView.hideLoading();
                        handleError(error);
                    }

                    @Override
                    public void onSuccess(HttpResponse<SearchResult<CodeSearchItem>> response) {
                        mView.hideLoading();
                        if (items == null || page == 1) {
                            items = response.body().getItems();
                        } else {
                            items.addAll(response.body().getItems());
                        }
                        if (response.body().getItems().size() == 0 && items.size() != 0) {
                            mView.setCanLoadMore(false);
                        } else {
                            mView.showCodeSearchResult(items);
                        }
                    }
                };
        generalRxHttpExecute(new IObservableCreator<SearchResult<CodeSearchItem>>() {
            @Override
            public Observable<Response<SearchResult<CodeSearchItem>>> createObservable(
                    boolean forceNetWork) {
                return getSearchService().searchCode(forceNetWork, searchModel.getQuery(),
                        searchModel.getSort(), searchModel.getOrder(), page);
            }
        }, httpObserver);
    }

    @Subscribe
    public void onSearchEvent(Event.SearchEvent searchEvent) {
        if (!searchEvent.searchModel.getType().equals(SearchModel.SearchType.Code)) return;
        setLoaded(false);
        this.searchModel = searchEvent.searchModel;
        prepareLoadData();
    }

    private void handleError(Throwable error) {
        if (!StringUtils.isBlankList(items)) {
            mView.showErrorToast(getErrorTip(error));
        } else if (error instanceof HttpPageNoFoundError) {
            mView.showCodeSearchResult(new ArrayList<CodeSearchItem>());
        } else {
            mView.showLoadError(getErrorTip(error));
        }
    }
}
