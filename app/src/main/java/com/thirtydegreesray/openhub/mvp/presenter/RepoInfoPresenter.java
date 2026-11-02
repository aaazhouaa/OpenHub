

package com.thirtydegreesray.openhub.mvp.presenter;

import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.AppConfig;
import com.thirtydegreesray.openhub.AppData;
import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.dao.DaoSession;
import com.thirtydegreesray.openhub.http.core.HttpObserver;
import com.thirtydegreesray.openhub.http.core.HttpResponse;
import com.thirtydegreesray.openhub.http.error.HttpPageNoFoundError;
import com.thirtydegreesray.openhub.http.model.FileRequestModel;
import com.thirtydegreesray.openhub.mvp.contract.IRepoInfoContract;
import com.thirtydegreesray.openhub.mvp.model.FileModel;
import com.thirtydegreesray.openhub.mvp.model.Repository;
import com.thirtydegreesray.openhub.mvp.model.RepositoryPermissions;
import com.thirtydegreesray.openhub.mvp.presenter.base.BasePagerPresenter;
import com.thirtydegreesray.openhub.util.StringUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;

/**
 * Created by ThirtyDegreesRay on 2017/8/11 11:34:31
 */

public class RepoInfoPresenter extends BasePagerPresenter<IRepoInfoContract.View>
        implements IRepoInfoContract.Presenter{

    @AutoAccess Repository repository;
    @AutoAccess String curBranch = "";
    private String readmeSource;
    private FileModel readmeFile;

    private static final Pattern README_PATH_PATTERN = Pattern.compile("data-path=\"([^\"]+)\"");
    private static final String DEFAULT_README_PATH = "README.md";
    private static final String DEFAULT_COMMIT_MESSAGE = "Update README.md";

    @Inject
    public RepoInfoPresenter(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public void onViewInitialized() {
        super.onViewInitialized();
    }

    @Override
    protected void loadData() {
        mView.showRepoInfo(repository);
        if(readmeSource == null){
            loadReadMe();
        }
    }

    @Override
    public void loadReadMe() {
        final String readmeFileUrl = AppConfig.GITHUB_API_BASE_URL + "repos/" + getFullName()
                + "/readme" + (StringUtils.isBlank(curBranch) ? "" : "?ref=" + Uri.encode(curBranch));
        mView.showReadMeLoader();
        HttpObserver<ResponseBody> httpObserver = new HttpObserver<ResponseBody>() {
            @Override
            public void onError(Throwable error) {
                if(error instanceof HttpPageNoFoundError){
                    mView.showNoReadMe();
                } else {
                    // leave the page retryable, so switching back to this tab reloads it
                    setLoaded(false);
                    mView.showReadMeLoadError(getErrorTip(error));
                }
            }

            @Override
            public void onSuccess(HttpResponse<ResponseBody> response) {
                try {
                    readmeSource = response.body().string();
                    mView.showReadMe(readmeSource, getReadmeBaseUrl(readmeSource));
                } catch (IOException e) {
                    mView.showReadMeLoadError(getErrorTip(e));
                }
            }
        };
        // The readme is fetched without reading the http cache first: a cached page keeps
        // the image urls of the time it was fetched, and GitHub signs those with a short
        // lifetime, so serving it again leaves broken images behind.
        generalRxHttpExecute(new IObservableCreator<ResponseBody>() {
            @Override
            public Observable<Response<ResponseBody>> createObservable(boolean forceNetWork) {
                return getRepoService().getFileAsHtmlStream(forceNetWork, readmeFileUrl);
            }
        }, httpObserver, false);

    }

    @NonNull
    private String getFullName() {
        if (!StringUtils.isBlank(repository.getFullName())) return repository.getFullName();
        return repository.getOwner().getLogin() + "/" + repository.getName();
    }

    /**
     * Relative links of a README resolve against the folder holding that README, so the
     * base url has to use the real readme path reported by the API instead of a guess.
     */
    @NonNull
    private String getReadmeBaseUrl(@NonNull String readmeHtml) {
        String path = DEFAULT_README_PATH;
        Matcher matcher = README_PATH_PATTERN.matcher(readmeHtml);
        if (matcher.find()) path = matcher.group(1);
        String branch = StringUtils.isBlank(curBranch) ? repository.getDefaultBranch() : curBranch;
        return AppConfig.GITHUB_BASE_URL + getFullName() + "/blob/" + branch + "/" + path;
    }

    public Repository getRepository() {
        return repository;
    }

    /**
     * 当前用户是否可编辑 README（仓库 owner 本人或拥有 push/admin 权限）。
     */
    @Override
    public boolean canEdit() {
        if (repository == null || repository.getOwner() == null) return false;
        if (AppData.INSTANCE.getLoggedUser() == null) return false;
        String logged = AppData.INSTANCE.getLoggedUser().getLogin();
        if (repository.getOwner().getLogin().equalsIgnoreCase(logged)) return true;
        RepositoryPermissions p = repository.getPermissions();
        return p != null && (p.isAdmin() || p.isPush());
    }

    /**
     * 读取 README 的原始 markdown 文本与 sha，供编辑使用。
     */
    @Override
    public void loadReadmeForEdit() {
        HttpObserver<FileModel> httpObserver = new HttpObserver<FileModel>() {
            @Override
            public void onError(Throwable error) {
                mView.showErrorToast(getErrorTip(error));
            }

            @Override
            public void onSuccess(HttpResponse<FileModel> response) {
                FileModel file = response.body();
                if (file == null || StringUtils.isBlank(file.getContent())) {
                    mView.showErrorToast(getString(R.string.readme_load_failed));
                    return;
                }
                readmeFile = file;
                String raw = new String(Base64.decode(file.getContent(), Base64.DEFAULT));
                mView.onReadmeEditContentLoaded(raw);
            }
        };
        generalRxHttpExecute(forceNetWork ->
                getRepoService().getReadme(repository.getOwner().getLogin(), repository.getName(),
                        StringUtils.isBlank(curBranch) ? null : curBranch),
                httpObserver, false);
    }

    /**
     * 提交 README 更新（固定 commit message）。
     */
    @Override
    public void updateReadme(String content) {
        if (readmeFile == null) {
            mView.showErrorToast(getString(R.string.readme_load_failed));
            return;
        }
        String encoded = Base64.encodeToString(content.getBytes(), Base64.NO_WRAP);
        final String path = StringUtils.isBlank(readmeFile.getPath())
                ? DEFAULT_README_PATH : readmeFile.getPath();
        FileRequestModel model = new FileRequestModel(DEFAULT_COMMIT_MESSAGE, encoded,
                readmeFile.getSha(), StringUtils.isBlank(curBranch) ? null : curBranch);
        HttpObserver<FileModel> httpObserver = new HttpObserver<FileModel>() {
            @Override
            public void onError(Throwable error) {
                mView.showErrorToast(getErrorTip(error));
            }

            @Override
            public void onSuccess(HttpResponse<FileModel> response) {
                readmeFile = response.body();
                readmeSource = null;
                setLoaded(false);
                prepareLoadData();
                mView.onReadmeUpdated();
            }
        };
        generalRxHttpExecute(forceNetWork ->
                getRepoService().updateFile(repository.getOwner().getLogin(), repository.getName(),
                        path, model),
                httpObserver, false, mView.getProgressDialog(getLoadTip()));
    }

    /**
     * check if the string size is too large to save
     */
    private void checkReadmeSourceSize(){
        if(readmeSource != null && readmeSource.getBytes().length > 128 * 1024){
            readmeSource = null;
        }
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void setCurBranch(String curBranch) {
        this.curBranch = curBranch;
        readmeSource = null;
    }
}
