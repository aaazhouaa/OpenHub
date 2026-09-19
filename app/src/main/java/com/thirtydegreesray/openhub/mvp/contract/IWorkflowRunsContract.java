package com.thirtydegreesray.openhub.mvp.contract;

import com.thirtydegreesray.openhub.mvp.contract.base.IBaseContract;
import com.thirtydegreesray.openhub.mvp.contract.base.IBaseListContract;
import com.thirtydegreesray.openhub.mvp.contract.base.IBasePagerContract;
import com.thirtydegreesray.openhub.mvp.model.WorkflowRun;

import java.util.ArrayList;

/**
 * Contract for the GitHub Actions workflow runs list.
 */
public interface IWorkflowRunsContract {

    interface View extends IBaseContract.View, IBasePagerContract.View, IBaseListContract.View {
        void showWorkflowRuns(ArrayList<WorkflowRun> runs);
    }

    interface Presenter extends IBaseContract.Presenter<IWorkflowRunsContract.View> {
        void loadWorkflowRuns(int page, boolean isReload);
    }
}
