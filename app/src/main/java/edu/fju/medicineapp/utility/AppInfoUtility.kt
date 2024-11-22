package edu.fju.medicineapp.utility

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Created by SKYLin on 2017/3/17.
 */
object AppInfoUtility
{
    private val TAG = AppInfoUtility::class.java.simpleName

    // APP keyhash
    fun getPackageKeyHash(context: Context): String
    {
        try
        {
            val info = context.packageManager.getPackageInfo(context.applicationContext.packageName, PackageManager.GET_SIGNATURES)
            info.signatures?.let()
            {
                for (signature in it)
                {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                    SOUT.Loge("KeyHash: ", keyHash)
                    return keyHash
                }
            }

            return ""
        }
        catch (e: PackageManager.NameNotFoundException)
        {
            SOUT.Loge(AppInfoUtility.TAG, e.message!!)
        }
        catch (e: NoSuchAlgorithmException)
        {
            SOUT.Loge(AppInfoUtility.TAG, e.message!!)
        }
        return ""
    }

    //取得 APP 版本名稱
    fun getAppVersionName(context: Context): String
    {
        return try
        {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        }
        catch (e: PackageManager.NameNotFoundException)
        {
            e.printStackTrace()
            "1.0"
        }
    }

    fun getAppVersionCode(context: Context): Int
    {
        return try
        {
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        }
        catch (e: PackageManager.NameNotFoundException)
        {
            e.printStackTrace()
            -1
        }
    }

    fun hasFroyo():                     Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO }
    fun hasGingerbread():               Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD }
    fun hasHoneycomb():                 Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB }
    fun hasHoneycombMR1():              Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1 }
    fun hasHoneycombMR2():              Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2 }
    fun hasJellyBean():                 Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN }
    fun hasJellyBeanMR1():              Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 }
    fun hasJellyBeanMR2():              Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 }
    fun hasIceCream():                  Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH }
    fun hasKitKat():                    Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT }
    fun hasLolipop():                   Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP }
    fun hasLolipopMR1():                Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 }
    fun hasMarshmallow():               Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M }
    fun hasNougat():                    Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N }
    fun hasNougatMR1():                 Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 }
    fun hasOreo():                      Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O }
    fun hasOreoMR1():                   Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 }
    fun has9Pie():                      Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P }
    fun has10Q():                       Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q }
    fun has11R():                       Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R }
    fun has12S():                       Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
    fun has13T():                       Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU }
    fun has14U():                       Boolean { return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE }
}