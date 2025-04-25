# Privacy Policy

The protection of your data is particularly important to us. Therefore, we process your data solely in accordance with applicable data protection laws (such as the GDPR[^1]).
We take appropriate precautions to protect your data from loss, manipulation, and unauthorized access.
This privacy policy informs you about the most important aspects of data processing within our apps.

**Applications that do not collect any personal information:**

- Dhizuku
- Dhizuku-API-Xposed

[^1]: **G**eneral **D**ata **P**rotection **R**egulation

## 1. Personal Information

We do not collect or store any personal information that could identify users of our apps.

## 2. Anonymous Identifiers

No anonymous identifiers are created or collected.

## 3. Use of Third-Party Services

- **Shizuku-API**[^2] (included provider)  
Used as an optional method to activate the app.

- **XXPermissions**[^3]  
Required for managing storage and notification permissions properly.

[^2]: <https://github.com/RikkaApps/Shizuku-API>
[^3]: <https://github.com/getActivity/XXPermissions>

## 4. Uses Permissions

The Dhizuku core only uses a portion of the declared permissions.
Other permissions may be **used by third-party applications**(DPCs[^4]) that use Dhizuku’s APIs depending on your permission.


- `FOREGROUND_SERVICE`[^5]
- `FOREGROUND_SERVICE_REMOTE_MESSAGING`[^5]
- `RECEIVE_BOOT_COMPLETED`[^6]

> Required to maintain Dhizuku or other DPCs[^4] service.

- `GET_ACCOUNTS`[^7]
- `MANAGE_ACCOUNTS`[^7]

> Required for profile operations from Dhizuku or other DPCs[^4].

- `QUERY_ALL_PACKAGES`[^8]

> Required to get the DPCs[^4] list.

- `READ_EXTERNAL_STORAGE`[^9]
- `WRITE_EXTERNAL_STORAGE`[^9]
- `MANAGE_EXTERNAL_STORAGE`[^10]

> Required when operating files from other DPCs[^4].

- `REQUEST_PASSWORD_COMPLEXITY`[^11]

> Required when changing advanced settings for screen lock password from other DPCs[^4].

- `moe.shizuku.manager.permission.API_V23`[^2]

> Required to enable Dhizuku's feature via Shizuku[^12].

[^4]: **D**evice **P**olicy **C**ontroller(s)
[^12]: <https://github.com/RikkaApps/Shizuku>

[^5]: <https://developer.android.com/develop/background-work/services/fgs/service-types#remote-messaging>
[^6]: <https://developer.android.com/develop/background-work/services/alarms/schedule#boot>
[^7]: <https://developer.android.com/reference/android/accounts/AccountManager>
[^8]: <https://developer.android.com/training/package-visibility/declaring#all-apps>
[^9]: <https://developer.android.com/training/data-storage#permissions>
[^10]: <https://developer.android.com/training/data-storage/manage-all-files?hl=ja#operations-allowed-manage-external-storage>
[^11]: <https://developer.android.com/work/versions/android-10#screen_lock_quality_check>

## 5. Your Rights

You have the right to disable and remove this app at any time.

## 6. Contact

For inquiries, please contact us via [GitHub Issues](https://github.com/iamr0s/Dhizuku/issues), and a maintainer will respond accordingly.
