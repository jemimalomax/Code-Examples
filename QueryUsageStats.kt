package com.example.chronoboss

import android.app.AppOpsManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.provider.Settings
import android.widget.TextView

/** fragment class that queries usage statistics for the user */
class QueryStatsFragment() : Fragment() {

    /** inflates the fragment for the activity
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //inflate view
        val view:View = inflater.inflate(R.layout.fragment_query_stats, container, false)
        //check if has stats access, if not request
        if(!hasPermission()){
            requestUsageStatsPermission()
        }
        //find the top package
        val top:UsageStats? = getTopPackage()
        //find the textview for the top package name
        val setTopView: TextView? = view.findViewById(R.id.top_package_name)
        //set the text to the top package name
        setTopView?.setText(top?.packageName)
        //convert the time the top app was used to a string
        val topTimeString:String? = top?.totalTimeInForeground.toString()
        //find the textview for the top package time used
        val timeV:TextView? = view.findViewById(R.id.top_package_time)
        //set it to the time the app was used
        timeV?.setText(topTimeString)
        //return the view that was inflated
        return view
    }

    /** function to open settings to allow access to usage stats from OS */
    fun requestUsageStatsPermission() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    /** function to query the usage statistics from the OS in given interval */
    fun getStats(context: Context?): List<UsageStats> {
        val statsManager =
            context?.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val milliDay = 86400000
        val endTime: Long = System.currentTimeMillis()
        val beginTime: Long = endTime - milliDay
        val usageSt: List<UsageStats> =
            statsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginTime, endTime)
        return usageSt
    }

    /** function to get the top package name based on the list of packages gathered
     * from the query
     */
    fun getTopPackage():UsageStats?{
        val usageSt = getStats(context)
        var timeUsed:Long = 0
        var topPack:UsageStats? = null
        for(pck in usageSt){
            if((pck.totalTimeInForeground > timeUsed ) &&
                (pck.packageName != "com.google.android.apps.nexuslauncher") &&
                (pck.packageName != "com.example.chronoboss")){
                timeUsed = pck.totalTimeInForeground
                topPack = pck
            }

        }
        return topPack
    }

    /** function to check whether our app has been granted access to query usage
     * statistics from the operating system
     */
    fun hasPermission():Boolean {
        val applicationInform: ApplicationInfo? =
            context?.packageName?.let { activity?.packageManager?.getApplicationInfo(it, 0) }
        val appOps: AppOpsManager =
            context?.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode: Int? = applicationInform?.let {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                it.uid,
                applicationInform.packageName
            )
        }
        return (mode == AppOpsManager.MODE_ALLOWED)
    }
}
