package com.thirtydegreesray.openhub.mvp.contract;

import com.thirtydegreesray.openhub.mvp.contract.base.IBaseContract;
import com.thirtydegreesray.openhub.mvp.contract.base.IBaseListContract;
import com.thirtydegreesray.openhub.mvp.model.WorkflowJob;

import java.util.ArrayList;

/**
 * Contract for the workflow run detail (jobs) screen.
 */
public interface IWorkflowRunDetailContract {

    interface View extends IBaseContract.View, IBaseListContract.View {
        void showJobs(ArrayList<WorkflowJob> jobs);
    }

    interface Presenter extends IBaseContract.Presenter<IWorkflowRunDetailContract.View> {
        void loadJobs();
    }
}
