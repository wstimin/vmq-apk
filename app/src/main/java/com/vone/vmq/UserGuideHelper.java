package com.vone.vmq;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vone.qrcode.R;

/**
 * 用户引导助手
 * 提供用户友好的权限请求说明和功能引导
 * 需求: 2.4, 4.2
 */
public class UserGuideHelper {

    private static final String TAG = "UserGuideHelper";
    private static final String PREFS_NAME = "user_guide_prefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_PERMISSION_GUIDE_SHOWN = "permission_guide_shown";

    private Context context;
    private SharedPreferences prefs;

    public UserGuideHelper(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 检查是否是首次启动
     * @return 是否首次启动
     */
    public boolean isFirstLaunch() {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    /**
     * 标记首次启动完成
     */
    public void markFirstLaunchComplete() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }

    /**
     * 显示欢迎引导对话框
     */
    public void showWelcomeGuide() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("欢迎使用V免签")
               .setMessage("V免签是一个免费的支付监控应用，可以帮助您监听支付宝和微信的收款通知。\n\n" +
                          "为了正常使用，应用需要以下权限：\n" +
                          "• 通知访问权限 - 监听支付通知\n" +
                          "• 网络权限 - 发送收款信息\n" +
                          "• 存储权限 - 保存配置和日志\n" +
                          "• 相机权限 - 扫描配置二维码\n\n" +
                          "点击\"开始使用\"继续设置。")
               .setPositiveButton("开始使用", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       markFirstLaunchComplete();
                       showPermissionGuide();
                   }
               })
               .setNegativeButton("稍后", null)
               .setCancelable(false)
               .show();
    }

    /**
     * 显示权限引导
     */
    public void showPermissionGuide() {
        if (prefs.getBoolean(KEY_PERMISSION_GUIDE_SHOWN, false)) {
            return;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("权限设置指南")
               .setMessage("请按照以下步骤设置必要权限：\n\n" +
                          "1. 通知访问权限\n" +
                          "   设置 → 应用 → 特殊访问权限 → 通知访问权限\n" +
                          "   找到\"V免签\"并开启\n\n" +
                          "2. 电池优化白名单\n" +
                          "   设置 → 电池 → 电池优化\n" +
                          "   找到\"V免签\"并选择\"不优化\"\n\n" +
                          "3. 自启动权限（部分手机）\n" +
                          "   设置 → 应用管理 → 自启动管理\n" +
                          "   找到\"V免签\"并开启")
               .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       prefs.edit().putBoolean(KEY_PERMISSION_GUIDE_SHOWN, true).apply();
                   }
               })
               .setNeutralButton("查看详细说明", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       showDetailedPermissionGuide();
                   }
               })
               .show();
    }

    /**
     * 显示详细权限说明
     */
    public void showDetailedPermissionGuide() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("详细权限说明")
               .setMessage("各项权限的作用说明：\n\n" +
                          "📱 通知访问权限\n" +
                          "用于监听支付宝和微信的收款通知，这是应用的核心功能。\n\n" +
                          "🌐 网络权限\n" +
                          "用于将收款信息发送到您配置的服务器地址。\n\n" +
                          "💾 存储权限\n" +
                          "用于保存应用配置、日志文件和扫码结果。\n\n" +
                          "📷 相机权限\n" +
                          "用于扫描配置二维码，快速完成应用配置。\n\n" +
                          "🔋 电池优化白名单\n" +
                          "确保应用在后台正常运行，不被系统杀死。\n\n" +
                          "所有权限都是为了应用正常功能所必需的，我们承诺不会滥用您的权限。")
               .setPositiveButton("明白了", null)
               .show();
    }

    /**
     * 显示权限被拒绝的处理引导
     * @param permissionName 权限名称
     * @param permissionDescription 权限描述
     */
    public void showPermissionDeniedGuide(String permissionName, String permissionDescription) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("权限被拒绝")
               .setMessage(permissionName + "被拒绝。\n\n" + permissionDescription + "\n\n" +
                          "没有此权限，相关功能将无法正常使用。您可以：\n\n" +
                          "1. 点击\"去设置\"手动开启权限\n" +
                          "2. 点击\"继续使用\"在功能受限的情况下继续使用应用")
               .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       openAppSettings();
                   }
               })
               .setNegativeButton("继续使用", null)
               .show();
    }

    /**
     * 显示功能不可用的说明
     * @param featureName 功能名称
     * @param reason 不可用原因
     * @param fallbackSolution 替代方案
     */
    public void showFeatureUnavailableGuide(String featureName, String reason, String fallbackSolution) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("功能不可用")
               .setMessage(featureName + "功能当前不可用。\n\n" +
                          "原因：" + reason + "\n\n" +
                          "替代方案：" + fallbackSolution)
               .setPositiveButton("我知道了", null)
               .show();
    }

    /**
     * 显示错误处理引导
     * @param errorType 错误类型
     * @param errorMessage 错误信息
     * @param solutions 解决方案
     */
    public void showErrorHandlingGuide(String errorType, String errorMessage, String[] solutions) {
        StringBuilder message = new StringBuilder();
        message.append("遇到了一个问题：").append(errorType).append("\n\n");
        message.append("错误详情：").append(errorMessage).append("\n\n");
        message.append("建议的解决方案：\n");

        for (int i = 0; i < solutions.length; i++) {
            message.append((i + 1)).append(". ").append(solutions[i]).append("\n");
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("问题解决指南")
               .setMessage(message.toString())
               .setPositiveButton("我知道了", null)
               .setNeutralButton("联系支持", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       // 这里可以添加联系支持的逻辑
                       showContactSupport();
                   }
               })
               .show();
    }

    /**
     * 显示加载状态对话框
     * @param message 加载信息
     * @return 对话框实例
     */
    public AlertDialog showLoadingDialog(String message) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);

        // 创建自定义布局
        View loadingView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);
        TextView textView = loadingView.findViewById(android.R.id.text1);
        textView.setText(message);
        textView.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_dialog_info, 0, 0, 0);
        textView.setCompoundDrawablePadding(16);

        builder.setView(loadingView)
               .setCancelable(false);

        return builder.show();
    }

    /**
     * 显示成功提示
     * @param message 成功信息
     */
    public void showSuccessMessage(String message) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("操作成功")
               .setMessage(message)
               .setPositiveButton("确定", null)
               .show();
    }

    /**
     * 打开应用设置页面
     */
    private void openAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            // 如果无法打开应用设置，打开系统设置
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * 显示联系支持信息
     */
    private void showContactSupport() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("联系支持")
               .setMessage("如果您遇到问题需要帮助，可以通过以下方式联系我们：\n\n" +
                          "• GitHub Issues: 在项目页面提交问题\n" +
                          "• 应用内日志: 查看详细错误日志\n" +
                          "• 重新安装: 尝试重新安装应用")
               .setPositiveButton("确定", null)
               .show();
    }
}