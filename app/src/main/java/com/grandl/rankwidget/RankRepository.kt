package com.grandl.rankwidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.jsoup.Jsoup
import java.net.URL
import java.time.LocalDate

data class RankData(val tier:String,val division:String,val lp:Int,val wins:Int,val losses:Int,val wr:Int)

object RankRepository {
 const val PROFILE_URL="https://op.gg/tr/lol/summoners/tr/grandl-wave"
 private val rankRe=Regex("(Iron|Bronze|Silver|Gold|Platinum|Emerald|Diamond|Master|Grandmaster|Challenger)\\s*([1-4IVX]*)\\s+(\\d+)\\s+LP",RegexOption.IGNORE_CASE)
 private val wlRe=Regex("(\\d+)G\\s+(\\d+)M\\s+Kazanma oranı\\s+(\\d+)%",RegexOption.IGNORE_CASE)
 fun fetch():RankData{
  val doc=Jsoup.connect(PROFILE_URL).userAgent("Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36").timeout(20000).get()
  val text=doc.body().wholeText().replace(Regex("\\s+")," ")
  val solo=text.substringAfter("Dereceli Tek/Çift").substringBefore("Dereceli Esnek")
  val r=rankRe.find(solo)?:rankRe.find(text)?:error("Solo/Duo rank bulunamadı")
  val w=wlRe.find(solo)?:wlRe.find(text)?:error("Solo/Duo W/L bulunamadı")
  return RankData(r.groupValues[1].replaceFirstChar{it.uppercase()},norm(r.groupValues[2]),r.groupValues[3].toInt(),w.groupValues[1].toInt(),w.groupValues[2].toInt(),w.groupValues[3].toInt())
 }
 private fun norm(s:String)=when(s.uppercase()){"I","1"->"1";"II","2"->"2";"III","3"->"3";"IV","4"->"4";else->s}
 fun emblemUrl(tier:String)="https://opgg-static.akamaized.net/images/medals_new/${tier.lowercase()}.png?image=q_auto:good,f_png,w_288"
 fun fetchEmblem(tier:String):Bitmap?=try{URL(emblemUrl(tier)).openConnection().run{connectTimeout=15000;readTimeout=15000;getInputStream().use{BitmapFactory.decodeStream(it)}}}catch(_:Exception){null}
 fun saveAndDailyDelta(c:Context,d:RankData):Int{
  val p=c.getSharedPreferences("rank",Context.MODE_PRIVATE);val today=LocalDate.now().toString();val now=score(d)
  if(p.getString("day",null)!=today)p.edit().putString("day",today).putInt("baseScore",now).apply()
  val delta=now-p.getInt("baseScore",now)
  p.edit().putString("tier",d.tier).putString("div",d.division).putInt("lp",d.lp).putInt("wins",d.wins).putInt("losses",d.losses).putInt("wr",d.wr).putInt("delta",delta).putLong("updated",System.currentTimeMillis()).apply();return delta
 }
 private fun score(d:RankData):Int{
  val tiers=listOf("Iron","Bronze","Silver","Gold","Platinum","Emerald","Diamond");val ti=tiers.indexOfFirst{it.equals(d.tier,true)}
  if(ti>=0){val div=d.division.toIntOrNull()?.coerceIn(1,4)?:4;return ti*400+(4-div)*100+d.lp}
  return 10000+when(d.tier.lowercase()){"master"->0;"grandmaster"->2000;"challenger"->4000;else->0}+d.lp
 }
}
