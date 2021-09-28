package cn.huoxian.dongtai.plugin.action;

import cn.huoxian.dongtai.plugin.dialog.TaintConfigDialog;
import cn.huoxian.dongtai.plugin.util.TaintConstant;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.compiled.ClsFileImpl;
import com.intellij.psi.impl.source.PsiJavaFileImpl;

import static cn.huoxian.dongtai.plugin.notify.DongTaiNotifier.notificationWarning;

/**
 * @author niuerzhuang@huoxian.cn
 */
public class TaintConfigAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        showDialog(e);
    }

    private void showDialog(AnActionEvent e) {
        try {
            StringBuilder hook = new StringBuilder();
            PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
            PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
            PsiMethod psiMethod = (PsiMethod) psiElement;
            String packageName = getPackageName(psiFile);
            String className = getClassName(psiFile);
            String classKind = getClassKind(psiFile);
            String methodName = getMethodName(psiMethod, className);
            String parameterTypes = getParameterTypes(psiMethod);
            hook.append(packageName).append(".").append(className).append(".").append(methodName).append("(").append(parameterTypes);
            String method = String.valueOf(hook);
            Project eventProject = getEventProject(e);
            TaintConfigDialog dialog = new TaintConfigDialog(method, classKind,eventProject);
            dialog.pack();
            dialog.setSize(700, 300);
            dialog.setTitle(TaintConstant.NAME_DONGTAI_IAST_RULE_ADD);
            dialog.setVisible(true);
        } catch (Exception ignore) {
            notificationWarning(getEventProject(e),TaintConstant.NOTIFICATION_CONTENT_WARNING_METHOD);
        }
    }

    private String getPackageName(PsiFile psiFile) {
        try {
            return ((PsiJavaFileImpl) psiFile).getPackageName();
        } catch (Exception ignore) {
            if (psiFile instanceof ClsFileImpl) {
                return ((ClsFileImpl) psiFile).getPackageName();
            }
        }
        return "<no package!>";
    }

    private String getClassName(PsiFile psiFile) {
        try {
            return ((PsiJavaFileImpl) psiFile).getClasses()[0].getName();
        } catch (Exception ignore) {
            if (psiFile instanceof ClsFileImpl) {
                return ((ClsFileImpl) psiFile).getClasses()[0].getName();
            }
        }
        return "<no class name!>";
    }

    private String getClassKind(PsiFile psiFile) {
        try {
            return ((PsiJavaFileImpl) psiFile).getClasses()[0].getClassKind().name();
        } catch (Exception ignore) {
            if (psiFile instanceof ClsFileImpl) {
                return ((ClsFileImpl) psiFile).getClasses()[0].getClassKind().name();
            }
        }
        return "<no class kind!>";
    }

    private String getMethodName(PsiMethod psiMethod, String className) {
        String methodName = psiMethod.getName();
        if (className.equals(methodName)) {
            methodName = "<init>";
        }
        return methodName;
    }

    private String getParameterTypes(PsiMethod psiMethod) {
        StringBuilder parameterTypesStr = new StringBuilder();
        PsiType[] parameterTypes = psiMethod.getSignature(PsiSubstitutor.EMPTY).getParameterTypes();
        for (PsiType psiType : parameterTypes
        ) {
            parameterTypesStr.append(psiType.getInternalCanonicalText()).append(",");
        }
        String superfluousWords = ",";
        if (parameterTypesStr.lastIndexOf(superfluousWords) != -1) {
            parameterTypesStr.deleteCharAt(parameterTypesStr.lastIndexOf(",")).append(")");
        } else {
            parameterTypesStr.append(")");
        }
        return String.valueOf(parameterTypesStr);
    }
}
