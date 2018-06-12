package com.abupdate.iot_sdk;


import android.support.test.runner.AndroidJUnit4;

import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.constant.OtaConstants;
import com.abupdate.iot_libs.engine.UnitProvide;
import com.abupdate.iot_libs.info.PolicyMapInfo;
import com.abupdate.iot_libs.info.VersionInfo;
import com.abupdate.iot_libs.inter.IParsePolicyListener;
import com.abupdate.iot_libs.policy.PolicyConfig;
import com.abupdate.iot_libs.policy.PolicyManager;
import com.abupdate.iot_libs.utils.BeanUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author fighter_lee
 * @date 2018/2/9
 */
@RunWith(AndroidJUnit4.class)
public class PolicyTest {

    String[] errorS = {"abc", "111111", "false"};

    @Before
    public void init() {
        BeanUtils.setVersionInfo("{\"status\":1000,\"msg\":\"success\",\"data\":{\"releaseNotes\":{\"publishDate\":\"2017-12-14\",\"version\":\"V2\",\"content\":\"[{\\\"country\\\":\\\"zh_CN\\\",\\\"content\\\":\\\"1.优化系统.修复错误\\\"},{\\\"country\\\":\\\"en\\\",\\\"content\\\":\\\"1.Optimization system,2.Fix bugs.\\\"}]\"},\"safe\":{\"isEncrypt\":0},\"version\":{\"segmentMd5\":\"\",\"bakUrl\":\"http://iottest3.adups.com/upload/1511509320/2219695/718851db-3ed6-4d3f-99f7-d74970b2f078.zip\",\"versionAlias\":\"\",\"deltaUrl\":\"https://iottest.adups.com/upload/1511509320/2219695/718851db-3ed6-4d3f-99f7-d74970b2f078.zip\",\"deltaID\":\"2219695\",\"fileSize\":169461,\"md5sum\":\"0b6a0fef144b67ddd2e1086d6d791626\",\"versionName\":\"V2\"},\"policy\":{\"download\":[{\"key_name\":\"wifi\",\"key_message\":\"仅wifi下载\",\"key_value\":\"optional\"},{\"key_name\":\"storageSize\",\"key_message\":\"存储空间不足\",\"key_value\":\"100000\"},{\"key_name\":\"forceDownload\",\"key_message\":\"\",\"key_value\":\"false\"}],\"install\":[{\"key_name\":\"battery\",\"key_message\":\"电量不足，请充电后重试！\",\"key_value\":\"30\"},{\"key_name\":\"rebootUpgrade\",\"key_message\":\"\",\"key_value\":\"false\"},{\"key_name\":\"freeInstall\",\"key_message\":\"\",\"key_value\":\"{\\\"from\\\": \\\"00:00\\\", \\\"to\\\": \\\"23:55\\\"}\"},{\"key_name\":\"force\",\"key_message\":\"\",\"key_value\":\"{\\\"from\\\": \\\"00:00\\\", \\\"to\\\": \\\"00:00\\\",\\\"gap\\\": \\\"00:00\\\"}\"}],\"check\":[{\"key_name\":\"cycle\",\"key_message\":\"\",\"key_value\":\"1440\"},{\"key_name\":\"remind\",\"key_message\":\"\",\"key_value\":\"10080\"}]}}}");
    }

    @Test
    public void testCheckCycle() {
        PolicyMapInfo info = VersionInfo.getInstance().policyHashMap.get(OtaConstants.KEY_CHECK_CYCLE);
        info.key_value = String.valueOf(1600);
        assertThat(PolicyManager.INSTANCE.get_check_cycle(), is(1600));

        info.key_value = String.valueOf(20);
        assertThat(PolicyManager.INSTANCE.get_check_cycle(), is(60));

        info.key_value = "abc";
        assertThat(PolicyManager.INSTANCE.get_check_cycle(), is(-2));

        info.key_value = "1111111111111111111111111111";
        assertThat(PolicyManager.INSTANCE.get_check_cycle(), is(-2));

        VersionInfo.getInstance().policyHashMap.remove(OtaConstants.KEY_CHECK_CYCLE);
        assertThat(PolicyManager.INSTANCE.get_check_cycle(), is(-1));
    }

    @Test
    public void testRemindCycle() {
        PolicyMapInfo info = VersionInfo.getInstance().policyHashMap.get(OtaConstants.KEY_REMIND_CYCLE);
        info.key_value = String.valueOf(1600);
        assertThat(PolicyManager.INSTANCE.get_remind_cycle(), is(1600));

        info.key_value = String.valueOf(20);
        assertThat(PolicyManager.INSTANCE.get_remind_cycle(), is(60));

        info.key_value = "abc";
        assertThat(PolicyManager.INSTANCE.get_remind_cycle(), is(-2));

        info.key_value = "1111111111111111111111111111";
        assertThat(PolicyManager.INSTANCE.get_remind_cycle(), is(-2));

        VersionInfo.getInstance().policyHashMap.remove(OtaConstants.KEY_REMIND_CYCLE);
        assertThat(PolicyManager.INSTANCE.get_remind_cycle(), is(-1));
    }

    @Test
    public void testIsForceDownload() {
        PolicyConfig.getInstance().parsePolicyYourself(OtaConstants.PolicyType.TYPE_DOWNLOAD_FORCE, new IParsePolicyListener() {
            @Override
            public boolean doParse() {
                return false;
            }
        });

        PolicyConfig.getInstance().parsePolicyYourself(OtaConstants.PolicyType.TYPE_DOWNLOAD_FORCE, new IParsePolicyListener() {
            @Override
            public boolean doParse() {
                return true;
            }
        });

        assertThat(PolicyManager.INSTANCE.isDownloadForce(), is(true));

        PolicyConfig.getInstance().parsePolicyListenerMap.clear();

        HashMap<String, PolicyMapInfo> map = VersionInfo.getInstance().policyHashMap;
        PolicyMapInfo info = map.get(OtaConstants.KEY_DOWNLOAD_FORCE);

        info.key_value = "true";
        assertThat(PolicyManager.INSTANCE.isDownloadForce(), is(true));

        for (int i = 0; i < errorS.length; i++) {
            info.key_value = errorS[i];
            assertThat(PolicyManager.INSTANCE.isDownloadForce(), is(false));
        }

        VersionInfo.getInstance().policyHashMap.remove(OtaConstants.KEY_DOWNLOAD_FORCE);
        assertThat(PolicyManager.INSTANCE.isDownloadForce(), is(false));
    }

    @Test
    public void testRebootUpdateForce() {
        PolicyConfig.getInstance().parsePolicyYourself(OtaConstants.PolicyType.TYPE_INSTALL_REBOOT_FORCE, new IParsePolicyListener() {
            @Override
            public boolean doParse() {
                return false;
            }
        });

        PolicyConfig.getInstance().parsePolicyYourself(OtaConstants.PolicyType.TYPE_INSTALL_REBOOT_FORCE, new IParsePolicyListener() {
            @Override
            public boolean doParse() {
                return true;
            }
        });

        assertThat(PolicyManager.INSTANCE.isRebootUpdateForce(), is(true));

        PolicyConfig.getInstance().parsePolicyListenerMap.clear();

        HashMap<String, PolicyMapInfo> map = VersionInfo.getInstance().policyHashMap;
        PolicyMapInfo info = map.get(OtaConstants.KEY_REBOOT_UPDATE_FORCE);

        info.key_value = "true";
        assertThat(PolicyManager.INSTANCE.isRebootUpdateForce(), is(true));

        for (int i = 0; i < errorS.length; i++) {
            info.key_value = errorS[i];
            assertThat(PolicyManager.INSTANCE.isRebootUpdateForce(), is(false));
        }

        VersionInfo.getInstance().policyHashMap.remove(OtaConstants.KEY_REBOOT_UPDATE_FORCE);
        assertThat(PolicyManager.INSTANCE.isRebootUpdateForce(), is(false));
    }

    @Test
    public void testGetToInstallFreeTime() {
        PolicyConfig.getInstance().parsePolicyYourself(OtaConstants.PolicyType.TYPE_INSTALL_FREE_TIME, new IParsePolicyListener() {
            @Override
            public boolean doParse() {
                return true;
            }
        });

        assertThat(PolicyManager.INSTANCE.isGetToInstallFreeTime(), is(true));

        PolicyConfig.getInstance().parsePolicyListenerMap.clear();

        HashMap<String, PolicyMapInfo> map = VersionInfo.getInstance().policyHashMap;
        PolicyMapInfo info = map.get(OtaConstants.KEY_INSTALL_FREE_TIME);

        Calendar calendar = mock(Calendar.class);
        UnitProvide unitProvide = mock(UnitProvide.class);
        UnitProvide.setInstance(unitProvide);
        when(unitProvide.getCalendar()).thenReturn(calendar);
        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(11);
        when(calendar.get(Calendar.MINUTE)).thenReturn(1);

        info.key_value = "{\"from\": \"00:00\", \"to\": \"23:55\"}";
        assertThat(PolicyManager.INSTANCE.isGetToInstallFreeTime(),is(true));

        info.key_value = "{\"from\": \"12:00\", \"to\": \"23:55\"}";
        assertThat(PolicyManager.INSTANCE.isGetToInstallFreeTime(),is(false));
        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(13);
        assertThat(PolicyManager.INSTANCE.isGetToInstallFreeTime(),is(true));
        //隔天
        info.key_value = "{\"from\": \"23:00\", \"to\": \"04:00\"}";
        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(22);
        assertThat(PolicyManager.INSTANCE.isGetToInstallFreeTime(),is(false));

        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(0);
        assertThat(PolicyManager.INSTANCE.isGetToInstallFreeTime(),is(true));

        VersionInfo.getInstance().policyHashMap.remove(OtaConstants.KEY_INSTALL_FREE_TIME);
        assertThat(PolicyManager.INSTANCE.isGetToInstallFreeTime(), is(false));
    }

    @Test
    public void testIsRequest_wifi() {
        PolicyConfig.getInstance().parsePolicyYourself(OtaConstants.PolicyType.TYPE_DOWNLOAD_REQUEST_WIFI, new IParsePolicyListener() {
            @Override
            public boolean doParse() {
                return false;
            }
        });

        PolicyConfig.getInstance().parsePolicyYourself(OtaConstants.PolicyType.TYPE_DOWNLOAD_REQUEST_WIFI, new IParsePolicyListener() {
            @Override
            public boolean doParse() {
                return true;
            }
        });

        assertThat(PolicyManager.INSTANCE.is_request_wifi(), is(true));

        PolicyConfig.getInstance().parsePolicyListenerMap.clear();

        HashMap<String, PolicyMapInfo> map = VersionInfo.getInstance().policyHashMap;
        PolicyMapInfo info = map.get(OtaConstants.KEY_DOWNLOAD_WIFI);

        info.key_value = "required";
        assertThat(PolicyManager.INSTANCE.is_request_wifi(), is(true));

        for (int i = 0; i < errorS.length; i++) {
            info.key_value = errorS[i];
            assertThat(PolicyManager.INSTANCE.is_request_wifi(), is(false));
        }

        VersionInfo.getInstance().policyHashMap.remove(OtaConstants.KEY_DOWNLOAD_WIFI);
        assertThat(PolicyManager.INSTANCE.is_request_wifi(), is(false));
    }

    @Test
    public void testStorage_space_enough() {
        PolicyConfig.getInstance().parsePolicyYourself(OtaConstants.PolicyType.TYPE_DOWNLOAD_STORAGE_SIZE, new IParsePolicyListener() {
            @Override
            public boolean doParse() {
                return false;
            }
        });

        assertThat(PolicyManager.INSTANCE.is_storage_space_enough(OtaAgentPolicy.getConfig().updatePath), is(false));
        PolicyConfig.getInstance().parsePolicyListenerMap.clear();

        HashMap<String, PolicyMapInfo> map = VersionInfo.getInstance().policyHashMap;
        PolicyMapInfo info = map.get(OtaConstants.KEY_DOWNLOAD_STORAGE_SIZE);

        UnitProvide unit = mock(UnitProvide.class);
        UnitProvide.setInstance(unit);
        when(unit.getStorageSpace(OtaAgentPolicy.getConfig().updatePath)).thenReturn(100000L);
        assertThat(PolicyManager.INSTANCE.is_storage_space_enough(OtaAgentPolicy.getConfig().updatePath),is(true));

        when(unit.getStorageSpace(OtaAgentPolicy.getConfig().updatePath)).thenReturn(100001L);
        assertThat(PolicyManager.INSTANCE.is_storage_space_enough(OtaAgentPolicy.getConfig().updatePath),is(true));

        when(unit.getStorageSpace(OtaAgentPolicy.getConfig().updatePath)).thenReturn(99999L);
        assertThat(PolicyManager.INSTANCE.is_storage_space_enough(OtaAgentPolicy.getConfig().updatePath),is(false));

        VersionInfo.getInstance().policyHashMap.remove(OtaConstants.KEY_DOWNLOAD_STORAGE_SIZE);
        assertThat(PolicyManager.INSTANCE.is_storage_space_enough(OtaAgentPolicy.getConfig().updatePath), is(true));
    }

    @Test
    public void testIs_battery_enough() {
        PolicyConfig.getInstance().parsePolicyYourself(OtaConstants.PolicyType.TYPE_INSTALL_BATTERY, new IParsePolicyListener() {
            @Override
            public boolean doParse() {
                return false;
            }
        });
        assertThat(PolicyManager.INSTANCE.is_battery_enough(App.context), is(false));
        PolicyConfig.getInstance().parsePolicyListenerMap.clear();

        UnitProvide unit = mock(UnitProvide.class);
        UnitProvide.setInstance(unit);

        HashMap<String, PolicyMapInfo> map = VersionInfo.getInstance().policyHashMap;
        PolicyMapInfo info = map.get(OtaConstants.KEY_INSTALL_BATTERY);
        info.key_value = "30";

        when(unit.getBatteryLevel(App.context)).thenReturn(30);
        assertThat(PolicyManager.INSTANCE.is_battery_enough(App.context),is(true));

        info.key_value = "null";
        assertThat(PolicyManager.INSTANCE.is_battery_enough(App.context),is(true));

        info.key_value = "40";
        assertThat(PolicyManager.INSTANCE.is_battery_enough(App.context),is(false));

        info.key_value = "20";
        assertThat(PolicyManager.INSTANCE.is_battery_enough(App.context),is(true));

        VersionInfo.getInstance().policyHashMap.remove(OtaConstants.KEY_INSTALL_BATTERY);
        assertThat(PolicyManager.INSTANCE.is_battery_enough(App.context), is(true));
    }

    @Test
    public void test_is_force_install() {
        PolicyConfig.getInstance().parsePolicyYourself(OtaConstants.PolicyType.TYPE_INSTALL_FORCE, new IParsePolicyListener() {
            @Override
            public boolean doParse() {
                return false;
            }
        });

        assertThat(PolicyManager.INSTANCE.is_force_install(), is(false));

        PolicyConfig.getInstance().parsePolicyListenerMap.clear();

        HashMap<String, PolicyMapInfo> map = VersionInfo.getInstance().policyHashMap;
        PolicyMapInfo info = map.get(OtaConstants.KEY_INSTALL_FORCE);

        Calendar calendar = mock(Calendar.class);
        UnitProvide unitProvide = mock(UnitProvide.class);
        UnitProvide.setInstance(unitProvide);
        when(unitProvide.getCalendar()).thenReturn(calendar);
        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(11);
        when(calendar.get(Calendar.MINUTE)).thenReturn(1);

        info.key_value = "{\"from\": \"00:00\", \"to\": \"23:55\"}";
        assertThat(PolicyManager.INSTANCE.is_force_install(),is(true));

        info.key_value = "{\"from\": \"12:00\", \"to\": \"23:55\"}";
        assertThat(PolicyManager.INSTANCE.is_force_install(),is(false));
        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(13);
        assertThat(PolicyManager.INSTANCE.is_force_install(),is(true));
        //隔天
        info.key_value = "{\"from\": \"23:00\", \"to\": \"04:00\"}";
        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(22);
        assertThat(PolicyManager.INSTANCE.is_force_install(),is(false));

        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(0);
        assertThat(PolicyManager.INSTANCE.is_force_install(),is(true));

        VersionInfo.getInstance().policyHashMap.remove(OtaConstants.KEY_INSTALL_FORCE);
        assertThat(PolicyManager.INSTANCE.is_force_install(), is(false));
    }

}