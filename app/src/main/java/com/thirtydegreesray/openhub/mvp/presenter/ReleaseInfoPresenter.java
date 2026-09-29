package com.thirtydegreesray.openhub.mvp.presenter;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.dao.DaoSession;
import com.thirtydegreesray.openhub.dao.ReleaseCommit;
import com.thirtydegreesray.openhub.dao.ReleaseCommitDao;
import com.thirtydegreesray.openhub.http.core.HttpObserver;
import com.thirtydegreesray.openhub.http.core.HttpResponse;
import com.thirtydegreesray.openhub.mvp.contract.IReleaseInfoContract;
import com.thirtydegreesray.openhub.mvp.model.Release;
import com.thirtydegreesray.openhub.mvp.model.RepoCommitExt;
import com.thirtydegreesray.openhub.mvp.presenter.base.BasePresenter;
import com.thirtydegreesray.openhub.util.StringUtils;

import javax.inject.Inject;

import retrofit2.Response;
import rx.Observable;

/**
 * Created by ThirtyDegreesRay on 2017/9/16 13:11:46
 */

public class ReleaseInfoPresenter extends BasePresenter<IReleaseInfoContract.View>
        implements IReleaseInfoContract.Presenter{

    @AutoAccess String owner;
    @AutoAccess String repoName;
    @AutoAccess String tagName;
    @AutoAccess Release release;
    private ReleaseCommitDao releaseCommitDao;
    private String commitSha;

    @Inject
    public ReleaseInfoPresenter(DaoSession daoSession) {
        super(daoSession);
        releaseCommitDao = daoSession.getReleaseCommitDao();
    }

    @Override
    public void onViewInitialized() {
        super.onViewInitialized();
        if(release != null){
            mView.showReleaseInfo(release);
            loadCommitSha();
        } else {
            loadReleaseInfo();
        }
    }

    private void loadReleaseInfo(){
        mView.showLoading();
        HttpObserver<Release> httpObserver = new HttpObserver<Release>() {
            @Override
            public void onError(Throwable error) {
                mView.showErrorToast(getErrorTip(error));
                mView.hideLoading();
            }

            @Override
            public void onSuccess(HttpResponse<Release> response) {
                release = response.body();
                mView.showReleaseInfo(release);
                loadCommitSha();
                mView.hideLoading();
            }
        };
        generalRxHttpExecute(new IObservableCreator<Release>() {
            @Override
            public Observable<Response<Release>> createObservable(boolean forceNetWork) {
                return getRepoService().getReleaseByTagName(forceNetWork, owner, repoName, tagName);
            }
        }, httpObserver, true);
    }

    /**
     * 加载 tag 指向的 commit 短哈希。
     * 先查本地库（key = owner/repo/tag），命中则直接复用，不再消耗 API 配额；
     * 未命中才请求并写入缓存，失败时静默不显示。
     */
    private void loadCommitSha(){
        String tag = getTagName();
        if(StringUtils.isBlank(tag)) return;
        String key = owner.concat("/").concat(repoName).concat("/").concat(tag);
        ReleaseCommit cached = releaseCommitDao.load(key);
        if(cached != null && !StringUtils.isBlank(cached.getSha())){
            commitSha = cached.getSha();
            mView.showCommitSha(shortSha(commitSha));
            return;
        }
        HttpObserver<RepoCommitExt> httpObserver = new HttpObserver<RepoCommitExt>() {
            @Override
            public void onError(Throwable error) {
                //静默处理，不影响发布信息展示
            }

            @Override
            public void onSuccess(HttpResponse<RepoCommitExt> response) {
                RepoCommitExt commit = response.body();
                if(commit != null && !StringUtils.isBlank(commit.getSha())){
                    commitSha = commit.getSha();
                    releaseCommitDao.insertOrReplace(new ReleaseCommit(key, commitSha));
                    mView.showCommitSha(shortSha(commitSha));
                }
            }
        };
        generalRxHttpExecute(new IObservableCreator<RepoCommitExt>() {
            @Override
            public Observable<Response<RepoCommitExt>> createObservable(boolean forceNetWork) {
                return getCommitService().getCommitInfo(forceNetWork, owner, repoName, tag);
            }
        }, httpObserver, false);
    }

    private String shortSha(String sha){
        return sha == null || sha.length() <= 7 ? sha : sha.substring(0, 7);
    }

    public String getTagName(){
        return  release == null ? tagName : release.getTagName();
    }

    public String getReleaseName(){
        return release == null || StringUtils.isBlank(release.getName())
                ? tagName : release.getName();
    }

    public String getCommitSha(){
        return commitSha;
    }

    public String getRepoName() {
        return repoName;
    }

    public Release getRelease(){
        return release;
    }

    public String getOwner() {
        return owner;
    }
}
