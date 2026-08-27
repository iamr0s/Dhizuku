package com.rosan.dhizuku.data.account.model

import android.accounts.Account
import android.accounts.IAccountManager
import android.accounts.IAccountManagerResponse
import android.content.Context
import android.content.pm.IPackageManager
import android.content.pm.PackageInfo
import android.os.Build
import android.os.Bundle
import android.os.IUserManager
import android.os.RemoteException
import com.rosan.dhizuku.data.common.util.dumpText
import com.rosan.dhizuku.data.common.util.getParcelableCompat
import com.rosan.dhizuku.data.common.util.requireShizukuPermissionGranted
import com.rosan.dhizuku.data.common.util.shizukuBinder
import com.rosan.dhizuku.data.account.entity.AccountAuthenticatorEntity
import com.rosan.dhizuku.data.account.entity.AccountEntity
import com.rosan.dhizuku.data.account.entity.UserEntity
import com.rosan.dhizuku.data.account.repo.UserService
import rikka.shizuku.Shizuku
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShizukuUserService(private val context: Context) : UserService {
    // 本地包管理器：仅传入包名字符串读取资源，不接收远程ApplicationInfo
    private val basePackageManager by lazy { context.packageManager }
    // Shizuku远程Binder服务
    private val userManager by lazy { IUserManager.Stub.asInterface(shizukuBinder(Context.USER_SERVICE)) }
    private val accountManager by lazy { IAccountManager.Stub.asInterface(shizukuBinder(Context.ACCOUNT_SERVICE)) }
    private val packageManager by lazy { IPackageManager.Stub.asInterface(shizukuBinder("package")) }

    private fun writeLog(message: String) {
        try {
            val dir = context.getExternalFilesDir(null)
            if (dir != null) {
                val logFile = File(dir, "log.txt")
                val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                logFile.appendText("[$time] $message\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun removeUser(user: UserEntity): Boolean = removeUser(user.id)

    override suspend fun removeUser(userId: Int): Boolean = userManager.removeUser(userId)

    override suspend fun getUsers(): List<UserEntity> = requireShizukuPermissionGranted(context) {
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            userManager.getUsers(false, false, false)
        else userManager.getUsers(false)).map {
            UserEntity(id = it.id, name = it.name)
        }
    }

    override suspend fun getAccountAuthenticators(userId: Int): List<AccountAuthenticatorEntity> =
        requireShizukuPermissionGranted(context) {
            val flags = 0x00000200L // PackageManager.MATCH_DISABLED_COMPONENTS
            accountManager.getAuthenticatorTypes(userId).mapNotNull { description ->
                try {
                    val packageName = description.packageName
                    // 远程仅获取PackageInfo，不使用内部ApplicationInfo
                    val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        packageManager.getPackageInfo(packageName, flags, userId)
                    else packageManager.getPackageInfo(packageName, flags.toInt(), userId)

                    // 本地PackageManager通过【包名字符串】读取资源，不传递远程ApplicationInfo
                    val appLabel = basePackageManager.getApplicationLabel(basePackageManager.getApplicationInfo(packageName, 0)).toString()
                    val descLabel = basePackageManager.getText(packageName, description.labelId, null).toString()
                    val label = if (appLabel == descLabel) appLabel else "$appLabel - $descLabel"
                    val icon = basePackageManager.getApplicationIcon(packageName)

                    AccountAuthenticatorEntity(
                        userId = userId,
                        type = description.type,
                        packageName = description.packageName,
                        label = label,
                        icon = icon
                    )
                } catch (e: RemoteException) {
                    writeLog("getAccountAuthenticators RemoteException for ${description.packageName}: ${e.message}")
                    null
                } catch (e: Exception) {
                    writeLog("getAccountAuthenticators exception for ${description.packageName}: ${e.message}")
                    null
                }
            }
        }

    override suspend fun getAccounts(userId: Int): List<AccountEntity> =
        requireShizukuPermissionGranted(context) {
            val dumpAccounts = getAccountsByDump(userId)
            val managerAccounts = try {
                getAccountsByManager(userId)
            } catch (e: Exception) {
                emptyList()
            }

            val managerGrouped = managerAccounts.groupBy { it.type }.mapValues { it.value.toMutableList() }

            dumpAccounts.map { dumpAccount ->
                val managerList = managerGrouped[dumpAccount.type]
                if (managerList != null && managerList.isNotEmpty()) {
                    val managerAccount = managerList.removeAt(0)
                    dumpAccount.copy(name = managerAccount.name)
                } else {
                    dumpAccount
                }
            }
        }

    private fun getAccountsByManager(userId: Int): List<AccountEntity> {
        return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val callingPackage = try {
                basePackageManager.getPackagesForUid(Shizuku.getUid())?.firstOrNull() ?: "android"
            } catch (e: Exception) {
                "android"
            }
            accountManager.getAccountsAsUser(null, userId, callingPackage)
        } else accountManager.getAccountsAsUser(null, userId)).map {
            AccountEntity(
                userId = userId,
                type = it.type,
                name = it.name
            )
        }
    }

    private fun getAccountsByDump(userId: Int): List<AccountEntity> {
        val text = accountManager.asBinder().dumpText()
        val lines = text.lines()

        val userStartRegex = """^\s*User UserInfo\{(\d+)[:\}].*""".toRegex()
        var inUserBlock = false
        val userLines = mutableListOf<String>()

        for (line in lines) {
            val match = userStartRegex.matchEntire(line)
            if (match != null) {
                val currentUserId = match.groupValues[1].toInt()
                if (currentUserId == userId) {
                    inUserBlock = true
                    continue
                } else if (inUserBlock) {
                    break
                }
            }
            if (inUserBlock) {
                if (line.isNotEmpty() && !line.first().isWhitespace()) {
                    break
                }
                userLines.add(line)
            }
        }

        val accountLines = mutableListOf<String>()
        var foundAccountsSection = false
        for (line in userLines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("Accounts:")) {
                foundAccountsSection = true
                continue
            }
            if (foundAccountsSection) {
                if (line.isEmpty()) continue
                val leadingSpaces = line.length - line.trimStart().length
                if (leadingSpaces <= 2) {
                    break
                }
                accountLines.add(line)
            }
        }

        val accountsText = accountLines.joinToString("\n")
        val accountRegex = """Account\s*\{\s*name=(.*?), type=(.*?)[,}]""".toRegex()

        return accountRegex.findAll(accountsText)
            .map {
                val name = it.groupValues[1]
                val type = it.groupValues[2]

                AccountEntity(
                    userId = userId,
                    type = type,
                    name = name
                )
            }
            .toList()
    }

    override suspend fun removeAccount(account: AccountEntity): Boolean = requireShizukuPermissionGranted(context) {
        val targetAccount = Account(account.name, account.type)
        val result = suspendCancellableCoroutine<Boolean> { continuation ->
            val response = object : IAccountManagerResponse.Stub() {
                override fun onResult(value: Bundle?) {
                    val intent = value?.getParcelableCompat<android.content.Intent>("intent")
                    if (intent != null) {
                        try {
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    val success = value?.getBoolean("booleanResult", false) ?: false
                    if (continuation.isActive) {
                        continuation.resume(success)
                    }
                }

                override fun onError(errorCode: Int, errorMessage: String?) {
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }
            }
            try {
                accountManager.removeAccountAsUser(response, targetAccount, false, account.userId)
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWith(Result.failure(e))
                }
            }
        }
        result
    }

    override suspend fun setPackageEnabled(packageName: String, enabled: Boolean, userId: Int): Boolean = requireShizukuPermissionGranted(context) {
        val newState = if (enabled) 1 else 3
        val callingPackage = when (Shizuku.getUid()) {
            0 -> "android"
            2000 -> "com.android.shell"
            else -> try {
                basePackageManager.getPackagesForUid(Shizuku.getUid())?.firstOrNull() ?: "android"
            } catch (e: Exception) {
                "android"
            }
        }
        writeLog("setPackageEnabled: pkg=$packageName enabled=$enabled state=$newState callingPkg=$callingPackage userId=$userId")
        try {
            packageManager.setApplicationEnabledSetting(packageName, newState, 0, userId, callingPackage)
            writeLog("setApplicationEnabledSetting success for $packageName")
            true
        } catch (e: Exception) {
            writeLog("setApplicationEnabledSetting error: ${e.message}")
            throw e
        }
    }

    override suspend fun isPackageEnabled(packageName: String, userId: Int): Boolean = requireShizukuPermissionGranted(context) {
        try {
            val flags = 0x00000200L
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, flags, userId)
            } else {
                packageManager.getPackageInfo(packageName, flags.toInt(), userId)
            }
            val enabledState = info?.applicationInfo?.enabled ?: true
            writeLog("isPackageEnabled of $packageName: $enabledState")
            enabledState
        } catch (e: Exception) {
            writeLog("isPackageEnabled exception of $packageName: ${e.message}")
            e.printStackTrace()
            true
        }
    }
}