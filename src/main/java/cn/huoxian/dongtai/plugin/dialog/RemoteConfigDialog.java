package cn.huoxian.dongtai.plugin.dialog;

import cn.huoxian.dongtai.plugin.util.TaintConstant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import static cn.huoxian.dongtai.plugin.util.TaintUtil.config;
import static cn.huoxian.dongtai.plugin.util.TaintUtil.configWrite;

/**
 * @author niuerzhuang@huoxian.cn
 */
public class RemoteConfigDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField tokenTextArea;
    private JTextField urlTextField;
    private JTextField openapi;

    public static boolean isNewToken = false;

    public RemoteConfigDialog() {
        String newAgent = isNewAgent();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setCenter();
        String agentUrlStr = config("AGENTURL");
        if (agentUrlStr == null || "".equals(agentUrlStr)) {
            openapi.setText(TaintConstant.DEFAULT_AGENT_URL);
        } else {
            openapi.setText(agentUrlStr);
        }
        String urlStr = config("URL");
        if (urlStr == null || "".equals(urlStr)) {
            urlTextField.setText(TaintConstant.DEFAULT_URL);
        } else {
            urlTextField.setText(urlStr);
        }
        String token = config("TOKEN");
        if (token == null || "".equals(token)) {
            tokenTextArea.setText(TaintConstant.DEFAULT_TOKEN);
        } else {
            tokenTextArea.setText(token);
        }
        buttonOK.addActionListener(e -> {
            String url = urlTextField.getText();
            String token1 = tokenTextArea.getText();
            String agent = openapi.getText();
            Map<String, String> remoteConfig = new HashMap<>(3);
            remoteConfig.put("URL", url);
            remoteConfig.put("TOKEN", token1);
            remoteConfig.put("AGENTURL", agent);
            configWrite(remoteConfig);
            onOK();
            if (!isNewAgent().equals(newAgent)){
                isNewToken = true;
            }
        });
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        dispose();
    }

    private String isNewAgent(){
        String agentUrlStr = config("AGENTURL");
        String url = config("URL");
        String token = config("TOKEN");
        return agentUrlStr+url+token;
    }

    private void onCancel() {
        dispose();
    }

    /**
     * 将 Dialog 居中
     */
    public void setCenter() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int width = TaintConstant.DIALOG_SIZE_WIDTH;
        int height = TaintConstant.DIALOG_SIZE_HEIGHT;
        setBounds((dimension.width - width) / 2, (dimension.height - height) / 2, width, height);
    }
}
