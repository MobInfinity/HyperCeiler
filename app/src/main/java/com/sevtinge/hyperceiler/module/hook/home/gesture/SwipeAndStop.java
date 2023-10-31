package com.sevtinge.hyperceiler.module.hook.home.gesture;

import static com.sevtinge.hyperceiler.utils.Helpers.findAndHookMethodUseUnhook;

import android.content.Context;
import android.os.Bundle;

import com.sevtinge.hyperceiler.module.app.GlobalActions;
import com.sevtinge.hyperceiler.module.base.BaseHook;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public class SwipeAndStop extends BaseHook {
    @Override
    public void init() {
            Class<?> VibratorCls = findClassIfExists("android.os.Vibrator", lpparam.classLoader);
            hookAllMethods("com.miui.home.recents.GestureBackArrowView", "setReadyFinish", new MethodHook() {
                private Unhook vibratorHook = null;
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    vibratorHook = findAndHookMethodUseUnhook(VibratorCls, "vibrate", long.class, XC_MethodReplacement.DO_NOTHING);
                }
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    if (vibratorHook != null) {
                        vibratorHook.unhook();
                    }
                }
            });

        findAndHookMethod("com.miui.home.recents.GestureStubView", "disableQuickSwitch", boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = false;
            }
        });
        findAndHookMethod("com.miui.home.recents.GestureStubView", "isDisableQuickSwitch", XC_MethodReplacement.returnConstant(false));
        findAndHookMethod("com.miui.home.recents.GestureStubView","getNextTask", Context.class, boolean.class, int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                boolean switchApp = (boolean) param.args[1];
                if (switchApp) {
                    Context mContext = (Context) param.args[0];
                    Bundle bundle = new Bundle();
                    bundle.putInt("inDirection", (int)param.args[2]);
                    if (GlobalActions.handleAction(mContext, "pref_key_controls_fsg_swipeandstop")) {
                        Class<?> Task = findClassIfExists("com.android.systemui.shared.recents.model.Task", lpparam.classLoader);
                        param.setResult(XposedHelpers.newInstance(Task));
                        return;
                    }
                }
                param.setResult(null);
            }
        });
    }
}