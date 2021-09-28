package cn.huoxian.dongtai.plugin.dialog;

import cn.huoxian.dongtai.plugin.pojo.Taint;
import cn.huoxian.dongtai.plugin.pojo.TaintConvert;
import cn.huoxian.dongtai.plugin.util.GetJson;
import cn.huoxian.dongtai.plugin.util.TaintConstant;
import cn.huoxian.dongtai.plugin.util.TaintUtil;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static cn.huoxian.dongtai.plugin.notify.DongTaiNotifier.notificationInfo;
import static cn.huoxian.dongtai.plugin.notify.DongTaiNotifier.notificationWarning;

/**
 * @author niuerzhuang@huoxian.cn
 **/
public class TaintListWindow {
    private JPanel jContent;
    private JButton refreshListButton;
    private JTextField searchTextField;
    private JButton searchConfirmButton;
    private JButton searchResetButton;
    private JTable contentTable;
    private JComboBox<String> searchCriteria;
    private JButton detailButton;
    private JTextField detailTextField;
    private List<Taint> taints;
    private String json = "";
    private int size;

    public TaintListWindow(Project project) {
        init();
        searchConfirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                confirm((String) searchCriteria.getSelectedItem());
            }
        });
        searchResetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                searchTextField.setText("");
                refresh();
            }
        });
        refreshListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                refresh();
                timeTaskNotice(90000, 10000,project);
                notificationInfo(project,"已刷新");
            }
        });
        detailButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    int selectedRow = contentTable.getSelectedRow();
                    Taint taint = taints.get(selectedRow);
                    String detail = taint.getDetail();
                    Desktop desktop = Desktop.getDesktop();
                    URI uri = new URI(detail);
                    desktop.browse(uri);
                } catch (Exception e) {
                    notificationWarning(project,"请在列表中选择漏洞");
                }
            }
        });
        contentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int selectedRow = contentTable.getSelectedRow();
                int selectedColumn = contentTable.getSelectedColumn();
                String valueAt = (String) contentTable.getValueAt(selectedRow, selectedColumn);
                detailTextField.setText(valueAt);
            }
        });
    }

    public void init() {
        contentTable.setModel(TaintConstant.TABLE_MODEL);
        contentTable.setEnabled(true);
        detailTextField.setEditable(false);
        refresh();
    }

    public void refresh() {
        removeAll();
        json = GetJson.getTaintsJson();
        taints = getTaints(json);
        try {
            size = taints.size();
            for (Taint taint : taints
            ) {
                String taintDetail = TaintUtil.config("URL") +TaintConstant.TAINT_DETAIL;
                taint.setDetail( taintDetail+ taint.getId());
                TaintConstant.TABLE_MODEL.addRow(TaintConvert.convert(taint));
            }
        } catch (Exception e) {
            size = 0;
        }
    }

    public void timeTaskNotice(Integer delay, Integer period,Project project) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                json = GetJson.getTaintsJson();
                String newJson = GetJson.getTaintsCountJson();
                int newSize;
                try {
                    newSize = getTaintsCount(newJson);
                } catch (Exception e) {
                    newSize = size;
                }
                if (size != newSize) {
                    notificationWarning(project,"发现新漏洞");
                    List<Taint> newTaints = getTaints(json);
                    refreshWithJson(newTaints);
                }
            }
        }, delay, period);
    }

    public void refreshWithJson(List<Taint> newTaints) {
        removeAll();
        taints = newTaints;
        try {
            size = taints.size();
            for (Taint taint : taints
            ) {
                taint.setDetail(TaintConstant.TAINT_DETAIL + taint.getId());
                TaintConstant.TABLE_MODEL.addRow(TaintConvert.convert(taint));
            }
        } catch (Exception e) {
            size = 0;
        }
    }

    public List<Taint> getTaints(String json) {
        JSONObject jsonObject = new JSONObject(json);
        try {
            JSONArray data = jsonObject.getJSONArray("data");
            List<Taint> taints = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = (JSONObject) data.get(i);
                // [{"id": 31398, "type": "\u547d\u4ee4\u6267\u884c ", "level": null, "url": "http://localhost:8080/cmdi", "http_method": "GET", "top_stack": "org.springframework.web.method.support.HandlerMethodArgumentResolver.resolveArgument", "bottom_stack": "java.lang.Runtime.exec", "hook_type_id": 40}]
                Taint taint = new Taint();
                Integer id = (Integer) o.get("id");
                taint.setId(id.toString());
                taint.setType(o.get("type").toString());
                taint.setLevel(o.get("level").toString());
                taint.setUrl(o.get("url").toString());
                taint.setHttp_method(o.get("http_method").toString());
                taint.setTop_stack(o.get("top_stack").toString());
                taint.setBottom_stack(o.get("bottom_stack").toString());
                taints.add(taint);
            }
            return taints;
        } catch (Exception exception) {
            return new ArrayList<>();
        }
    }

    public Integer getTaintsCount(String json) {
        JSONObject jsonObject = new JSONObject(json);
        try {
            return jsonObject.getInt("data");
        } catch (Exception exception) {
            return null;
        }
    }

    public JPanel getJContent() {
        return jContent;
    }

    public List<Taint> searchUrl(String requirement) {
        List<Taint> urls = new ArrayList<>();
        for (Taint taint : taints
        ) {
            String url = taint.getUrl();
            if (StringUtils.containsIgnoreCase(url, requirement)) {
                urls.add(taint);
            }
        }
        return urls;
    }

    public List<Taint> searchLevel(String requirement) {
        List<Taint> levels = new ArrayList<>();
        for (Taint taint : taints
        ) {
            String level = taint.getLevel();
            if (StringUtils.containsIgnoreCase(level, requirement)) {
                levels.add(taint);
            }
        }
        return levels;
    }

    public void removeAll() {
        while (TaintConstant.TABLE_MODEL.getRowCount() > 0) {
            TaintConstant.TABLE_MODEL.removeRow(0);
        }
    }

    public void confirm(String confirm) {
        String urlConfirm = "url";
        String levelConfirm = "等级";
        if (urlConfirm.equals(confirm)) {
            removeAll();
            List<Taint> urls = searchUrl(searchTextField.getText());
            for (Taint url : urls
            ) {
                TaintConstant.TABLE_MODEL.addRow(TaintConvert.convert(url));
            }
        }
        if (levelConfirm.equals(confirm)) {
            removeAll();
            List<Taint> levels = searchLevel(searchTextField.getText());
            for (Taint level : levels
            ) {
                TaintConstant.TABLE_MODEL.addRow(TaintConvert.convert(level));
            }
        }
    }
}
