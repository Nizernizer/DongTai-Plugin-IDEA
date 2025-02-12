package cn.huoxian.dongtai.plugin.runner;

import cn.huoxian.dongtai.plugin.dialog.RemoteConfigDialog;
import cn.huoxian.dongtai.plugin.executor.RunExecutor;
import cn.huoxian.dongtai.plugin.util.TaintConstant;
import cn.huoxian.dongtai.plugin.util.TaintUtil;
import com.intellij.execution.configurations.*;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.jar.JarApplicationConfiguration;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.target.TargetEnvironmentAwareRunProfileState;
import com.intellij.execution.ui.RunContentDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.Promise;

import java.io.File;

import static cn.huoxian.dongtai.plugin.notify.DongTaiNotifier.notificationError;
import static cn.huoxian.dongtai.plugin.util.TaintUtil.downloadAgent;

/**
 * @author niuerzhuang@huoxian.cn
 **/
public class RunCodeRunner extends DefaultJavaProgramRunner {

    @NotNull
    @Override
    public String getRunnerId() {
        return RunExecutor.RUN_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return (executorId.equals(RunExecutor.RUN_ID) && (profile instanceof ModuleRunProfile || profile instanceof JarApplicationConfiguration) && !(profile instanceof RemoteConfiguration));
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment env) {
        try {
            super.execute(env);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) {
        try {
            String url = TaintUtil.fixUrl() + "/api/v1/agent/download?url="+ TaintUtil.fixUrl() + "&language=java";
            downloadAgent(url, TaintConstant.IAST_AGENT_PATH);
        } catch (Exception e) {
            notificationError(env.getProject(),TaintConstant.NOTIFICATION_CONTENT_ERROR_FAILURE);
            RemoteConfigDialog remoteConfigDialog = new RemoteConfigDialog();
            remoteConfigDialog.pack();
            remoteConfigDialog.setTitle(TaintConstant.NAME_DONGTAI_IAST_RULE);
            remoteConfigDialog.setVisible(true);
        }
        String name = env.getProject().getName();
        JavaParameters parameters = null;
        try {
            parameters = ((JavaCommandLine) state).getJavaParameters();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ParametersList parametersList = parameters.getVMParametersList();
        parametersList.add("-javaagent:" + TaintConstant.IAST_AGENT_PATH + File.separator + TaintConstant.AGENT_NAME);
        parametersList.add("-Dproject.name=" + name);
        parametersList.add("-Diast.server.mode=local");
        RunContentDescriptor runContentDescriptor = null;
        try {
            runContentDescriptor = super.doExecute(state, env);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return runContentDescriptor;
    }

    @Override
    @NotNull
    protected Promise<RunContentDescriptor> doExecuteAsync(@NotNull TargetEnvironmentAwareRunProfileState state,
                                                                     @NotNull ExecutionEnvironment env)
    {
        try {
            String url = TaintUtil.fixUrl() + "/api/v1/agent/download?url="+ TaintUtil.fixUrl() + "&language=java";
            downloadAgent(url, TaintConstant.IAST_AGENT_PATH);
        } catch (Exception e) {
            notificationError(env.getProject(),TaintConstant.NOTIFICATION_CONTENT_ERROR_FAILURE);
            RemoteConfigDialog remoteConfigDialog = new RemoteConfigDialog();
            remoteConfigDialog.pack();
            remoteConfigDialog.setTitle(TaintConstant.NAME_DONGTAI_IAST_RULE);
            remoteConfigDialog.setVisible(true);
        }
        String name = env.getProject().getName();
        JavaParameters parameters = null;
        try {
            parameters = ((JavaCommandLine) state).getJavaParameters();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ParametersList parametersList = parameters.getVMParametersList();
        parametersList.add("-javaagent:" + TaintConstant.IAST_AGENT_PATH + File.separator + TaintConstant.AGENT_NAME);
        parametersList.add("-Dproject.name=" + name);
        Promise<RunContentDescriptor> runContentDescriptor = null;
        try {
            runContentDescriptor = super.doExecuteAsync(state, env);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return runContentDescriptor;
    }

}
