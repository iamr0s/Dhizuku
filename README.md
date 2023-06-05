简体中文 | [English](README_EN.md)

# Dhizuku

## 介绍

参考Shizuku的设计思想，分享 DeviceOwner (设备所有者) 权限给其余应用


## 下载

支持的 Android 版本：5.0 ~ 13

[![Downloads](https://img.shields.io/github/downloads/iamr0s/Dhizuku/total?label=Downloads)](https://github.com/iamr0s/Dhizuku/releases)
[![Lastest](https://img.shields.io/github/v/release/iamr0s/Dhizuku?label=Lastest)](https://github.com/iamr0s/Dhizuku/releases/latest)

<details><summary><h3>常见错误&解决方案</h3></summary>

#### 📌未能获得 `device-owner` 权限

##### ❗报错

```shell
java.lang.IllegalStateException: Not allowed to set the device owner because there are already several users on the device
```

##### 🎯解决方案

在设置中关闭多用户或删除所有其他用户，然后再次尝试。

<b>Oneplus</b>：若依旧报错，请执行 `adb shell pm list users` ，若输出中存在以下内容

```
UserInfo{999:MultiApp:4001010} running
```

请执行以下命令

```shell
pm remove-user 999
pm disable-user 999
```

有报错是正常现象，完成后即可再次尝试激活。


</details>


## 致开发者（接入Dhizuku API）

- [Dhizuku API](https://github.com/iamr0s/Dhizuku-API.git)

## 参与翻译

如果你想参与到Dhizuku的翻译工作中来，请前往[Weblate](https://hosted.weblate.org/engage/dhizuku/)

[![status](https://hosted.weblate.org/widgets/dhizuku/-/multi-auto.svg)](https://hosted.weblate.org/engage/dhizuku/)

## 捐赠支持

- [支付宝](https://qr.alipay.com/fkx18580lfpydiop04dze47)
- [微信](https://missuo.ru/file/fee5df1381671c996b127.png)
- [币安](https://missuo.ru/file/28368c28d4ff28d59ed4b.jpg)

## 开源协议

Dhizuku目前基于 [**GNU General Public License v3 (GPL-3)**](http://www.gnu.org/copyleft/gpl.html) 开源，且保证未来依然继续遵循此协议开源。
