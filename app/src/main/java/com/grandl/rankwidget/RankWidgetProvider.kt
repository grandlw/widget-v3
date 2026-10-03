package com.grandl.rankwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews

class RankWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, manager, it) }
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        update(context, manager, id)
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, RankWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { update(context, manager, it) }
        }

        private fun windFrames(tier: String): IntArray = when (tier.lowercase()) {
            "gold" -> intArrayOf(R.drawable.wind_gold_1,R.drawable.wind_gold_2,R.drawable.wind_gold_3)
            "platinum" -> intArrayOf(R.drawable.wind_platinum_1,R.drawable.wind_platinum_2,R.drawable.wind_platinum_3)
            "emerald" -> intArrayOf(R.drawable.wind_emerald_1,R.drawable.wind_emerald_2,R.drawable.wind_emerald_3)
            "diamond" -> intArrayOf(R.drawable.wind_diamond_1,R.drawable.wind_diamond_2,R.drawable.wind_diamond_3)
            "master" -> intArrayOf(R.drawable.wind_master_1,R.drawable.wind_master_2,R.drawable.wind_master_3)
            "grandmaster" -> intArrayOf(R.drawable.wind_grandmaster_1,R.drawable.wind_grandmaster_2,R.drawable.wind_grandmaster_3)
            "challenger" -> intArrayOf(R.drawable.wind_challenger_1,R.drawable.wind_challenger_2,R.drawable.wind_challenger_3)
            else -> intArrayOf(R.drawable.wind_neutral_1,R.drawable.wind_neutral_2,R.drawable.wind_neutral_3)
        }

        private fun update(context: Context, manager: AppWidgetManager, id: Int) {
            val p = context.getSharedPreferences("rank", Context.MODE_PRIVATE)
            val tier = p.getString("tier", "Platinum")!!
            val delta = p.getInt("delta", 0)
            val v = RemoteViews(context.packageName, R.layout.rank_widget)
            val frames = windFrames(tier)
            v.setImageViewResource(R.id.wind1, frames[0])
            v.setImageViewResource(R.id.wind2, frames[1])
            v.setImageViewResource(R.id.wind3, frames[2])
            v.setInt(R.id.windFlipper, "setFlipInterval", 6000)
            v.setBoolean(R.id.windFlipper, "setAutoStart", true)
            v.setTextViewText(R.id.wRank, "$tier ${p.getString("div", "1")}")
            v.setTextViewText(R.id.wLp, "${p.getInt("lp", 30)} LP")
            v.setTextViewText(R.id.wRecord, "${p.getInt("wins", 68)}W • ${p.getInt("losses", 55)}L • %${p.getInt("wr", 55)}")
            v.setTextViewText(R.id.wDelta, "Bugün ${if (delta >= 0) "+" else ""}$delta LP")
            RankRepository.fetchEmblem(tier)?.let { v.setImageViewBitmap(R.id.wBadge, it) }
            manager.updateAppWidget(id, v)
        }
    }
}
